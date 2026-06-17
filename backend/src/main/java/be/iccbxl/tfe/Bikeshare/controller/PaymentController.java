package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.GainService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.PaymentService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ReservationService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Paiement d'une location via Stripe Checkout (page de paiement hébergée par Stripe). */
@Controller
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private static final double COMMISSION_RATE = 0.15; // commission BikeShare : 15 %

    @Autowired private ReservationService reservationService;
    @Autowired private BikeService bikeService;
    @Autowired private PaymentService paymentService;
    @Autowired private GainService gainService;

    @Value("${stripe.api.key}") private String stripeApiKey;

    @PostConstruct
    public void init() { Stripe.apiKey = stripeApiKey; }

    /** Démarre le paiement : crée une session Stripe Checkout et redirige vers la page de paiement. */
    @PostMapping("/account/reservations/{id}/pay")
    public String pay(@PathVariable Long id,
                      @AuthenticationPrincipal CustomUserDetail userDetails,
                      RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        if (r == null || r.getUser() == null || !r.getUser().getId().equals(userDetails.getUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Réservation introuvable ou accès refusé.");
            return "redirect:/account/reservations";
        }
        if (r.getPayment() != null) {
            redirectAttributes.addFlashAttribute("info", "Cette réservation est déjà payée.");
            return "redirect:/account/reservations";
        }

        int days = (r.getDuration() != null && r.getDuration() > 0) ? r.getDuration() : 1;
        double amount = bikeService.computeTotal(r.getBike(), days);
        if (amount <= 0) {
            redirectAttributes.addFlashAttribute("error", "Montant de la réservation invalide.");
            return "redirect:/account/reservations";
        }
        String bikeLabel = (r.getBike() != null) ? r.getBike().getBrand() + " " + r.getBike().getModel() : "Vélo";
        String base = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(base + "/payment/success?session_id={CHECKOUT_SESSION_ID}&reservationId=" + id)
                    .setCancelUrl(base + "/payment/cancel")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount(Math.round(amount * 100))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Location BikeShare — " + bikeLabel)
                                            .build())
                                    .build())
                            .build())
                    .build();
            Session session = Session.create(params);
            return "redirect:" + session.getUrl();
        } catch (Exception e) {
            logger.error("Création de la session Stripe échouée : {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Le paiement est momentanément indisponible. Réessayez plus tard.");
            return "redirect:/account/reservations";
        }
    }

    /** Retour Stripe après paiement réussi : enregistre le paiement + le gain du propriétaire et confirme la réservation. */
    @GetMapping("/payment/success")
    public String success(@RequestParam("session_id") String sessionId,
                          @RequestParam Long reservationId,
                          @AuthenticationPrincipal CustomUserDetail userDetails,
                          RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(reservationId);
        if (r == null || r.getUser() == null || !r.getUser().getId().equals(userDetails.getUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Réservation introuvable.");
            return "redirect:/account/reservations";
        }
        if (r.getPayment() != null) { // déjà traité (ex. rafraîchissement de la page)
            redirectAttributes.addFlashAttribute("success", "Paiement déjà enregistré.");
            return "redirect:/account/reservations";
        }
        try {
            Session session = Session.retrieve(sessionId);
            if (!"paid".equals(session.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("error", "Le paiement n'a pas été confirmé.");
                return "redirect:/account/reservations";
            }
            double amount = (session.getAmountTotal() != null) ? session.getAmountTotal() / 100.0
                    : bikeService.computeTotal(r.getBike(), r.getDuration() != null ? r.getDuration() : 1);
            double commission = Math.round(amount * COMMISSION_RATE * 100.0) / 100.0;

            Payment payment = new Payment();
            payment.setReservation(r);
            payment.setStatut("PAID");
            payment.setPaymentMode("STRIPE");
            payment.setTotalPrice(amount);
            payment.setPartBikeshare(commission);
            paymentService.save(payment);

            Gain gain = new Gain();
            gain.setPayment(payment);
            gain.setAmountEarned(amount - commission);
            gain.setStatus("PENDING");
            gain.setDescription("Gain location — " +
                    (r.getBike() != null ? r.getBike().getBrand() + " " + r.getBike().getModel() : ""));
            gainService.save(gain);

            r.setStatut("CONFIRMED");
            reservationService.saveReservation(r);

            redirectAttributes.addFlashAttribute("success", "Paiement réussi ! Votre réservation est confirmée.");
        } catch (Exception e) {
            logger.error("Traitement du retour Stripe échoué : {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la confirmation du paiement.");
        }
        return "redirect:/account/reservations";
    }

    @GetMapping("/payment/cancel")
    public String cancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", "Paiement annulé. Vous pouvez réessayer quand vous voulez.");
        return "redirect:/account/reservations";
    }
}

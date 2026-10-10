package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.service.serviceImpl.FinanceService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import be.iccbxl.tfe.Bikeshare.service.ReservationServiceI;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

/** Finances de la plateforme : paiements, versements aux propriétaires, remboursements. Réservé aux admins. */
@Controller
@RequestMapping("/admin/finances")
public class AdminFinanceController {

    @Autowired private FinanceService financeService;
    @Autowired private PaymentService paymentService;
    @Autowired private ReservationServiceI reservationService;
    @Autowired private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(AdminFinanceController.class);

    @GetMapping
    public String finances(Model model) {
        model.addAttribute("paiements", financeService.getPaiements());
        model.addAttribute("gains", financeService.getGains());
        model.addAttribute("remboursements", financeService.getRemboursements());
        model.addAttribute("totalRevenu", paymentService.getTotalRevenue());
        model.addAttribute("totalBenefit", paymentService.getTotalBenefit());
        model.addAttribute("montantDu", financeService.getMontantDu());
        model.addAttribute("nbPaiements", financeService.getPaiements().size());
        model.addAttribute("demandesAnnulation", financeService.getDemandesAnnulation());
        return "admin/finances/index";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String annulerLocation(@PathVariable Long id, @RequestParam int pourcentage, RedirectAttributes ra) {
        Reservation r = reservationService.getReservationById(id);
        boolean ok = r != null && financeService.annulerAvecRemboursement(id, pourcentage);
        if (ok) {
            notifierAnnulation(r, pourcentage);
            ra.addFlashAttribute("success", pourcentage == 100
                    ? "Location annulée avec remboursement intégral. N'oubliez pas de rembourser sur Stripe."
                    : "Location annulée sans remboursement.");
        } else {
            ra.addFlashAttribute("error",
                    "Annulation impossible : location non payée, déjà annulée, ou pourcentage non autorisé.");
        }
        return "redirect:/admin/finances";
    }

    /** Prévient le locataire et le propriétaire de l'annulation (cloche). */
    private void notifierAnnulation(Reservation r, int pourcentage) {
        String message = pourcentage == 100
                ? "Location annulée avec remboursement intégral."
                : "Location annulée sans remboursement.";
        try {
            notificationService.notify(r.getUser(), r.getBike().getUser(), r.getBike(),
                    "RESERVATION", message, "/account/reservations");
            notificationService.notify(r.getBike().getUser(), r.getUser(), r.getBike(),
                    "RESERVATION", message, "/account/received-reservations");
        } catch (Exception e) {
            logger.warn("Notification d'annulation non créée : {}", e.getMessage());
        }
    }

    @PostMapping("/gains/{id}/transfer")
    public String marquerVerse(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetail admin,
                               RedirectAttributes ra) {
        Gain gain = financeService.getGains().stream()
                .filter(g -> id.equals(g.getId())).findFirst().orElse(null);
        boolean ok = financeService.marquerVerse(id);
        if (ok && gain != null && admin != null) {
            notifierVersement(gain, admin.getUser());
        }
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Gain marqué comme versé au propriétaire." : "Gain introuvable ou déjà versé.");
        return "redirect:/admin/finances";
    }

    /** Prévient le propriétaire que son gain a été versé. */
    private void notifierVersement(Gain gain, User admin) {
        try {
            Reservation r = gain.getPayment().getReservation();
            String montant = String.format("%.2f", gain.getAmountEarned()).replace('.', ',');
            notificationService.notify(r.getBike().getUser(), admin, r.getBike(), "RESERVATION",
                    "Votre gain de " + montant + " € a été versé.", "/account/gains");
        } catch (Exception e) {
            logger.warn("Notification de versement non créée : {}", e.getMessage());
        }
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exporterCsv() {
        String csv = financeService.exporterCsv(financeService.getPaiements());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"paiements-bikeshare.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}

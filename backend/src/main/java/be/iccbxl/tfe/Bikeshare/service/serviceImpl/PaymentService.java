package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.GainRepository;
import be.iccbxl.tfe.Bikeshare.repository.PaymentRepository;
import be.iccbxl.tfe.Bikeshare.repository.ReservationRepository;
import be.iccbxl.tfe.Bikeshare.service.PaymentServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService implements PaymentServiceI {

    /** Commission prélevée par BikeShare : 15 % (le propriétaire touche 85 %). */
    private static final double COMMISSION_RATE = 0.15;

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private GainRepository gainRepository;
    @Autowired private ReservationRepository reservationRepository;

    public Payment save(Payment payment) { return paymentRepository.save(payment); }

    /**
     * Confirme un paiement de façon ATOMIQUE (« tout ou rien ») : calcule la commission,
     * enregistre le paiement + le gain du propriétaire, puis passe la réservation en CONFIRMED.
     * Si une étape échoue, toute la transaction est annulée (jamais de données incohérentes).
     */
    @Transactional
    public void confirmPayment(Reservation r, double amount) {
        double commission = Math.round(amount * COMMISSION_RATE * 100.0) / 100.0;
        Payment payment = new Payment();
        payment.setReservation(r);
        payment.setStatut("PAID");
        payment.setPaymentMode("STRIPE");
        payment.setTotalPrice(amount);
        payment.setPartBikeshare(commission);
        paymentRepository.save(payment);

        Gain gain = new Gain();
        gain.setPayment(payment);
        gain.setAmountEarned(amount - commission);
        gain.setStatus("PENDING");
        gain.setDescription("Gain location — " +
                (r.getBike() != null ? r.getBike().getBrand() + " " + r.getBike().getModel() : ""));
        gainRepository.save(gain);

        r.setStatut("CONFIRMED");
        reservationRepository.save(r);
    }

    @Override
    public List<Payment> getPaymentsForUser(User user, LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getReservation() != null
                        && p.getReservation().getUser() != null
                        && p.getReservation().getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }

    /** Revenu total = somme des montants payés. */
    public BigDecimal getTotalRevenue() {
        double total = paymentRepository.findAll().stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getStatut()))
                .mapToDouble(Payment::getTotalPrice).sum();
        return BigDecimal.valueOf(total);
    }

    /** Bénéfice total = somme des commissions BikeShare. */
    public BigDecimal getTotalBenefit() {
        double total = paymentRepository.findAll().stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getStatut()))
                .mapToDouble(Payment::getPartBikeshare).sum();
        return BigDecimal.valueOf(total);
    }
}

package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.PaymentRepository;
import be.iccbxl.tfe.Bikeshare.service.PaymentServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService implements PaymentServiceI {

    @Autowired private PaymentRepository paymentRepository;

    public Payment save(Payment payment) { return paymentRepository.save(payment); }

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

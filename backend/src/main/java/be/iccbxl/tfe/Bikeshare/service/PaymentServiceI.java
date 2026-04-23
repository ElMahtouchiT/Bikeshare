package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentServiceI {
    List<Payment> getPaymentsForUser(User user, LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalRevenue();
    BigDecimal getTotalBenefit();
}

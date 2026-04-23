package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}

package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    java.util.List<Claim> findByReservationId(Long reservationId);
    boolean existsByReservationId(Long reservationId);
}

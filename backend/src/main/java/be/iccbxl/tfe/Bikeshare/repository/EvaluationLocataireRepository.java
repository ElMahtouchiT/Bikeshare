package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.EvaluationLocataire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationLocataireRepository extends JpaRepository<EvaluationLocataire, Long> {
    boolean existsByReservationId(Long reservationId);
}

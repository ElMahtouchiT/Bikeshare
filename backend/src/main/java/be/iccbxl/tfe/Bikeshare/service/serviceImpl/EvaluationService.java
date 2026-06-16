package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.DTO.EvaluationDTO;
import be.iccbxl.tfe.Bikeshare.DTO.EvaluationDashboardDTO;
import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.model.Evaluation;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.repository.EvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationService {
    @Autowired private EvaluationRepository evaluationRepository;

    public Evaluation save(Evaluation e) { return evaluationRepository.save(e); }
    public void deleteEvaluationById(Long id) { evaluationRepository.deleteById(id); }

    /** Crée et enregistre l'évaluation d'une réservation (note 1-5 + commentaire). */
    public Evaluation createForReservation(Reservation reservation, int note, String comment) {
        Evaluation e = new Evaluation();
        e.setReservation(reservation);
        e.setNote(note);
        e.setComment(comment);
        return evaluationRepository.save(e); // createdAt renseigné par @PrePersist
    }

    public List<EvaluationDTO> getAllEvaluationsGroupedByBike() {
        return evaluationRepository.findAll().stream()
                .map(MapperDTO::toEvaluationDTO).collect(Collectors.toList());
    }

    public List<EvaluationDTO> getEvaluationsByBikeId(Long bikeId) {
        return evaluationRepository.findByReservationBikeId(bikeId).stream()
                .map(MapperDTO::toEvaluationDTO).collect(Collectors.toList());
    }

    public EvaluationDashboardDTO getEvaluationDashboardData() {
        List<Evaluation> all = evaluationRepository.findAll();
        EvaluationDashboardDTO dto = new EvaluationDashboardDTO();
        dto.setTotalEvaluations(all.size());
        dto.setAverageNote(all.isEmpty() ? 0d :
                all.stream().mapToInt(Evaluation::getNote).average().orElse(0d));
        dto.setLatest(all.stream().limit(10).map(MapperDTO::toEvaluationDTO).collect(Collectors.toList()));
        return dto;
    }
}

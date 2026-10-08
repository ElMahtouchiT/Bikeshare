package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.EvaluationLocataire;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.repository.EvaluationLocataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/** Évaluations des locataires par les propriétaires. */
@Service
public class EvaluationLocataireService {

    @Autowired private EvaluationLocataireRepository evaluationLocataireRepository;

    public boolean existePour(Long reservationId) {
        return evaluationLocataireRepository.existsByReservationId(reservationId);
    }

    /** Identifiants des réservations déjà évaluées par le propriétaire. */
    public Set<Long> idsReservationsEvaluees() {
        return evaluationLocataireRepository.findAll().stream()
                .map(e -> e.getReservation().getId())
                .collect(Collectors.toSet());
    }

    public EvaluationLocataire creer(Reservation reservation, int note, String comment) {
        EvaluationLocataire evaluation = new EvaluationLocataire();
        evaluation.setReservation(reservation);
        evaluation.setNote(note);
        evaluation.setComment(comment);
        return evaluationLocataireRepository.save(evaluation);
    }
}

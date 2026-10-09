package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    private Long id;
    private LocalDate startLocation;
    private LocalDate endLocation;
    private Integer duration;
    private String statut;
    private String assurance;
    private LocalDateTime createdAt;
    private BikeDTO bike;
    private UserPublicDTO user;
    private Double totalPrice;
    private Integer evaluationNote; // note de l'évaluation si la location a été évaluée, sinon null
    private boolean paid;           // vrai si un paiement PAID existe pour cette réservation
    private boolean cancellationRequested;      // le locataire a demandé l'annulation (location payée)
    private boolean annulableDirectement;       // le locataire peut annuler sans l'administrateur (non payée)
    private boolean demandeAnnulationPossible;  // le locataire peut demander l'annulation à l'administrateur
}

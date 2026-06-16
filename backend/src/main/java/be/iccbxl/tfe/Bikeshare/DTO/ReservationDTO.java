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
    private UserDTO user;
    private Double totalPrice;
    private Integer evaluationNote; // note de l'évaluation si la location a été évaluée, sinon null
}

package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvaluationDTO {
    private Long id;
    private int note;
    private String comment;
    private LocalDateTime createdAt;
    private Long reservationId;
    private Long bikeId;
    private String bikeLabel;
    private String authorName;
}

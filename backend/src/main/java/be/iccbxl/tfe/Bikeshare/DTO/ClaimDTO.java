package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClaimDTO {
    private Long id;
    private String claimantRole;
    private String message;
    private String status;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime responseAt;
    private Long reservationId;
}

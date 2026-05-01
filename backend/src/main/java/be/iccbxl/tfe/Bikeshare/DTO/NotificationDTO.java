package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;
    private Long bikeId;
    private Long fromUserId;
    private Long toUserId;
}

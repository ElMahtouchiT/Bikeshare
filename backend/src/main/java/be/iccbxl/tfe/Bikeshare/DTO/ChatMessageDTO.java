package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;
    private String content;
    private Long reservationId;
    private Long fromUserId;
    private Long toUserId;
    private LocalDateTime sentAt;
}

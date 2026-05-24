package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class UserReservationKpiDTO {
    private Long userId;
    private String name;
    private long reservationCount;
}

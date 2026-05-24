package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class BikeReservationKpiDTO {
    private Long bikeId;
    private String label;
    private long reservationCount;
}

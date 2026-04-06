package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;

@Data
public class PriceDTO {
    private Long id;
    private Double lowPrice;
    private Double middlePrice;
    private Double highPrice;
    private Double promo1;
    private Double promo2;
}

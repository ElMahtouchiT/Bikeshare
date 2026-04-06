package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.util.List;

@Data
public class BikeDTO {
    private Long id;
    private String brand;
    private String model;
    private String bikeType;
    private boolean electric;
    private String wheelSize;
    private Integer gears;
    private String frameSize;
    private String adresse;
    private String postalCode;
    private String locality;
    private String reservationMode;
    private Boolean online;
    private Double latitude;
    private Double longitude;
    private Double averageRating;
    private Integer reviewCount;
    private CategoryDTO category;
    private PriceDTO price;
    private UserDTO owner;
    private List<PhotoDTO> photos;
}

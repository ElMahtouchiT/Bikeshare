package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "prices")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "high_price")   private Double highPrice;
    @Column(name = "low_price")    private Double lowPrice;
    @Column(name = "middle_price") private Double middlePrice;
    private Double promo1;
    private Double promo2;
}

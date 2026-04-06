package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "equipements")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description; // antivol, casque, panier, éclairage...
    private String icon;

    @ManyToMany(mappedBy = "equipments")
    private List<Bike> bikes;
}

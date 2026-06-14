package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Entité centrale : un vélo proposé à la location. */
@Getter
@Setter
@Entity
@Table(name = "bikes")
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private String offer;

    @Column(name = "frame_number")
    private String frameNumber;          // numéro de cadre

    @Column(name = "purchase_year")
    private LocalDate purchaseYear;

    @Column(name = "bike_type")
    private String bikeType;             // CITY, MTB, ROAD, ELECTRIC...

    @Column(name = "is_electric")
    private boolean electric;

    @Column(name = "wheel_size")
    private String wheelSize;            // 26", 28"...

    private Integer gears;

    @Column(name = "frame_size")
    private String frameSize;            // S, M, L, XL

    private String adresse;
    @Column(name = "postal_code") private String postalCode;
    private String locality;

    @Column(name = "reservation_mode")
    private String reservationMode;      // MANUAL / AUTOMATIC

    @Column(name = "registration_path")
    private String registrationPath;     // preuve d'achat / gravure

    private Boolean online;
    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;                   // propriétaire

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "price_id")
    private Price price;

    @OneToMany(mappedBy = "bike", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "bike", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Condition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "bike", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "bike_feature",
            joinColumns = @JoinColumn(name = "bike_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id"))
    private List<Feature> features = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "bike_equipment",
            joinColumns = @JoinColumn(name = "bike_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private List<Equipment> equipments = new ArrayList<>();

    @OneToMany(mappedBy = "bike", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Unavailability> unavailabilities = new ArrayList<>();

    public void addReservation(Reservation r) { reservations.add(r); r.setBike(this); }
    public void addPhoto(Photo p) { photos.add(p); p.setBike(this); }
}

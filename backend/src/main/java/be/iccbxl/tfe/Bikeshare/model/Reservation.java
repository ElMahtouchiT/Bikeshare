package be.iccbxl.tfe.Bikeshare.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")     private LocalDateTime createdAt;
    @Column(name = "start_location") private LocalDate startLocation;
    @Column(name = "end_location")   private LocalDate endLocation;
    private Integer duration;
    @Enumerated(EnumType.STRING)
    private ReservationStatus statut;
    private String assurance;

    // Demande d'annulation d'une location payée, à traiter par l'administrateur.
    @Column(columnDefinition = "boolean not null default false")
    private boolean cancellationRequested;

    @ManyToOne
    @JoinColumn(name = "bike_id", nullable = false)
    private Bike bike;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;         // locataire

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Evaluation evaluation;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Claim> claims = new ArrayList<>();

    /** Le locataire peut annuler directement une réservation non payée (en attente ou confirmée). */
    public boolean peutEtreAnnuleeDirectement() {
        return (statut == ReservationStatus.PENDING || statut == ReservationStatus.CONFIRMED) && !estPayee();
    }

    /** Une location payée (confirmée ou en cours) ne s'annule que sur demande à l'administrateur. */
    public boolean peutDemanderAnnulation() {
        return statut == ReservationStatus.CONFIRMED && estPayee() && !cancellationRequested;
    }

    /** Le propriétaire peut marquer le vélo comme rendu : location payée, commencée, pas encore terminée. */
    public boolean peutEtreMarqueeRendue(LocalDate aujourdhui) {
        return statut == ReservationStatus.CONFIRMED && estPayee()
                && endLocation != null && !endLocation.isAfter(aujourdhui);
    }

    /** Une location confirmée et payée dont la date de fin est passée est terminée. */
    public boolean doitEtreTerminee(LocalDate aujourdhui) {
        return statut == ReservationStatus.CONFIRMED && estPayee()
                && endLocation != null && endLocation.isBefore(aujourdhui);
    }

    /** Libellé affiché pour le statut de la réservation. */
    public String getLibelleStatut() {
        return statut != null ? statut.getLibelle() : "";
    }

    /** Libellé affiché pour le paiement du locataire (BikeShare). */
    public String getLibellePaiement() {
        return payment != null ? payment.getStatut().getLibelle() : "Non payée";
    }

    private boolean estPayee() {
        return payment != null && payment.getStatut() == PaymentStatus.PAID;
    }

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

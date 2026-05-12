package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "claims")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "claimant_role", nullable = false)
    private String claimantRole; // LOCATAIRE / PROPRIETAIRE

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    private String response;
    @Column(name = "response_at")
    private LocalDateTime responseAt;

    public void closeClaim() { this.status = "FINISHED"; }

    public void reopenClaim(String newMessage) {
        this.message = newMessage;
        this.status = "IN_PROGRESS";
        this.response = null;
        this.responseAt = null;
    }
}

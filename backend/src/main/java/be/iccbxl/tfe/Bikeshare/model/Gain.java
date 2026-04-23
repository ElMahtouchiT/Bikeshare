package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gains")
public class Gain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")    private LocalDateTime createdAt;
    private String description;
    @Column(name = "amount_earned") private double amountEarned;
    private String status;          // PENDING, TRANSFERRED

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

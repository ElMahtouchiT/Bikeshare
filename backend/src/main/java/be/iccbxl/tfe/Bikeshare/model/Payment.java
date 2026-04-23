package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")    private LocalDateTime createdAt;
    @Column(name = "payment_mode")  private String paymentMode;
    @Column(name = "total_price")   private double totalPrice;
    private String statut;          // PAID, REFUNDED, FAILED
    @Column(name = "part_bikeshare") private double partBikeshare; // commission

    @OneToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    private Refund refund;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    private Gain gain;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

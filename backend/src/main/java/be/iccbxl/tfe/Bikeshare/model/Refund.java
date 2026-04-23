package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "refunds")
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    @Column(name = "created_at")        private LocalDateTime createdAt;
    @Column(name = "refund_percentage") private double refundPercentage;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;
}

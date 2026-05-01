package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String message;

    @Column(nullable = false)
    private String type;

    @Column(name = "is_read")
    private boolean read;

    @ManyToOne @JoinColumn(name = "bike_id", nullable = false)
    private Bike bike;

    @ManyToOne @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;
}

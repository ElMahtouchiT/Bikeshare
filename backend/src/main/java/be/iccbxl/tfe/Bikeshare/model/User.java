package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_name")
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @Column(name = "first_name")
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @Column(name = "email", unique = true)
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "locality")
    private String locality;

    @Column(name = "postal_code")
    @Pattern(regexp = "^\\d+$", message = "Le code postal ne doit contenir que des chiffres")
    private String postalCode;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password")
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
             message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
    private String password;

    @Column(name = "profil_picture")
    private String photoUrl;

    @Column(name = "iban")
    private String iban;

    @Column(name = "bic")
    private String bic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private boolean isVerified;

    @Column(name = "delete_requested", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleteRequested;

    /** Réinitialisation du mot de passe : token à usage unique + date d'expiration. */
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    /** Vélos proposés par cet utilisateur (propriétaire). */
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bike> ownedBikes;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notificationsSent;

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notificationsReceived;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

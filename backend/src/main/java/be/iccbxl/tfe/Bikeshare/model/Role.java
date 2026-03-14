package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role; // ROLE_VISITOR, ROLE_MEMBER, ROLE_ADMIN

    @ManyToMany(mappedBy = "roles")
    private List<User> users;
}

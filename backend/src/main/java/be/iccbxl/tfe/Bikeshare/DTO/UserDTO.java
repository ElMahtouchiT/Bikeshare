package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String adresse;
    private String locality;
    private String postalCode;
    private String phone;
    private String photoUrl;
    private String iban;
    private String bic;
    private LocalDateTime createdAt;
    private boolean isVerified;
    private List<RoleDTO> roles;

    public UserDTO() {}

    public UserDTO(Long id, String firstName, String lastName, String photoUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
    }
}

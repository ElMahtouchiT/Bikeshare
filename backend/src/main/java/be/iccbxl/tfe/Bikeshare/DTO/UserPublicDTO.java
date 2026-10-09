package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;

/** Données d'un membre visibles publiquement : propriétaire d'un vélo ou locataire d'une réservation. */
@Data
public class UserPublicDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private boolean isVerified;
}

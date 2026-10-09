package be.iccbxl.tfe.Bikeshare.DTO;

import static org.assertj.core.api.Assertions.assertThat;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class MapperDTOTest {

    @Test
    void lesCoordonneesBancairesDuProprietaireNeSortentPasDansLeJson() throws Exception {
        User proprietaire = new User();
        proprietaire.setId(1L);
        proprietaire.setFirstName("Test");
        proprietaire.setLastName("Proprietaire");
        proprietaire.setEmail("test@example.com");
        proprietaire.setIban("BE00000000000000");
        proprietaire.setBic("TESTBEBB");

        Bike velo = new Bike();
        velo.setId(10L);
        velo.setUser(proprietaire);

        String json = new ObjectMapper().writeValueAsString(MapperDTO.toBikeDTO(velo));

        assertThat(json).contains("\"owner\"");
        assertThat(json).doesNotContain("iban").doesNotContain("bic");
        assertThat(json).doesNotContain("BE00000000000000").doesNotContain("TESTBEBB");
    }
}

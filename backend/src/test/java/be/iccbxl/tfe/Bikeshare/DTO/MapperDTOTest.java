package be.iccbxl.tfe.Bikeshare.DTO;

import static org.assertj.core.api.Assertions.assertThat;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class MapperDTOTest {

    @Test
    void lesCoordonneesDuProprietaireNeSortentPasDansLeJson() throws Exception {
        User proprietaire = new User();
        proprietaire.setId(1L);
        proprietaire.setFirstName("Test");
        proprietaire.setLastName("Proprietaire");
        proprietaire.setEmail("proprio@exemple.test");
        proprietaire.setPhone("0470123456");
        proprietaire.setAdresse("Rue Secrete 12");
        proprietaire.setPostalCode("1000");
        proprietaire.setIban("BE00000000000000");
        proprietaire.setBic("TESTBEBB");

        Bike velo = new Bike();
        velo.setId(10L);
        velo.setUser(proprietaire);

        String json = new ObjectMapper().writeValueAsString(MapperDTO.toBikeDTO(velo));

        assertThat(json).contains("\"owner\"").contains("\"firstName\":\"Test\"");
        assertThat(json).doesNotContain("iban").doesNotContain("bic").doesNotContain("email").doesNotContain("phone");
        assertThat(json).doesNotContain("proprio@exemple.test").doesNotContain("0470123456");
        assertThat(json).doesNotContain("Rue Secrete 12").doesNotContain("BE00000000000000").doesNotContain("TESTBEBB");
    }
}

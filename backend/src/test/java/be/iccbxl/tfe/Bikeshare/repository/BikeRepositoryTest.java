package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration de la couche d'accès aux données (Spring Data JPA + base H2 en mémoire).
 * Vérifie la requête de recherche du catalogue, notamment la recherche par code postal.
 */
@DataJpaTest
class BikeRepositoryTest {

    @Autowired
    private BikeRepository bikeRepository;

    private Bike bike(String brand, String locality, String postalCode, boolean online) {
        Bike b = new Bike();
        b.setBrand(brand);
        b.setModel("M");
        b.setLocality(locality);
        b.setPostalCode(postalCode);
        b.setOnline(online);
        return b;
    }

    @BeforeEach
    void setUp() {
        bikeRepository.save(bike("Trek",   "Bruxelles", "1000", true));
        bikeRepository.save(bike("Btwin",  "Jette",     "1090", true));
        bikeRepository.save(bike("Cache",  "Bruxelles", "1000", false)); // hors ligne
    }

    @Test
    void search_parLocalite() {
        List<Bike> result = bikeRepository.search("Jette", null, null);
        assertThat(result).extracting(Bike::getBrand).containsExactly("Btwin");
    }

    @Test
    void search_parCodePostal() {
        List<Bike> result = bikeRepository.search("1090", null, null);
        assertThat(result).extracting(Bike::getBrand).containsExactly("Btwin");
    }

    @Test
    void search_ignoreLesVelosHorsLigne() {
        List<Bike> result = bikeRepository.search("1000", null, null);
        assertThat(result).extracting(Bike::getBrand).containsExactly("Trek");
    }

    @Test
    void search_sansFiltre_retourneTousLesVelosEnLigne() {
        List<Bike> result = bikeRepository.search(null, null, null);
        assertThat(result).hasSize(2);
    }
}

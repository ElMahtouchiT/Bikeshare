package be.iccbxl.tfe.Bikeshare.config;

import be.iccbxl.tfe.Bikeshare.model.Equipment;
import be.iccbxl.tfe.Bikeshare.repository.EquipmentRepository;
import be.iccbxl.tfe.Bikeshare.repository.RoleRepository;
import be.iccbxl.tfe.Bikeshare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerEquipementsTest {

    @Mock private RoleRepository roleRepository;
    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private EquipmentRepository equipmentRepository;

    private DataInitializer initialiseur() {
        return new DataInitializer(roleRepository, userRepository, passwordEncoder, equipmentRepository);
    }

    @Test
    void catalogueVide_estInitialiseAvecLesEquipementsCourants() {
        when(equipmentRepository.count()).thenReturn(0L);

        initialiseur().run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Equipment>> captor = ArgumentCaptor.forClass(List.class);
        verify(equipmentRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(8);
        assertThat(captor.getValue()).extracting(Equipment::getDescription).contains("Antivol", "Casque");
    }

    @Test
    void catalogueDejaRempli_nEstPasModifie() {
        when(equipmentRepository.count()).thenReturn(3L);

        initialiseur().run();

        verify(equipmentRepository, never()).saveAll(any());
    }
}

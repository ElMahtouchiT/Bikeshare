package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.repository.GainRepository;
import be.iccbxl.tfe.Bikeshare.repository.PaymentRepository;
import be.iccbxl.tfe.Bikeshare.repository.RefundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private GainRepository gainRepository;
    @Mock private RefundRepository refundRepository;
    @InjectMocks private FinanceService financeService;

    @Test
    void csv_echappeLesSeparateursEtLesGuillemets() {
        assertThat(FinanceService.ligneCsv("a;b", "c\"d", null, 12))
                .isEqualTo("\"a;b\";\"c\"\"d\";;12");
    }

    @Test
    void marquerVerse_passeEnVerseSeulementSiLeGainEstEnAttente() {
        Gain gain = new Gain();
        gain.setId(5L);
        gain.setStatus("PENDING");
        when(gainRepository.findById(5L)).thenReturn(Optional.of(gain));

        assertThat(financeService.marquerVerse(5L)).isTrue();
        assertThat(gain.getStatus()).isEqualTo("TRANSFERRED");
        assertThat(financeService.marquerVerse(5L)).isFalse();
    }

    @Test
    void montantDu_neCompteQueLesGainsNonVerses() {
        Gain enAttente = new Gain();
        enAttente.setStatus("PENDING");
        enAttente.setAmountEarned(34.0);
        Gain verse = new Gain();
        verse.setStatus("TRANSFERRED");
        verse.setAmountEarned(50.0);
        when(gainRepository.findAll()).thenReturn(List.of(enAttente, verse));

        assertThat(financeService.getMontantDu()).isEqualTo(34.0);
    }
}

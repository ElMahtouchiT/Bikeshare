package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Refund;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.repository.GainRepository;
import be.iccbxl.tfe.Bikeshare.repository.PaymentRepository;
import be.iccbxl.tfe.Bikeshare.repository.RefundRepository;
import be.iccbxl.tfe.Bikeshare.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceAnnulationTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private GainRepository gainRepository;
    @Mock private RefundRepository refundRepository;
    @InjectMocks private FinanceService financeService;

    /** Location payée de 40 € (commission 6 €), gain du propriétaire de 34 € en attente. */
    private Reservation locationPayee(Payment[] pOut, Gain[] gOut) {
        Payment p = new Payment();
        p.setStatut("PAID");
        p.setTotalPrice(40.0);
        p.setPartBikeshare(6.0);
        Gain g = new Gain();
        g.setStatus("PENDING");
        g.setAmountEarned(34.0);
        g.setPayment(p);
        p.setGain(g);
        Reservation r = new Reservation();
        r.setStatut("CONFIRMED");
        r.setPayment(p);
        p.setReservation(r);
        pOut[0] = p;
        gOut[0] = g;
        return r;
    }

    @Test
    void remboursementIntegral_annuleLaLocationLePaiementEtLeGain() {
        Payment[] p = new Payment[1];
        Gain[] g = new Gain[1];
        Reservation r = locationPayee(p, g);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThat(financeService.annulerAvecRemboursement(1L, 100)).isTrue();

        assertThat(r.getStatut()).isEqualTo("CANCELLED");
        assertThat(p[0].getStatut()).isEqualTo("REFUNDED");
        assertThat(g[0].getStatus()).isEqualTo("ANNULE");
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    void sansRemboursement_gardeLePaiementEtLeGainDuProprietaire() {
        Payment[] p = new Payment[1];
        Gain[] g = new Gain[1];
        Reservation r = locationPayee(p, g);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThat(financeService.annulerAvecRemboursement(1L, 0)).isTrue();

        assertThat(r.getStatut()).isEqualTo("CANCELLED");
        assertThat(p[0].getStatut()).isEqualTo("PAID");
        assertThat(g[0].getStatus()).isEqualTo("PENDING");
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void reservationNonPayee_nePeutPasEtreAnnuleeParLAdmin() {
        Reservation r = new Reservation();
        r.setStatut("CONFIRMED");
        when(reservationRepository.findById(2L)).thenReturn(Optional.of(r));

        assertThat(financeService.annulerAvecRemboursement(2L, 100)).isFalse();
        assertThat(r.getStatut()).isEqualTo("CONFIRMED");
    }

    @Test
    void pourcentageIntermediaire_nEstPasAutorise() {
        assertThat(financeService.annulerAvecRemboursement(1L, 50)).isFalse();
    }
}

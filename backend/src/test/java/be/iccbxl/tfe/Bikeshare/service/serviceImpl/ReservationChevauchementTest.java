package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Une demande en attente bloque les dates, et l'acceptation ignore la réservation elle-même. */
@ExtendWith(MockitoExtension.class)
class ReservationChevauchementTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation reservation(Long id, LocalDate debut, LocalDate fin) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setStartLocation(debut);
        r.setEndLocation(fin);
        return r;
    }

    @Test
    void uneDemandeEnAttenteBloqueLesDates() {
        Reservation attente = reservation(1L, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 16));
        when(reservationRepository.findByBikeIdAndStatutIn(anyLong(), anyList())).thenReturn(List.of(attente));

        assertThat(reservationService.hasBookingOverlap(10L, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11))).isTrue();
        verify(reservationRepository).findByBikeIdAndStatutIn(eq(10L), eq(List.of("PENDING", "CONFIRMED", "NOW")));
    }

    @Test
    void laReservationAcceptéeNeSeCompareQuaAuxAutres() {
        Reservation demande = reservation(2L, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 16));
        when(reservationRepository.findByBikeIdAndStatutIn(anyLong(), anyList())).thenReturn(List.of(demande));

        assertThat(reservationService.hasOverlapWithOthers(10L, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11), 2L)).isFalse();
        assertThat(reservationService.hasOverlapWithOthers(10L, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11), 3L)).isTrue();
    }

    @Test
    void desDatesSansChevauchementNeBloquentRien() {
        Reservation autre = reservation(4L, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 25));
        when(reservationRepository.findByBikeIdAndStatutIn(anyLong(), anyList())).thenReturn(List.of(autre));

        assertThat(reservationService.hasBookingOverlap(10L, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11))).isFalse();
    }
}

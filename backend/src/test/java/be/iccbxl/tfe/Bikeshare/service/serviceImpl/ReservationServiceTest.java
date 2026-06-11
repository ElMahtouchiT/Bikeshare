package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de la logique de réservation (anti-chevauchement),
 * avec un dépôt simulé par Mockito (aucune base de données).
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation reservation(LocalDate start, LocalDate end) {
        Reservation r = new Reservation();
        r.setStartLocation(start);
        r.setEndLocation(end);
        return r;
    }

    @Test
    void hasBookingOverlap_renvoieVrai_quandLesPeriodesSeChevauchent() {
        when(reservationRepository.findByBikeIdAndStatutIn(anyLong(), any()))
                .thenReturn(List.of(reservation(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5))));

        boolean overlap = reservationService.hasBookingOverlap(
                1L, LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7));

        assertThat(overlap).isTrue();
    }

    @Test
    void hasBookingOverlap_renvoieFaux_quandLesPeriodesNeSeChevauchentPas() {
        when(reservationRepository.findByBikeIdAndStatutIn(anyLong(), any()))
                .thenReturn(List.of(reservation(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5))));

        boolean overlap = reservationService.hasBookingOverlap(
                1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        assertThat(overlap).isFalse();
    }

    @Test
    void hasBookingOverlap_renvoieFaux_quandAucuneReservation() {
        when(reservationRepository.findByBikeIdAndStatutIn(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(reservationService.hasBookingOverlap(
                1L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5))).isFalse();
    }

    @Test
    void hasBookingOverlap_renvoieFaux_quandDatesNulles() {
        assertThat(reservationService.hasBookingOverlap(1L, null, null)).isFalse();
    }
}

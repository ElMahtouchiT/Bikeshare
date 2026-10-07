package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
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

    /* ─── Participation : locataire ou propriétaire uniquement (annulation, chat) ─── */

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    /** Réservation d'un vélo appartenant à {@code owner}, louée par {@code renter}. */
    private Reservation reservationBetween(User renter, User owner) {
        Bike bike = new Bike();
        bike.setUser(owner);
        Reservation r = new Reservation();
        r.setUser(renter);
        r.setBike(bike);
        return r;
    }

    @Test
    void isParticipant_vrai_pourLeLocataire() {
        assertThat(reservationService.isParticipant(reservationBetween(user(1L), user(2L)), user(1L))).isTrue();
    }

    @Test
    void isParticipant_vrai_pourLePropriétaireDuVelo() {
        assertThat(reservationService.isParticipant(reservationBetween(user(1L), user(2L)), user(2L))).isTrue();
    }

    @Test
    void isParticipant_faux_pourUnTiers() {
        assertThat(reservationService.isParticipant(reservationBetween(user(1L), user(2L)), user(3L))).isFalse();
    }

    @Test
    void isParticipant_faux_siReservationOuUtilisateurAbsent() {
        assertThat(reservationService.isParticipant(null, user(1L))).isFalse();
        assertThat(reservationService.isParticipant(reservationBetween(user(1L), user(2L)), null)).isFalse();
    }

    /* ─── Paiement : seule une réservation confirmée et non payée peut être payée ─── */

    private Reservation reservationAvecStatut(String statut) {
        Reservation r = new Reservation();
        r.setStatut(statut);
        return r;
    }

    @Test
    void isPayable_vrai_pourUneReservationConfirmeeNonPayee() {
        assertThat(reservationService.isPayable(reservationAvecStatut("CONFIRMED"))).isTrue();
    }

    @Test
    void isPayable_faux_pourUneReservationEnAttenteRefuseeOuAnnulee() {
        assertThat(reservationService.isPayable(reservationAvecStatut("PENDING"))).isFalse();
        assertThat(reservationService.isPayable(reservationAvecStatut("REFUSED"))).isFalse();
        assertThat(reservationService.isPayable(reservationAvecStatut("CANCELLED"))).isFalse();
    }

    @Test
    void isPayable_faux_pourUneReservationDejaPayee() {
        Reservation r = reservationAvecStatut("CONFIRMED");
        r.setPayment(new Payment());
        assertThat(reservationService.isPayable(r)).isFalse();
    }
}

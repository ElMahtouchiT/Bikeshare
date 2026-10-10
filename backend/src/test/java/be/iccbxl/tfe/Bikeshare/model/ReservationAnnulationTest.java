package be.iccbxl.tfe.Bikeshare.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Qui peut annuler quoi : annulation directe si non payée, demande à l'administrateur si payée. */
class ReservationAnnulationTest {

    private static Reservation reservation(String statut, String statutPaiement) {
        Reservation r = new Reservation();
        r.setStatut(ReservationStatus.valueOf(statut));
        r.setStartLocation(LocalDate.of(2026, 10, 10));
        r.setEndLocation(LocalDate.of(2026, 10, 12));
        if (statutPaiement != null) {
            Payment p = new Payment();
            p.setStatut(PaymentStatus.valueOf(statutPaiement));
            r.setPayment(p);
        }
        return r;
    }

    @Test
    void nonPayee_sAnnuleDirectement() {
        assertThat(reservation("PENDING", null).peutEtreAnnuleeDirectement()).isTrue();
        assertThat(reservation("CONFIRMED", null).peutEtreAnnuleeDirectement()).isTrue();
        assertThat(reservation("PENDING", null).peutDemanderAnnulation()).isFalse();
    }

    @Test
    void payee_nePeutPasAnnulerDirectement_maisPeutDemanderAnnulation() {
        assertThat(reservation("CONFIRMED", "PAID").peutEtreAnnuleeDirectement()).isFalse();
        assertThat(reservation("CONFIRMED", "PAID").peutDemanderAnnulation()).isTrue();
        assertThat(reservation("CONFIRMED", "PAID").peutDemanderAnnulation()).isTrue();
    }

    @Test
    void demandeDejaEnvoyee_ouTerminee_nePeutPasRedemander() {
        Reservation demandee = reservation("CONFIRMED", "PAID");
        demandee.setCancellationRequested(true);
        assertThat(demandee.peutDemanderAnnulation()).isFalse();
        assertThat(reservation("COMPLETED", "PAID").peutDemanderAnnulation()).isFalse();
    }
}

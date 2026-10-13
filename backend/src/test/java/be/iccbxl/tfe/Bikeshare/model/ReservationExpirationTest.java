package be.iccbxl.tfe.Bikeshare.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Expiration d'une réservation confirmée non payée après le délai de paiement. */
class ReservationExpirationTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 10, 13, 12, 0);

    private static Reservation confirmee(LocalDateTime confirmeeLe, PaymentStatus statutPaiement) {
        Reservation r = new Reservation();
        r.confirmer(confirmeeLe);
        if (statutPaiement != null) {
            Payment p = new Payment();
            p.setStatut(statutPaiement);
            r.setPayment(p);
        }
        return r;
    }

    @Test
    void nonPayee_apresLeDelai_doitExpirer() {
        assertThat(confirmee(MAINTENANT.minusHours(25), null).doitExpirerFauteDePaiement(MAINTENANT)).isTrue();
    }

    @Test
    void nonPayee_avantLeDelai_nExpirePas() {
        assertThat(confirmee(MAINTENANT.minusHours(23), null).doitExpirerFauteDePaiement(MAINTENANT)).isFalse();
    }

    @Test
    void payee_nExpirePasMemeApresLeDelai() {
        assertThat(confirmee(MAINTENANT.minusHours(48), PaymentStatus.PAID).doitExpirerFauteDePaiement(MAINTENANT)).isFalse();
    }

    @Test
    void sansDateDeConfirmation_nExpirePasPas() {
        // réservations antérieures au déploiement : on ne les annule pas automatiquement
        Reservation ancienne = new Reservation();
        ancienne.setStatut(ReservationStatus.CONFIRMED);
        assertThat(ancienne.doitExpirerFauteDePaiement(MAINTENANT)).isFalse();
    }

    @Test
    void enAttenteDeLAccord_nExpirePas() {
        Reservation r = new Reservation();
        r.setStatut(ReservationStatus.PENDING);
        assertThat(r.doitExpirerFauteDePaiement(MAINTENANT)).isFalse();
    }
}

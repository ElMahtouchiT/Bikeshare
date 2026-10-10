package be.iccbxl.tfe.Bikeshare.model;

import be.iccbxl.tfe.Bikeshare.model.PaymentStatus;
import be.iccbxl.tfe.Bikeshare.model.ReservationStatus;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Règles de fin de location : marquage « rendu » par le propriétaire, et fin automatique. */
class ReservationRulesTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 8);

    private static Reservation reservation(String statut, String statutPaiement,
                                           LocalDate debut, LocalDate fin) {
        Reservation r = new Reservation();
        r.setStatut(ReservationStatus.valueOf(statut));
        r.setStartLocation(debut);
        r.setEndLocation(fin);
        if (statutPaiement != null) {
            Payment p = new Payment();
            p.setStatut(PaymentStatus.valueOf(statutPaiement));
            r.setPayment(p);
        }
        return r;
    }

    @Test
    void peutEtreMarqueeRendue_seulementSiPayeeEtTerminee() {
        // période encore en cours : pas encore rendue
        assertThat(reservation("CONFIRMED", "PAID", AUJOURDHUI.minusDays(1), AUJOURDHUI.plusDays(2))
                .peutEtreMarqueeRendue(AUJOURDHUI)).isFalse();
        assertThat(reservation("CONFIRMED", "PAID", AUJOURDHUI, AUJOURDHUI.plusDays(2))
                .peutEtreMarqueeRendue(AUJOURDHUI)).isFalse();
        // terminée le jour même ou avant : rendue possible
        assertThat(reservation("CONFIRMED", "PAID", AUJOURDHUI.minusDays(3), AUJOURDHUI)
                .peutEtreMarqueeRendue(AUJOURDHUI)).isTrue();
        assertThat(reservation("CONFIRMED", "PAID", AUJOURDHUI.minusDays(3), AUJOURDHUI.minusDays(1))
                .peutEtreMarqueeRendue(AUJOURDHUI)).isTrue();
        // non payée, pas encore commencée, ou déjà terminée
        assertThat(reservation("CONFIRMED", null, AUJOURDHUI.minusDays(1), AUJOURDHUI.minusDays(1))
                .peutEtreMarqueeRendue(AUJOURDHUI)).isFalse();
        assertThat(reservation("CONFIRMED", "PAID", AUJOURDHUI.plusDays(1), AUJOURDHUI.plusDays(3))
                .peutEtreMarqueeRendue(AUJOURDHUI)).isFalse();
        assertThat(reservation("COMPLETED", "PAID", AUJOURDHUI.minusDays(3), AUJOURDHUI.minusDays(1))
                .peutEtreMarqueeRendue(AUJOURDHUI)).isFalse();
    }

    @Test
    void doitEtreTerminee_seulementSiPayeeEtFinieAvantAujourdhui() {
        assertThat(reservation("CONFIRMED", "PAID", AUJOURDHUI.minusDays(3), AUJOURDHUI.minusDays(1))
                .doitEtreTerminee(AUJOURDHUI)).isTrue();
        assertThat(reservation("CONFIRMED", "PAID", AUJOURDHUI.minusDays(3), AUJOURDHUI)
                .doitEtreTerminee(AUJOURDHUI)).isFalse();
        assertThat(reservation("CONFIRMED", null, AUJOURDHUI.minusDays(3), AUJOURDHUI.minusDays(1))
                .doitEtreTerminee(AUJOURDHUI)).isFalse();
        assertThat(reservation("PENDING", "PAID", AUJOURDHUI.minusDays(3), AUJOURDHUI.minusDays(1))
                .doitEtreTerminee(AUJOURDHUI)).isFalse();
    }
}

package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Notification;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Suppression d'un vélo : physique s'il n'a aucun historique, sinon retiré de la location. */
@DataJpaTest
@Import(BikeService.class)
class SuppressionVeloTest {

    @Autowired private BikeRepository bikeRepository;
    @Autowired private BikeService bikeService;
    @Autowired private TestEntityManager em;

    private User membre(String email) {
        User u = new User();
        u.setEmail(email);
        u.setFirstName("Test");
        u.setLastName("Membre");
        u.setPassword("motdepasse1");
        u.setAdresse("Rue du Test 1");
        u.setLocality("Bruxelles");
        u.setPostalCode("1000");
        em.persist(u);
        return u;
    }

    private Bike velo(User proprietaire) {
        Bike b = new Bike();
        b.setUser(proprietaire);
        b.setBrand("Btwin");
        b.setOnline(true);
        em.persist(b);
        return b;
    }

    private Reservation reservation(Bike bike, User locataire, String statut) {
        Reservation r = new Reservation();
        r.setUser(locataire);
        r.setBike(bike);
        r.setStartLocation(LocalDate.of(2026, 7, 1));
        r.setEndLocation(LocalDate.of(2026, 7, 5));
        r.setDuration(4);
        r.setStatut(statut);
        em.persist(r);
        return r;
    }

    private void paiement(Reservation r) {
        Payment p = new Payment();
        p.setReservation(r);
        p.setStatut("PAID");
        p.setPaymentMode("STRIPE");
        p.setTotalPrice(40.0);
        p.setPartBikeshare(6.0);
        em.persist(p);

        Gain g = new Gain();
        g.setPayment(p);
        g.setAmountEarned(34.0);
        g.setStatus("PENDING");
        em.persist(g);
    }

    private void notification(Bike bike, User from, User to) {
        Notification n = new Notification();
        n.setType("RESERVATION");
        n.setMessage("Votre réservation a été confirmée");
        n.setBike(bike);
        n.setFromUser(from);
        n.setToUser(to);
        em.persist(n);
    }

    private long count(String jpql) {
        return em.getEntityManager().createQuery(jpql, Long.class).getSingleResult();
    }

    @Test
    void veloSansHistorique_estSupprime() {
        User proprio = membre("sans.histoire@test.be");
        Bike b = velo(proprio);
        em.flush(); em.clear();

        boolean supprime = bikeService.deleteBike(b.getId());
        em.flush();

        assertThat(supprime).isTrue();
        assertThat(bikeRepository.count()).isZero();
    }

    @Test
    void veloAvecReservationEtNotification_estRetireDeLaLocation() {
        User proprio = membre("proprio.notif@test.be");
        User loc = membre("loc.notif@test.be");
        Bike b = velo(proprio);
        reservation(b, loc, "COMPLETED");
        notification(b, proprio, loc);
        em.flush(); em.clear();

        boolean supprime = bikeService.deleteBike(b.getId());
        em.flush(); em.clear();

        assertThat(supprime).isFalse();
        Bike apres = em.find(Bike.class, b.getId());
        assertThat(apres).isNotNull();
        assertThat(apres.getOnline()).isFalse();
        assertThat(apres.isArchived()).isTrue();
        assertThat(count("select count(r) from Reservation r")).isEqualTo(1);
        assertThat(count("select count(n) from Notification n")).isEqualTo(1);
    }

    @Test
    void veloAvecReservationPayeeSansNotification_gardeLesPaiementsEtGains() {
        User proprio = membre("proprio.paye@test.be");
        User loc = membre("loc.paye@test.be");
        Bike b = velo(proprio);
        paiement(reservation(b, loc, "COMPLETED"));
        em.flush(); em.clear();

        boolean supprime = bikeService.deleteBike(b.getId());
        em.flush(); em.clear();

        assertThat(supprime).isFalse();
        assertThat(em.find(Bike.class, b.getId())).isNotNull();
        assertThat(count("select count(p) from Payment p")).isEqualTo(1);
        assertThat(count("select count(g) from Gain g")).isEqualTo(1);
    }
}

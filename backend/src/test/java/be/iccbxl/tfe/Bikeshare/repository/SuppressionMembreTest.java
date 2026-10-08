package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.ChatMessage;
import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Notification;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.Role;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Suppression d'un membre : physique s'il n'a aucune trace, sinon anonymisé et désactivé. */
@DataJpaTest
@Import({UserService.class, SuppressionMembreTest.Encodeur.class})
class SuppressionMembreTest {

    @TestConfiguration
    static class Encodeur {
        @Bean BCryptPasswordEncoder bCryptPasswordEncoder() { return new BCryptPasswordEncoder(); }
    }

    @Autowired private UserService userService;
    @Autowired private TestEntityManager em;

    private Role role() {
        Role r = new Role();
        r.setRole("ROLE_MEMBER");
        em.persist(r);
        return r;
    }

    private User membre(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setFirstName("Jean");
        u.setLastName("Dupont");
        u.setPassword("motdepasse1");
        u.setPhone("0470000000");
        u.setAdresse("Rue du Test 1");
        u.setLocality("Bruxelles");
        u.setPostalCode("1000");
        u.setIban("BE68539007547034");
        u.setVerified(true);
        u.getRoles().add(role);
        em.persist(u);
        return u;
    }

    private Bike velo(User proprietaire) {
        Bike b = new Bike();
        b.setUser(proprietaire);
        b.setBrand("Btwin");
        b.setOnline(true);
        b.setAdresse("Rue du Test 1");
        b.setLatitude(50.85);
        b.setLongitude(4.35);
        em.persist(b);
        return b;
    }

    private Reservation reservation(Bike bike, User locataire) {
        Reservation r = new Reservation();
        r.setUser(locataire);
        r.setBike(bike);
        r.setStartLocation(LocalDate.of(2026, 7, 1));
        r.setEndLocation(LocalDate.of(2026, 7, 5));
        r.setDuration(4);
        r.setStatut("COMPLETED");
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

    private Notification notification(Bike bike, User from, User to) {
        Notification n = new Notification();
        n.setType("RESERVATION");
        n.setMessage("Votre réservation a été confirmée");
        n.setBike(bike);
        n.setFromUser(from);
        n.setToUser(to);
        em.persist(n);
        return n;
    }

    private long count(String jpql) {
        return em.getEntityManager().createQuery(jpql, Long.class).getSingleResult();
    }

    @Test
    void membreSansTrace_estSupprime_etLeRoleResteEnBase() {
        Role role = role();
        User u = membre("vide@test.be", role);
        em.flush(); em.clear();

        boolean supprime = userService.deleteUser(u.getId());
        em.flush();

        assertThat(supprime).isTrue();
        assertThat(count("select count(u) from User u")).isZero();
        assertThat(count("select count(r) from Role r")).isEqualTo(1);
    }

    @Test
    void proprietaireAvecHistorique_estAnonymise_etSonHistoriqueEstConserve() {
        Role role = role();
        User proprio = membre("proprio@test.be", role);
        User loc = membre("loc@test.be", role);
        Bike b = velo(proprio);
        paiement(reservation(b, loc));
        notification(b, proprio, loc);
        em.flush(); em.clear();

        boolean supprime = userService.deleteUser(proprio.getId());
        em.flush(); em.clear();

        assertThat(supprime).isFalse();
        User apres = em.find(User.class, proprio.getId());
        assertThat(apres).isNotNull();
        assertThat(apres.getEmail()).isEqualTo("anonyme-" + apres.getId() + "@supprime.invalid");
        assertThat(apres.getFirstName()).isEqualTo("Ancien");
        assertThat(apres.getPhone()).isNull();
        assertThat(apres.getIban()).isNull();
        // Ces colonnes sont NOT NULL dans la base : une valeur neutre, jamais null
        assertThat(apres.getAdresse()).isNotBlank();
        assertThat(apres.getLocality()).isNotBlank();
        assertThat(apres.getPostalCode()).isNotBlank();
        assertThat(apres.getRoles()).isEmpty();
        assertThat(em.find(Bike.class, b.getId()).getOnline()).isFalse();
        assertThat(em.find(Bike.class, b.getId()).getAdresse()).isNull();
        assertThat(count("select count(r) from Reservation r")).isEqualTo(1);
        assertThat(count("select count(p) from Payment p")).isEqualTo(1);
        assertThat(count("select count(g) from Gain g")).isEqualTo(1);
        assertThat(count("select count(n) from Notification n")).isEqualTo(1);
    }

    @Test
    void locataireAvecReservation_estAnonymise_sansPerdreLaReservation() {
        Role role = role();
        User proprio = membre("proprio2@test.be", role);
        User loc = membre("loc2@test.be", role);
        Bike b = velo(proprio);
        Reservation r = reservation(b, loc);
        em.flush(); em.clear();

        boolean supprime = userService.deleteUser(loc.getId());
        em.flush(); em.clear();

        assertThat(supprime).isFalse();
        assertThat(count("select count(r) from Reservation r")).isEqualTo(1);
        assertThat(em.find(Reservation.class, r.getId()).getUser().getEmail())
                .startsWith("anonyme-");
        assertThat(em.find(User.class, proprio.getId()).getEmail()).isEqualTo("proprio2@test.be");
    }

    @Test
    void anonymisation_effaceMessagesEtNotifications_etArchiveLesVelos() {
        Role role = role();
        User proprio = membre("proprio3@test.be", role);
        User loc = membre("loc3@test.be", role);
        Bike b = velo(proprio);
        Reservation r = reservation(b, loc);
        ChatMessage message = new ChatMessage();
        message.setReservation(r);
        message.setContent("Mon numéro est le 0470000000");
        message.setSentAt(LocalDateTime.now());
        message.setFromUserId(proprio.getId());
        message.setToUserId(loc.getId());
        em.persist(message);
        Notification n = notification(b, proprio, loc);
        em.flush(); em.clear();

        boolean supprime = userService.deleteUser(proprio.getId());
        em.flush(); em.clear();

        assertThat(supprime).isFalse();
        assertThat(em.find(ChatMessage.class, message.getId()).getContent()).isEqualTo("[supprimé]");
        assertThat(em.find(Notification.class, n.getId()).getMessage()).isEqualTo("[supprimé]");
        Bike apres = em.find(Bike.class, b.getId());
        assertThat(apres.getOnline()).isFalse();
        assertThat(apres.isArchived()).isTrue();
    }
}

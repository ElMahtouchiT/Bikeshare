package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.ReservationStatus;
import be.iccbxl.tfe.Bikeshare.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** Annule les réservations confirmées restées impayées après le délai de paiement, pour libérer les dates. */
@Component
public class ExpirationReservationsJob {

    private static final Logger logger = LoggerFactory.getLogger(ExpirationReservationsJob.class);
    private static final ZoneId FUSEAU = ZoneId.of("Europe/Brussels");

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private NotificationService notificationService;

    /** Toutes les 15 minutes, avec une première exécution une minute après le démarrage. */
    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT15M")
    @Transactional
    public void annulerReservationsNonPayees() {
        LocalDateTime maintenant = LocalDateTime.now(FUSEAU);
        for (Reservation r : reservationRepository.findByStatut(ReservationStatus.CONFIRMED)) {
            if (r.doitExpirerFauteDePaiement(maintenant)) {
                r.setStatut(ReservationStatus.CANCELLED);
                reservationRepository.save(r);
                notifier(r);
                logger.info("Réservation {} annulée : paiement non reçu dans le délai", r.getId());
            }
        }
    }

    private void notifier(Reservation r) {
        try {
            notificationService.notify(r.getUser(), r.getBike().getUser(), r.getBike(), "RESERVATION",
                    "Votre réservation a été annulée : le paiement n'a pas été reçu dans les "
                            + Reservation.DELAI_PAIEMENT_HEURES + " heures.",
                    "/account/reservations");
            notificationService.notify(r.getBike().getUser(), r.getUser(), r.getBike(), "RESERVATION",
                    "Une réservation de votre vélo a été annulée faute de paiement. Les dates sont à nouveau disponibles.",
                    "/account/received-reservations");
        } catch (Exception e) {
            logger.warn("Notification d'expiration non créée : {}", e.getMessage());
        }
    }
}

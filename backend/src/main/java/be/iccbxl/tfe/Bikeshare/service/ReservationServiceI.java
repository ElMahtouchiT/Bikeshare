package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import java.time.LocalDate;
import java.util.List;

public interface ReservationServiceI {
    List<Reservation> getAllReservations();
    List<ReservationDTO> getAllReservationsDTOs();
    Reservation getReservationById(Long id);
    Reservation addReservation(Reservation reservation);
    Reservation saveReservation(Reservation reservation);
    Reservation updateReservation(Long id, Reservation reservation);
    void deleteReservation(Long id);
    List<ReservationDTO> getReservationsByUser(User user);
    List<Reservation> getReservationsByStatusesAndUser(List<String> statuses, User user);
    long getTotalConfirmedReservations();

    /** Vrai si le vélo a au moins une réservation en cours (PENDING, CONFIRMED ou NOW). */
    boolean hasActiveReservations(Long bikeId);

    /** Réservations reçues : faites sur les vélos appartenant à ce propriétaire. */
    List<Reservation> getReservationsOnOwnerBikes(User owner);

    /** Réservations qui bloquent le calendrier d'un vélo (CONFIRMED, NOW). */
    List<Reservation> getBookedReservationsForBike(Long bikeId);

    /** Vrai si la période [start, end] chevauche une réservation confirmée du vélo. */
    boolean hasBookingOverlap(Long bikeId, LocalDate start, LocalDate end);

    /** Crée une réservation (durée + statut initial) et l'enregistre. Règles partagées par le site et l'API. */
    Reservation createReservation(User user, Bike bike, LocalDate start, LocalDate end, String assurance);

    /** Vrai si l'utilisateur est le locataire OU le propriétaire du vélo de cette réservation. */
    boolean isParticipant(Reservation r, User user);

    /** Vrai si la réservation peut être payée : confirmée (automatiquement ou par le propriétaire) et pas encore payée. */
    boolean isPayable(Reservation r);
}

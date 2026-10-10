package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.model.ReservationStatus;

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
    List<Reservation> getReservationsByStatusesAndUser(List<ReservationStatus> statuses, User user);
    long getTotalConfirmedReservations();

    /** Vrai si le vélo a au moins une réservation en cours (PENDING ou CONFIRMED). */
    boolean hasActiveReservations(Long bikeId);

    /** Réservations reçues : faites sur les vélos appartenant à ce propriétaire. */
    List<Reservation> getReservationsOnOwnerBikes(User owner);

    /** Réservations qui bloquent le calendrier d'un vélo (PENDING, CONFIRMED). */
    List<Reservation> getBookedReservationsForBike(Long bikeId);

    /** Vrai si la période [start, end] chevauche une réservation bloquante du vélo (en attente, confirmée ou en cours). */
    boolean hasBookingOverlap(Long bikeId, LocalDate start, LocalDate end);

    /** Comme hasBookingOverlap, sans tenir compte de la réservation excluedReservationId (ex. celle que le propriétaire accepte). */
    boolean hasOverlapWithOthers(Long bikeId, LocalDate start, LocalDate end, Long excludedReservationId);

    /** Crée une réservation (durée + statut initial) et l'enregistre. Règles partagées par le site et l'API. */
    Reservation createReservation(User user, Bike bike, LocalDate start, LocalDate end, String assurance);

    /** Vrai si l'utilisateur est le locataire OU le propriétaire du vélo de cette réservation. */
    boolean isParticipant(Reservation r, User user);

    /** Vrai si la réservation peut être payée : confirmée (automatiquement ou par le propriétaire) et pas encore payée. */
    boolean isPayable(Reservation r);

    /** Passe en COMPLETED les locations payées dont la date de fin est passée. Retourne leur nombre. */
    int terminerLocationsPassees(LocalDate aujourdhui);
}

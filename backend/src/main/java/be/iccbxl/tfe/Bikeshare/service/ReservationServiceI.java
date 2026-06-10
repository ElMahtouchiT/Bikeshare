package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
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
}

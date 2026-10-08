package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.ReservationRepository;
import be.iccbxl.tfe.Bikeshare.service.ReservationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService implements ReservationServiceI {

    /** Statuts considérés comme « en cours » : bloquent la suppression du vélo. */
    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "CONFIRMED", "NOW");

    /** Statuts qui bloquent le calendrier (réservation confirmée / en cours). */
    private static final List<String> BOOKED_STATUSES = List.of("CONFIRMED", "NOW");

    @Autowired private ReservationRepository reservationRepository;

    @Override public List<Reservation> getAllReservations() { return reservationRepository.findAll(); }

    @Override
    public List<ReservationDTO> getAllReservationsDTOs() {
        return reservationRepository.findAll().stream()
                .map(MapperDTO::toReservationDTO).collect(Collectors.toList());
    }

    @Override public Reservation getReservationById(Long id) { return reservationRepository.findById(id).orElse(null); }
    @Override public Reservation addReservation(Reservation r) { return reservationRepository.save(r); }
    @Override public Reservation saveReservation(Reservation r) { return reservationRepository.save(r); }

    @Override
    public int terminerLocationsPassees(LocalDate aujourdhui) {
        int terminees = 0;
        for (Reservation r : reservationRepository.findAll()) {
            if (r.doitEtreTerminee(aujourdhui)) {
                r.setStatut("COMPLETED");
                reservationRepository.save(r);
                terminees++;
            }
        }
        return terminees;
    }

    /**
     * Crée une réservation : construit l'objet, calcule la durée en jours et fixe le statut
     * initial (AUTOMATIC = confirmée d'office, sinon PENDING en attente du propriétaire),
     * puis l'enregistre. Règles centralisées, appelées par le site ET l'API.
     */
    @Override
    public Reservation createReservation(User user, Bike bike, LocalDate start, LocalDate end, String assurance) {
        Reservation r = new Reservation();
        r.setBike(bike);
        r.setUser(user);
        r.setStartLocation(start);
        r.setEndLocation(end);
        r.setDuration((int) ChronoUnit.DAYS.between(start, end));
        r.setAssurance(assurance);
        r.setStatut("AUTOMATIC".equalsIgnoreCase(bike.getReservationMode()) ? "CONFIRMED" : "PENDING");
        return reservationRepository.save(r);
    }

    /** Vrai si l'utilisateur est le locataire OU le propriétaire du vélo de cette réservation. */
    @Override
    public boolean isParticipant(Reservation r, User user) {
        if (r == null || user == null || user.getId() == null) return false;
        boolean isRenter = r.getUser() != null && user.getId().equals(r.getUser().getId());
        boolean isOwner = r.getBike() != null && r.getBike().getUser() != null
                && user.getId().equals(r.getBike().getUser().getId());
        return isRenter || isOwner;
    }

    /** Une réservation ne se paie que si elle est confirmée et pas encore payée (refusée ou annulée : jamais). */
    @Override
    public boolean isPayable(Reservation r) {
        return r != null && "CONFIRMED".equalsIgnoreCase(r.getStatut()) && r.getPayment() == null;
    }

    @Override
    public Reservation updateReservation(Long id, Reservation r) {
        r.setId(id);
        return reservationRepository.save(r);
    }

    @Override public void deleteReservation(Long id) { reservationRepository.deleteById(id); }

    @Override
    public List<ReservationDTO> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user).stream()
                .map(MapperDTO::toReservationDTO).collect(Collectors.toList());
    }

    @Override
    public List<Reservation> getReservationsByStatusesAndUser(List<String> statuses, User user) {
        return reservationRepository.findByStatutInAndUser(statuses, user);
    }

    @Override
    public long getTotalConfirmedReservations() {
        return reservationRepository.countByStatut("CONFIRMED");
    }

    @Override
    public boolean hasActiveReservations(Long bikeId) {
        return reservationRepository.existsByBikeIdAndStatutIn(bikeId, ACTIVE_STATUSES);
    }

    @Override
    public List<Reservation> getReservationsOnOwnerBikes(User owner) {
        return reservationRepository.findByBikeUserId(owner.getId());
    }

    @Override
    public List<Reservation> getBookedReservationsForBike(Long bikeId) {
        return reservationRepository.findByBikeIdAndStatutIn(bikeId, BOOKED_STATUSES);
    }

    @Override
    public boolean hasBookingOverlap(Long bikeId, LocalDate start, LocalDate end) {
        if (start == null || end == null) return false;
        return getBookedReservationsForBike(bikeId).stream().anyMatch(r ->
                r.getStartLocation() != null && r.getEndLocation() != null
                        && !r.getStartLocation().isAfter(end)
                        && !r.getEndLocation().isBefore(start));
    }
}

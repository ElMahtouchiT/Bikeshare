package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByBikeUserId(Long ownerId);
    List<Reservation> findByBikeIdIn(List<Long> bikeIds);
    List<Reservation> findByStatutInAndUser(List<String> statuses, User user);

    long countByStatut(String statut);
    boolean existsByBikeIdAndStatutIn(Long bikeId, List<String> statuses);

    @Query("SELECT r.bike.id, COUNT(r) AS c FROM Reservation r GROUP BY r.bike.id ORDER BY c DESC")
    List<Object[]> findMostReservedBikes();
}

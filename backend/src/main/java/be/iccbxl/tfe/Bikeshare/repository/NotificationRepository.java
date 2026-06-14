package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Notification;
import be.iccbxl.tfe.Bikeshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByBikeId(Long bikeId);
    List<Notification> findByToUserId(Long userId);
    List<Notification> findByToUser(User user);
    List<Notification> findByFromUser(User user);
    List<Notification> findByToUserOrFromUser(User toUser, User fromUser);

    /* ─── Notifications reçues : compteur non lu, liste, marquage ─── */
    long countByToUserIdAndReadFalse(Long userId);
    List<Notification> findByToUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("update Notification n set n.read = true where n.toUser.id = :userId and n.read = false")
    int markAllReadForUser(@Param("userId") Long userId);
}

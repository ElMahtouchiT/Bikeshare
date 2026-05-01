package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Notification;
import be.iccbxl.tfe.Bikeshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByBikeId(Long bikeId);
    List<Notification> findByToUserId(Long userId);
    List<Notification> findByToUser(User user);
    List<Notification> findByFromUser(User user);
    List<Notification> findByToUserOrFromUser(User toUser, User fromUser);
}

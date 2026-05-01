package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Bikeshare.model.Notification;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.NotificationRepository;
import be.iccbxl.tfe.Bikeshare.service.NotificationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService implements NotificationServiceI {

    @Autowired private NotificationRepository notificationRepository;

    @Override public Notification save(Notification notification) { return notificationRepository.save(notification); }
    @Override public List<Notification> getNotificationsForBike(Long bikeId) { return notificationRepository.findByBikeId(bikeId); }
    @Override public List<Notification> getNotificationsForUser(Long userId) { return notificationRepository.findByToUserId(userId); }
    @Override public List<Notification> getReceivedNotifications(User user) { return notificationRepository.findByToUser(user); }
    @Override public List<Notification> getSentNotifications(User user) { return notificationRepository.findByFromUser(user); }

    public List<NotificationDTO> getNotificationsByUser(Long userId) {
        return notificationRepository.findByToUserId(userId).stream()
                .map(MapperDTO::toNotificationDTO).collect(Collectors.toList());
    }
}

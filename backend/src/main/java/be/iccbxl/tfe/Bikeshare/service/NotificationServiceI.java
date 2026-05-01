package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.model.Notification;
import be.iccbxl.tfe.Bikeshare.model.User;
import java.util.List;

public interface NotificationServiceI {
    List<Notification> getNotificationsForBike(Long bikeId);
    List<Notification> getNotificationsForUser(Long userId);
    List<Notification> getReceivedNotifications(User user);
    List<Notification> getSentNotifications(User user);
    Notification save(Notification notification);
}

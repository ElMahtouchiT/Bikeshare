package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Notification;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.NotificationRepository;
import be.iccbxl.tfe.Bikeshare.service.NotificationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    /* ─── Notifications reçues (cloche + page dédiée) ─── */

    /** Nombre de notifications non lues d'un utilisateur (pour le badge de la navbar). */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByToUserIdAndReadFalse(userId);
    }

    /** Toutes les notifications d'un utilisateur, les plus récentes d'abord. */
    public List<Notification> getAllForUser(Long userId) {
        return notificationRepository.findByToUserIdOrderByCreatedAtDesc(userId);
    }

    /** Marque toutes les notifications de l'utilisateur comme lues. */
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    /** Crée et enregistre une notification destinée à {@code to}. */
    public Notification notify(User to, User from, Bike bike, String type, String message, String link) {
        Notification n = new Notification();
        n.setToUser(to);
        n.setFromUser(from);
        n.setBike(bike);
        n.setType(type);
        n.setMessage(message);
        n.setLink(link);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }
}

package be.iccbxl.tfe.Bikeshare.DTO;

import be.iccbxl.tfe.Bikeshare.model.*;
import java.util.stream.Collectors;

/** Conversion des entités en DTO. */
public class MapperDTO {

    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setAdresse(user.getAdresse());
        dto.setLocality(user.getLocality());
        dto.setPostalCode(user.getPostalCode());
        dto.setPhone(user.getPhone());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setIban(user.getIban());
        dto.setBic(user.getBic());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setVerified(user.isVerified());
        if (user.getRoles() != null)
            dto.setRoles(user.getRoles().stream().map(MapperDTO::toRoleDTO).collect(Collectors.toList()));
        return dto;
    }

    public static RoleDTO toRoleDTO(Role role) {
        return role == null ? null : new RoleDTO(role.getId(), role.getRole());
    }

    public static CategoryDTO toCategoryDTO(Category c) {
        return c == null ? null : new CategoryDTO(c.getId(), c.getCategory());
    }

    public static PriceDTO toPriceDTO(Price p) {
        if (p == null) return null;
        PriceDTO dto = new PriceDTO();
        dto.setId(p.getId());
        dto.setLowPrice(p.getLowPrice());
        dto.setMiddlePrice(p.getMiddlePrice());
        dto.setHighPrice(p.getHighPrice());
        dto.setPromo1(p.getPromo1());
        dto.setPromo2(p.getPromo2());
        return dto;
    }

    public static BikeDTO toBikeDTO(Bike bike) {
        if (bike == null) return null;
        BikeDTO dto = new BikeDTO();
        dto.setId(bike.getId());
        dto.setBrand(bike.getBrand());
        dto.setModel(bike.getModel());
        dto.setBikeType(bike.getBikeType());
        dto.setElectric(bike.isElectric());
        dto.setWheelSize(bike.getWheelSize());
        dto.setGears(bike.getGears());
        dto.setFrameSize(bike.getFrameSize());
        dto.setAdresse(bike.getAdresse());
        dto.setPostalCode(bike.getPostalCode());
        dto.setLocality(bike.getLocality());
        dto.setReservationMode(bike.getReservationMode());
        dto.setOnline(bike.getOnline());
        dto.setLatitude(bike.getLatitude());
        dto.setLongitude(bike.getLongitude());
        dto.setCategory(toCategoryDTO(bike.getCategory()));
        dto.setPrice(toPriceDTO(bike.getPrice()));
        dto.setOwner(toUserDTO(bike.getUser()));
        if (bike.getPhotos() != null)
            dto.setPhotos(bike.getPhotos().stream()
                    .map(p -> new PhotoDTO(p.getId(), p.getUrl()))
                    .collect(Collectors.toList()));
        if (bike.getReservations() != null) {
            long count = bike.getReservations().stream()
                    .filter(r -> r.getEvaluation() != null).count();
            dto.setReviewCount((int) count);
            if (count > 0) {
                double avg = bike.getReservations().stream()
                        .filter(r -> r.getEvaluation() != null)
                        .mapToInt(r -> r.getEvaluation().getNote())
                        .average().orElse(0d);
                dto.setAverageRating(avg);
            } else {
                dto.setAverageRating(0d);
            }
        }
        return dto;
    }

    public static ReservationDTO toReservationDTO(Reservation r) {
        if (r == null) return null;
        ReservationDTO dto = new ReservationDTO();
        dto.setId(r.getId());
        dto.setStartLocation(r.getStartLocation());
        dto.setEndLocation(r.getEndLocation());
        dto.setDuration(r.getDuration());
        dto.setStatut(r.getStatut());
        dto.setAssurance(r.getAssurance());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setBike(toBikeDTO(r.getBike()));
        dto.setUser(toUserDTO(r.getUser()));
        if (r.getPayment() != null) dto.setTotalPrice(r.getPayment().getTotalPrice());
        return dto;
    }

    public static EvaluationDTO toEvaluationDTO(Evaluation e) {
        if (e == null) return null;
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(e.getId());
        dto.setNote(e.getNote());
        dto.setComment(e.getComment());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getReservation() != null) {
            dto.setReservationId(e.getReservation().getId());
            if (e.getReservation().getBike() != null) {
                dto.setBikeId(e.getReservation().getBike().getId());
                dto.setBikeLabel(e.getReservation().getBike().getBrand() + " " + e.getReservation().getBike().getModel());
            }
            if (e.getReservation().getUser() != null)
                dto.setAuthorName(e.getReservation().getUser().getFirstName());
        }
        return dto;
    }

    public static ClaimDTO toClaimDto(Claim c) {
        if (c == null) return null;
        ClaimDTO dto = new ClaimDTO();
        dto.setId(c.getId());
        dto.setClaimantRole(c.getClaimantRole());
        dto.setMessage(c.getMessage());
        dto.setStatus(c.getStatus());
        dto.setResponse(c.getResponse());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setResponseAt(c.getResponseAt());
        if (c.getReservation() != null) dto.setReservationId(c.getReservation().getId());
        return dto;
    }

    public static NotificationDTO toNotificationDTO(Notification n) {
        if (n == null) return null;
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        if (n.getBike() != null) dto.setBikeId(n.getBike().getId());
        if (n.getFromUser() != null) dto.setFromUserId(n.getFromUser().getId());
        if (n.getToUser() != null) dto.setToUserId(n.getToUser().getId());
        return dto;
    }

    public static ChatMessageDTO toChatMessageDTO(ChatMessage m) {
        if (m == null) return null;
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(m.getId());
        dto.setContent(m.getContent());
        dto.setReservationId(m.getReservation() != null ? m.getReservation().getId() : null);
        dto.setFromUserId(m.getFromUserId());
        dto.setToUserId(m.getToUserId());
        dto.setSentAt(m.getSentAt());
        return dto;
    }
}

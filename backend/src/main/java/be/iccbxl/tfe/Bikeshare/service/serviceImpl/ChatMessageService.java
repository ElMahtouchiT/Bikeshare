package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.model.ChatMessage;
import be.iccbxl.tfe.Bikeshare.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {
    @Autowired private ChatMessageRepository chatMessageRepository;

    public ChatMessage save(ChatMessage message) { return chatMessageRepository.save(message); }

    public List<ChatMessageDTO> getMessagesByReservationId(Long reservationId, Long currentUserId) {
        return chatMessageRepository.findByReservationIdOrderBySentAtAsc(reservationId).stream()
                .filter(m -> m.getFromUserId().equals(currentUserId) || m.getToUserId().equals(currentUserId))
                .map(MapperDTO::toChatMessageDTO).collect(Collectors.toList());
    }
}

package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.GainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GainService {
    @Autowired private GainRepository gainRepository;

    public Gain save(Gain gain) { return gainRepository.save(gain); }
    public List<Gain> getAll() { return gainRepository.findAll(); }

    /** Gains d'un propriétaire (via le paiement → réservation → vélo → propriétaire). */
    public List<Gain> getGainsForOwner(User owner) {
        return gainRepository.findAll().stream()
                .filter(g -> g.getPayment() != null && g.getPayment().getReservation() != null
                        && g.getPayment().getReservation().getBike() != null
                        && g.getPayment().getReservation().getBike().getUser() != null
                        && g.getPayment().getReservation().getBike().getUser().getId().equals(owner.getId()))
                .collect(Collectors.toList());
    }
}

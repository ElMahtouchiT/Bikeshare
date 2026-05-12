package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Claim;
import be.iccbxl.tfe.Bikeshare.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaimService {
    @Autowired private ClaimRepository claimRepository;

    public List<Claim> getALlClaims() { return claimRepository.findAll(); }
    public Claim getClaimById(Long id) { return claimRepository.findById(id).orElse(null); }
    public Claim save(Claim claim) { return claimRepository.save(claim); }
    public boolean existsByReservationId(Long reservationId) { return claimRepository.existsByReservationId(reservationId); }

    public void resolveClaim(Long id) {
        Claim c = getClaimById(id);
        if (c != null) { c.closeClaim(); claimRepository.save(c); }
    }

    public void addResponseToClaim(Long id, String response) {
        Claim c = getClaimById(id);
        if (c != null) {
            c.setResponse(response);
            c.setResponseAt(LocalDateTime.now());
            c.setStatus("IN_PROGRESS");
            claimRepository.save(c);
        }
    }
}

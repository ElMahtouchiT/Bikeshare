package be.iccbxl.tfe.Bikeshare.restController.admin;

import be.iccbxl.tfe.Bikeshare.DTO.ClaimDTO;
import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/claims")
public class AdminClaimRestController {

    @Autowired private ClaimService claimService;

    @GetMapping
    public ResponseEntity<List<ClaimDTO>> getAllClaims() {
        List<ClaimDTO> dtos = claimService.getALlClaims().stream()
                .map(MapperDTO::toClaimDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/resolve/{id}")
    public ResponseEntity<String> resolve(@PathVariable Long id) {
        claimService.resolveClaim(id);
        return ResponseEntity.ok("Réclamation résolue");
    }

    @PostMapping("/response/{id}")
    public ResponseEntity<String> respond(@PathVariable Long id, @RequestBody Map<String, String> body) {
        claimService.addResponseToClaim(id, body.get("response"));
        return ResponseEntity.ok("Réponse enregistrée");
    }
}

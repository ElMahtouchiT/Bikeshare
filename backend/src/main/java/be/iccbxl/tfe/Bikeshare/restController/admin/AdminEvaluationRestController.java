package be.iccbxl.tfe.Bikeshare.restController.admin;

import be.iccbxl.tfe.Bikeshare.DTO.EvaluationDTO;
import be.iccbxl.tfe.Bikeshare.DTO.EvaluationDashboardDTO;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Evaluation Management", description = "Gestion des évaluations par les administrateurs")
public class AdminEvaluationRestController {

    @Autowired private EvaluationService evaluationService;

    @Operation(summary = "Toutes les évaluations groupées par vélo")
    @GetMapping("/evaluations")
    public List<EvaluationDTO> all() { return evaluationService.getAllEvaluationsGroupedByBike(); }

    @Operation(summary = "Supprimer une évaluation")
    @DeleteMapping("/evaluations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            evaluationService.deleteEvaluationById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Données du tableau de bord des évaluations")
    @GetMapping("/evaluations/dashboard")
    public ResponseEntity<EvaluationDashboardDTO> dashboard() {
        return ResponseEntity.ok(evaluationService.getEvaluationDashboardData());
    }

    @Operation(summary = "Évaluations d'un vélo")
    @GetMapping("/evaluations/bike/{bikeId}")
    public ResponseEntity<List<EvaluationDTO>> byBike(@PathVariable Long bikeId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByBikeId(bikeId));
    }
}

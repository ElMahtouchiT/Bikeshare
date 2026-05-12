package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.util.List;

@Data
public class EvaluationDashboardDTO {
    private double averageNote;
    private long totalEvaluations;
    private List<EvaluationDTO> latest;
}

package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardKpiDTO {
    private BigDecimal totalRevenue;
    private BigDecimal totalBenefit;
    private long totalUsers;
    private long totalReservations;
}

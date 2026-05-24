package be.iccbxl.tfe.Bikeshare.restController.admin;

import be.iccbxl.tfe.Bikeshare.DTO.DashboardKpiDTO;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.PaymentService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ReservationService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class AdminDashboardKpiRestController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final ReservationService reservationService;

    @Autowired
    public AdminDashboardKpiRestController(PaymentService paymentService, UserService userService,
                                           ReservationService reservationService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping("/kpi")
    public DashboardKpiDTO getDashboardKpi() {
        DashboardKpiDTO kpi = new DashboardKpiDTO();
        kpi.setTotalRevenue(paymentService.getTotalRevenue());
        kpi.setTotalBenefit(paymentService.getTotalBenefit());
        kpi.setTotalUsers(userService.getTotalUsers());
        kpi.setTotalReservations(reservationService.getTotalConfirmedReservations());
        return kpi;
    }
}

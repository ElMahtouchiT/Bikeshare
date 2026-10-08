package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.ReservationServiceI;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Facture PDF d'une réservation : accessible au locataire concerné et aux admins. */
@Controller
public class InvoiceController {

    @Autowired private ReservationServiceI reservationService;
    @Autowired private InvoiceService invoiceService;

    @GetMapping("/account/reservations/{id}/facture.pdf")
    public ResponseEntity<byte[]> facture(@PathVariable Long id,
                                          @AuthenticationPrincipal CustomUserDetail userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Reservation r = reservationService.getReservationById(id);
        if (r == null || r.getPayment() == null) return ResponseEntity.notFound().build();

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean locataire = r.getUser() != null && r.getUser().getId().equals(userDetails.getUser().getId());
        if (!admin && !locataire) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        byte[] pdf = invoiceService.genererFacture(r);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"facture-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

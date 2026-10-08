package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.service.serviceImpl.FinanceService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

/** Finances de la plateforme : paiements, versements aux propriétaires, remboursements. Réservé aux admins. */
@Controller
@RequestMapping("/admin/finances")
public class AdminFinanceController {

    @Autowired private FinanceService financeService;
    @Autowired private PaymentService paymentService;

    @GetMapping
    public String finances(Model model) {
        model.addAttribute("paiements", financeService.getPaiements());
        model.addAttribute("gains", financeService.getGains());
        model.addAttribute("remboursements", financeService.getRemboursements());
        model.addAttribute("totalRevenu", paymentService.getTotalRevenue());
        model.addAttribute("totalBenefit", paymentService.getTotalBenefit());
        model.addAttribute("montantDu", financeService.getMontantDu());
        model.addAttribute("nbPaiements", financeService.getPaiements().size());
        return "admin/finances/index";
    }

    @PostMapping("/gains/{id}/transfer")
    public String marquerVerse(@PathVariable Long id, RedirectAttributes ra) {
        boolean ok = financeService.marquerVerse(id);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Gain marqué comme versé au propriétaire." : "Gain introuvable ou déjà versé.");
        return "redirect:/admin/finances";
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exporterCsv() {
        String csv = financeService.exporterCsv(financeService.getPaiements());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"paiements-bikeshare.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}

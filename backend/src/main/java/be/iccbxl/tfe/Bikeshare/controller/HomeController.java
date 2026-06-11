package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired private BikeService bikeService;
    @Autowired private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("bikes", bikeService.getAllOnlineBikes());
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String locality,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Boolean electric,
                         Model model) {
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("bikes", bikeService.search(locality, categoryId, electric));
        return "bike/index";
    }

    /* ─── Pages d'information (pied de page) ─────────────────── */

    @GetMapping("/contact")
    public String contact() { return "pages/contact"; }

    @GetMapping("/conditions")
    public String conditions() { return "pages/conditions"; }

    @GetMapping("/mentions-legales")
    public String mentionsLegales() { return "pages/mentions-legales"; }

    @GetMapping("/reclamation")
    public String reclamationForm() { return "pages/reclamation"; }

    @PostMapping("/reclamation")
    public String reclamationSubmit(@RequestParam String objet,
                                    @RequestParam String email,
                                    @RequestParam String message,
                                    RedirectAttributes redirectAttributes) {
        // Accusé de réception (l'envoi par e-mail / le stockage pourront être branchés ultérieurement)
        redirectAttributes.addFlashAttribute("success",
                "Votre réclamation a bien été envoyée. Notre équipe vous répondra dans les meilleurs délais.");
        return "redirect:/reclamation";
    }
}

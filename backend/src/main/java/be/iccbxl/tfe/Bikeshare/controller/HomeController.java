package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Bike;
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
        java.util.List<Bike> online = bikeService.getAllOnlineBikes();
        int featured = 6; // vitrine : un aperçu ; tout le catalogue est sur /bikes
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("bikes",
                online.size() > featured ? new java.util.ArrayList<>(online.subList(0, featured)) : online);
        model.addAttribute("totalBikes", online.size());
        return "index";
    }

    /** Ancienne URL de recherche : on redirige vers le catalogue filtré (source unique de vérité). */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String locality,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Boolean electric,
                         RedirectAttributes redirectAttributes) {
        if (locality != null && !locality.isBlank()) redirectAttributes.addAttribute("locality", locality);
        if (categoryId != null) redirectAttributes.addAttribute("categoryId", categoryId);
        if (electric != null) redirectAttributes.addAttribute("electric", electric);
        return "redirect:/bikes";
    }

    /* ─── Pages d'information (pied de page) ─────────────────── */

    @GetMapping("/a-propos")
    public String about() { return "pages/a-propos"; }

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

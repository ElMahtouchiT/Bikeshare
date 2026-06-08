package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Role;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.RoleRepository;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * Interface d'administration (vues Thymeleaf).
 * Accessible uniquement avec ROLE_ADMIN (configuré dans SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService       userService;
    @Autowired private BikeService       bikeService;
    @Autowired private ReservationService reservationService;
    @Autowired private ClaimService      claimService;
    @Autowired private EvaluationService evaluationService;
    @Autowired private PaymentService    paymentService;
    @Autowired private RoleRepository    roleRepository;

    // ── Dashboard ─────────────────────────────────────────────
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",        userService.getTotalUsers());
        model.addAttribute("totalBikes",        bikeService.getAllBikes().size());
        model.addAttribute("totalReservations", reservationService.getTotalConfirmedReservations());
        model.addAttribute("totalRevenue",      paymentService.getTotalRevenue());
        model.addAttribute("totalBenefit",      paymentService.getTotalBenefit());
        model.addAttribute("pendingClaims",
                claimService.getALlClaims().stream()
                        .filter(c -> "PENDING".equals(c.getStatus())).count());
        model.addAttribute("pendingBikes",
                bikeService.getAllBikes().stream()
                        .filter(b -> b.getOnline() == null || !b.getOnline()).count());
        return "admin/index";
    }

    // ── Validation des vélos ──────────────────────────────────
    @GetMapping("/bikes")
    public String bikes(Model model) {
        List<Bike> bikes = bikeService.getAllBikes();
        // Vélos à valider (hors ligne) en premier
        bikes.sort(Comparator.comparing(b -> b.getOnline() != null && b.getOnline()));
        long pending = bikes.stream().filter(b -> b.getOnline() == null || !b.getOnline()).count();
        model.addAttribute("bikes", bikes);
        model.addAttribute("pendingBikes", pending);
        return "admin/bikes/index";
    }

    @PostMapping("/bikes/{id}/publish")
    public String publishBike(@PathVariable Long id, RedirectAttributes ra) {
        Bike bike = bikeService.getBikeById(id);
        if (bike == null) { ra.addFlashAttribute("error", "Vélo introuvable."); return "redirect:/admin/bikes"; }
        bike.setOnline(true);
        bikeService.saveBike(bike);
        ra.addFlashAttribute("success", "Vélo « " + bike.getBrand() + " " + bike.getModel() + " » publié dans le catalogue.");
        return "redirect:/admin/bikes";
    }

    @PostMapping("/bikes/{id}/unpublish")
    public String unpublishBike(@PathVariable Long id, RedirectAttributes ra) {
        Bike bike = bikeService.getBikeById(id);
        if (bike == null) { ra.addFlashAttribute("error", "Vélo introuvable."); return "redirect:/admin/bikes"; }
        bike.setOnline(false);
        bikeService.saveBike(bike);
        ra.addFlashAttribute("success", "Vélo « " + bike.getBrand() + " " + bike.getModel() + " » retiré du catalogue.");
        return "redirect:/admin/bikes";
    }

    // ── Gestion des utilisateurs ──────────────────────────────
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/users/index";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam String roleName,
                             RedirectAttributes ra) {
        User user = userService.getUserById(id);
        if (user == null) { ra.addFlashAttribute("error", "Utilisateur introuvable."); return "redirect:/admin/users"; }
        Role role = roleRepository.findByRole(roleName);
        if (role == null) { ra.addFlashAttribute("error", "Rôle introuvable."); return "redirect:/admin/users"; }
        user.getRoles().clear();
        user.getRoles().add(role);
        userService.saveUser(user);
        ra.addFlashAttribute("success", "Rôle de " + user.getFirstName() + " modifié en " + roleName);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        User user = userService.getUserById(id);
        if (user == null) { ra.addFlashAttribute("error", "Utilisateur introuvable."); return "redirect:/admin/users"; }
        userService.deleteUser(id);
        ra.addFlashAttribute("success", "Utilisateur supprimé.");
        return "redirect:/admin/users";
    }

    // ── Gestion des réclamations ──────────────────────────────
    @GetMapping("/claims")
    public String claims(Model model) {
        model.addAttribute("claims", claimService.getALlClaims());
        return "admin/claims/index";
    }

    @PostMapping("/claims/{id}/resolve")
    public String resolveClaim(@PathVariable Long id, RedirectAttributes ra) {
        claimService.resolveClaim(id);
        ra.addFlashAttribute("success", "Réclamation clôturée.");
        return "redirect:/admin/claims";
    }

    // ── Gestion des évaluations ───────────────────────────────
    @GetMapping("/evaluations")
    public String evaluations(Model model) {
        model.addAttribute("evaluations", evaluationService.getAllEvaluationsGroupedByBike());
        model.addAttribute("dashboard",   evaluationService.getEvaluationDashboardData());
        return "admin/evaluations/index";
    }

    @PostMapping("/evaluations/{id}/delete")
    public String deleteEvaluation(@PathVariable Long id, RedirectAttributes ra) {
        evaluationService.deleteEvaluationById(id);
        ra.addFlashAttribute("success", "Évaluation supprimée.");
        return "redirect:/admin/evaluations";
    }
}

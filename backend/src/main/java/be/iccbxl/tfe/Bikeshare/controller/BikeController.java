package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.CategoryService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.EvaluationService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BikeController {

    private static final Logger logger = LoggerFactory.getLogger(BikeController.class);

    @Autowired private BikeService bikeService;
    @Autowired private CategoryService categoryService;
    @Autowired private EvaluationService evaluationService;
    @Autowired private ReservationService reservationService;

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @GetMapping("/bikes")
    public String getAllBikes(@RequestParam(required = false) String locality,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) String bikeType,
                              @RequestParam(required = false) Boolean electric,
                              @RequestParam(required = false) Double priceMin,
                              @RequestParam(required = false) Double priceMax,
                              Model model) {
        model.addAttribute("bikes",
                bikeService.search(locality, categoryId, bikeType, electric, priceMin, priceMax));
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("bikeTypes", bikeService.getBikeTypes());
        // Valeurs courantes pour pré-remplir le formulaire de filtres
        model.addAttribute("fLocality", locality);
        model.addAttribute("fCategoryId", categoryId);
        model.addAttribute("fBikeType", bikeType);
        model.addAttribute("fElectric", electric);
        model.addAttribute("fPriceMin", priceMin);
        model.addAttribute("fPriceMax", priceMax);
        return "bike/index";
    }

    @GetMapping("/bikes/{id}")
    public String getBikeById(@PathVariable Long id, Model model) {
        Bike bike = bikeService.getBikeById(id);
        if (bike == null) return "redirect:/";

        // Plages déjà réservées (confirmées/en cours) → grisées dans le calendrier
        List<Map<String, String>> reservedRanges = new ArrayList<>();
        for (Reservation r : reservationService.getBookedReservationsForBike(id)) {
            if (r.getStartLocation() != null && r.getEndLocation() != null) {
                Map<String, String> range = new HashMap<>();
                range.put("from", r.getStartLocation().toString());
                range.put("to", r.getEndLocation().toString());
                reservedRanges.add(range);
            }
        }

        model.addAttribute("bike", bike);
        model.addAttribute("averageRating", bikeService.calculateAverageRating(bike));
        model.addAttribute("evaluations", evaluationService.getEvaluationsByBikeId(id));
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        model.addAttribute("reservedRanges", reservedRanges);
        return "bike/show";
    }
}

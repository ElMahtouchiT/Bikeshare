package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.EvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BikeController {

    private static final Logger logger = LoggerFactory.getLogger(BikeController.class);

    @Autowired private BikeService bikeService;
    @Autowired private EvaluationService evaluationService;

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @GetMapping("/bikes")
    public String getAllBikes(Model model) {
        model.addAttribute("bikes", bikeService.getAllOnlineBikes());
        return "bike/index";
    }

    @GetMapping("/bikes/{id}")
    public String getBikeById(@PathVariable Long id, Model model) {
        Bike bike = bikeService.getBikeById(id);
        if (bike == null) return "redirect:/";
        model.addAttribute("bike", bike);
        model.addAttribute("averageRating", bikeService.calculateAverageRating(bike));
        model.addAttribute("evaluations", evaluationService.getEvaluationsByBikeId(id));
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        return "bike/show";
    }
}

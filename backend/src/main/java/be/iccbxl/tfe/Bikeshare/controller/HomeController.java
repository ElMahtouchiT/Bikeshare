package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}

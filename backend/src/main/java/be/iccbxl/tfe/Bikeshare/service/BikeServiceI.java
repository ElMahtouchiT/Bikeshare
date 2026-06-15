package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.DTO.BikeDTO;
import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.User;
import java.util.List;
import java.util.Map;

public interface BikeServiceI {
    List<Bike> getAllBikes();
    List<Bike> getAllOnlineBikes();
    Bike getBikeById(Long id);
    Bike saveBike(Bike bike);
    Bike updateBike(Long id, Bike bike);
    void deleteBike(Long id);
    List<Bike> getBikesByUser(User user);
    List<Bike> search(String locality, Long categoryId, String bikeType,
                      Boolean electric, Double priceMin, Double priceMax);
    List<String> getBikeTypes();
    double calculateAverageRating(Bike bike);
    Map<Long, Double> getAverageRatingsForBikes();
    Map<Long, Integer> getReviewCountsForBikes();
    double computeTotal(Bike bike, int days);
}

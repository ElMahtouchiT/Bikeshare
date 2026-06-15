package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Evaluation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.BikeRepository;
import be.iccbxl.tfe.Bikeshare.repository.UserRepository;
import be.iccbxl.tfe.Bikeshare.service.BikeServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BikeService implements BikeServiceI {

    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override public List<Bike> getAllBikes() { return bikeRepository.findAll(); }
    @Override public List<Bike> getAllOnlineBikes() { return bikeRepository.findByOnlineTrue(); }
    @Override public Bike getBikeById(Long id) { return bikeRepository.findById(id).orElse(null); }
    @Override public Bike saveBike(Bike bike) { return bikeRepository.save(bike); }

    @Override
    public Bike updateBike(Long id, Bike bike) {
        bike.setId(id);
        return bikeRepository.save(bike);
    }

    /**
     * Suppression d'un vélo. Comme {@code User.ownedBikes} est en cascade ALL + orphanRemoval,
     * un simple {@code deleteById} est annulé par la re-cascade du propriétaire. On retire donc
     * le vélo de la collection du propriétaire : l'orphanRemoval supprime alors réellement le
     * vélo et ses entités liées (photos, prix, réservations).
     */
    @Override
    @Transactional
    public void deleteBike(Long id) {
        Bike bike = bikeRepository.findById(id).orElse(null);
        if (bike == null) return;
        User owner = bike.getUser();
        if (owner != null && owner.getOwnedBikes() != null
                && owner.getOwnedBikes().removeIf(b -> b.getId().equals(id))) {
            userRepository.save(owner);
        } else {
            bikeRepository.delete(bike);
        }
    }
    @Override public List<Bike> getBikesByUser(User user) { return bikeRepository.findByUser(user); }

    @Override
    public List<Bike> search(String locality, Long categoryId, String bikeType,
                             Boolean electric, Double priceMin, Double priceMax) {
        if (locality != null && locality.isBlank()) locality = null;
        if (bikeType != null && bikeType.isBlank()) bikeType = null;
        return bikeRepository.search(locality, categoryId, bikeType, electric, priceMin, priceMax);
    }

    @Override
    public List<String> getBikeTypes() {
        return bikeRepository.findDistinctBikeTypes();
    }

    @Override
    public double calculateAverageRating(Bike bike) {
        if (bike.getReservations() == null) return 0d;
        List<Integer> notes = new ArrayList<>();
        for (var r : bike.getReservations()) {
            Evaluation e = r.getEvaluation();
            if (e != null) notes.add(e.getNote());
        }
        return notes.isEmpty() ? 0d : notes.stream().mapToInt(Integer::intValue).average().orElse(0d);
    }

    @Override
    public Map<Long, Double> getAverageRatingsForBikes() {
        Map<Long, Double> map = new HashMap<>();
        for (Bike b : bikeRepository.findAll()) map.put(b.getId(), calculateAverageRating(b));
        return map;
    }

    @Override
    public Map<Long, Integer> getReviewCountsForBikes() {
        Map<Long, Integer> map = new HashMap<>();
        for (Bike b : bikeRepository.findAll()) {
            int count = 0;
            if (b.getReservations() != null)
                for (var r : b.getReservations()) if (r.getEvaluation() != null) count++;
            map.put(b.getId(), count);
        }
        return map;
    }

    /** Prix total : tarif moyen × jours, avec réductions selon la durée. */
    @Override
    public double computeTotal(Bike bike, int days) {
        if (bike.getPrice() == null || bike.getPrice().getMiddlePrice() == null) return 0d;
        double total = bike.getPrice().getMiddlePrice() * days;
        if (days >= 7 && bike.getPrice().getPromo2() != null)
            total *= (1 - bike.getPrice().getPromo2() / 100);
        else if (days >= 3 && bike.getPrice().getPromo1() != null)
            total *= (1 - bike.getPrice().getPromo1() / 100);
        return Math.round(total * 100.0) / 100.0;
    }
}

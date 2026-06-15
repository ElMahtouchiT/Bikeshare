package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    List<Bike> findByOnlineTrue();
    List<Bike> findByUser(User user);
    List<Bike> findByUserId(Long userId);

    @Query("""
           SELECT b FROM Bike b
           LEFT JOIN b.price p
           WHERE b.online = true
             AND (:locality IS NULL
                  OR LOWER(b.locality) LIKE LOWER(CONCAT('%', :locality, '%'))
                  OR LOWER(b.postalCode) LIKE LOWER(CONCAT('%', :locality, '%')))
             AND (:categoryId IS NULL OR b.category.id = :categoryId)
             AND (:bikeType IS NULL OR b.bikeType = :bikeType)
             AND (:electric IS NULL OR b.electric = :electric)
             AND (:priceMin IS NULL OR p.middlePrice >= :priceMin)
             AND (:priceMax IS NULL OR p.middlePrice <= :priceMax)
           """)
    List<Bike> search(@Param("locality") String locality,
                      @Param("categoryId") Long categoryId,
                      @Param("bikeType") String bikeType,
                      @Param("electric") Boolean electric,
                      @Param("priceMin") Double priceMin,
                      @Param("priceMax") Double priceMax);

    /** Types de vélos distincts présents dans le catalogue en ligne (pour le menu déroulant du filtre). */
    @Query("SELECT DISTINCT b.bikeType FROM Bike b WHERE b.online = true AND b.bikeType IS NOT NULL ORDER BY b.bikeType")
    List<String> findDistinctBikeTypes();
}

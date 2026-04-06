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
           WHERE b.online = true
             AND (:locality IS NULL OR LOWER(b.locality) LIKE LOWER(CONCAT('%', :locality, '%')))
             AND (:categoryId IS NULL OR b.category.id = :categoryId)
             AND (:electric IS NULL OR b.electric = :electric)
           """)
    List<Bike> search(@Param("locality") String locality,
                      @Param("categoryId") Long categoryId,
                      @Param("electric") Boolean electric);
}

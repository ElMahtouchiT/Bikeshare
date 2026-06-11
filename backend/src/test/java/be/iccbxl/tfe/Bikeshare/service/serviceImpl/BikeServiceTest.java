package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Evaluation;
import be.iccbxl.tfe.Bikeshare.model.Price;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du calcul de prix (réductions selon la durée)
 * et de la note moyenne d'un vélo. Logique pure, sans base de données.
 */
class BikeServiceTest {

    private final BikeService bikeService = new BikeService();

    private Bike bikeWithPrice(Double middle, Double promo1, Double promo2) {
        Price price = new Price();
        price.setMiddlePrice(middle);
        price.setPromo1(promo1);
        price.setPromo2(promo2);
        Bike bike = new Bike();
        bike.setPrice(price);
        return bike;
    }

    @Test
    void computeTotal_sansReduction_pourUnJour() {
        assertThat(bikeService.computeTotal(bikeWithPrice(10.0, 10.0, 20.0), 1)).isEqualTo(10.0);
    }

    @Test
    void computeTotal_appliquePromo1_aPartirDe3Jours() {
        // 10 * 5 = 50, puis -10% = 45
        assertThat(bikeService.computeTotal(bikeWithPrice(10.0, 10.0, 20.0), 5)).isEqualTo(45.0);
    }

    @Test
    void computeTotal_appliquePromo2_aPartirDe7Jours() {
        // 10 * 7 = 70, puis -20% = 56
        assertThat(bikeService.computeTotal(bikeWithPrice(10.0, 10.0, 20.0), 7)).isEqualTo(56.0);
    }

    @Test
    void computeTotal_renvoieZero_quandPasDePrix() {
        assertThat(bikeService.computeTotal(new Bike(), 5)).isEqualTo(0.0);
    }

    @Test
    void calculateAverageRating_moyenneDesNotes() {
        Bike bike = new Bike();
        bike.setReservations(List.of(reservationWithNote(4), reservationWithNote(2)));
        assertThat(bikeService.calculateAverageRating(bike)).isEqualTo(3.0);
    }

    @Test
    void calculateAverageRating_renvoieZero_quandAucuneEvaluation() {
        assertThat(bikeService.calculateAverageRating(new Bike())).isEqualTo(0.0);
    }

    private Reservation reservationWithNote(int note) {
        Evaluation evaluation = new Evaluation();
        evaluation.setNote(note);
        Reservation reservation = new Reservation();
        reservation.setEvaluation(evaluation);
        return reservation;
    }
}

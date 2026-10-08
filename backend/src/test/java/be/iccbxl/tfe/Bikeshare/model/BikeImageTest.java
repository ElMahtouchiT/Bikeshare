package be.iccbxl.tfe.Bikeshare.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Les cartes et la fiche détail doivent afficher la même image principale. */
class BikeImageTest {

    @Test
    void sansPhoto_imagePrincipaleEstLImageDeSecoursDuType() {
        Bike bike = new Bike();
        bike.setId(128L);
        bike.setBikeType("MTB");

        assertThat(bike.getImagePrincipale()).isEqualTo("/images/bikes/mtb9.avif");
        assertThat(bike.getImagePrincipale()).isEqualTo(bike.imageDeSecours(0));
    }

    @Test
    void avecPhoto_imagePrincipaleEstLaPremierePhoto() {
        Bike bike = new Bike();
        bike.setId(128L);
        bike.setBikeType("MTB");
        Photo photo = new Photo();
        photo.setUrl("/uploads/bikes/photo.jpg");
        bike.addPhoto(photo);

        assertThat(bike.getImagePrincipale()).isEqualTo("/uploads/bikes/photo.jpg");
    }

    @Test
    void imagesDeSecoursDeLaGalerie_sontDistinctesEtCommenceParLaPrincipale() {
        Bike bike = new Bike();
        bike.setId(128L);
        bike.setBikeType("MTB");

        assertThat(bike.imageDeSecours(0)).isEqualTo("/images/bikes/mtb9.avif");
        assertThat(bike.imageDeSecours(1)).isEqualTo("/images/bikes/mtb10.avif");
        assertThat(bike.imageDeSecours(2)).isEqualTo("/images/bikes/mtb1.avif");
    }
}

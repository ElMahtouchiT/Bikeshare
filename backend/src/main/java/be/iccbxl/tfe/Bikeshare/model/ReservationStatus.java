package be.iccbxl.tfe.Bikeshare.model;

/** Statuts d'une réservation : code en anglais dans le code, libellé français affiché partout. */
public enum ReservationStatus {
    PENDING("En attente de l'accord du propriétaire"),
    CONFIRMED("Confirmée"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée"),
    REFUSED("Refusée");

    private final String libelle;

    ReservationStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}

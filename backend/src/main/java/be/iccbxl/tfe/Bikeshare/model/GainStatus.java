package be.iccbxl.tfe.Bikeshare.model;

/** Statuts du gain du propriétaire (sa part de 85 %) : à verser, versé, ou annulé. */
public enum GainStatus {
    PENDING("À verser"),
    TRANSFERRED("Versé"),
    ANNULE("Annulé");

    private final String libelle;

    GainStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}

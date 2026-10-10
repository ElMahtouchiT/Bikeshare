package be.iccbxl.tfe.Bikeshare.model;

/** Statuts d'un paiement fait par le locataire à BikeShare. */
public enum PaymentStatus {
    PAID("Payée"),
    REFUNDED("Remboursée"),
    FAILED("Échouée");

    private final String libelle;

    PaymentStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}

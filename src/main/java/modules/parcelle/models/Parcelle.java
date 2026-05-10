package modules.parcelle.models;

public class Parcelle {

    private int idParcelle;
    private String nom;
    private double superficie;
    private String localisation;
    private String typeSol;
    private int utilisateurId;
    private double latitude;
    private double longitude;
    private String utilisateurNom;

    // Constructor without ID (insert)
    public Parcelle(String nom, double superficie, String localisation,
                    String typeSol, int utilisateurId,
                    double latitude, double longitude) {

        this.nom = nom;
        this.superficie = superficie;
        this.localisation = localisation;
        this.typeSol = typeSol;
        this.utilisateurId = utilisateurId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Constructor with ID (select)
    public Parcelle(int idParcelle, String nom, double superficie,
                    String localisation, String typeSol,
                    int utilisateurId, double latitude, double longitude) {

        this.idParcelle = idParcelle;
        this.nom = nom;
        this.superficie = superficie;
        this.localisation = localisation;
        this.typeSol = typeSol;
        this.utilisateurId = utilisateurId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters & Setters
    public String getUtilisateurNom() {
        return utilisateurNom;
    }

    public void setUtilisateurNom(String utilisateurNom) {
        this.utilisateurNom = utilisateurNom;
    }

    public int getIdParcelle() {
        return idParcelle;
    }

    public void setIdParcelle(int idParcelle) {
        this.idParcelle = idParcelle;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(double superficie) {
        this.superficie = superficie;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getTypeSol() {
        return typeSol;
    }

    public void setTypeSol(String typeSol) {
        this.typeSol = typeSol;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return nom;
    }
}
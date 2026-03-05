package modules.carte.models;

import java.util.ArrayList;
import java.util.List;

public class Garage {

    private int id;
    private String nom;
    private String adresse;
    private double latitude;
    private double longitude;
    private int capacite;
    private String responsable;
    private String telephone;
    private List<EquipementGeo> equipements;

    public Garage() {
        this.equipements = new ArrayList<>();
    }

    public Garage(int id, String nom, String adresse, double latitude,
                  double longitude, int capacite, String responsable, String telephone) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacite = capacite;
        this.responsable = responsable;
        this.telephone = telephone;
        this.equipements = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public List<EquipementGeo> getEquipements() { return equipements; }
    public void setEquipements(List<EquipementGeo> equipements) { this.equipements = equipements; }

    public void addEquipement(EquipementGeo equipement) {
        equipements.add(equipement);
    }

    public int getEquipementsCount() {
        return equipements.size();
    }

    public double getTauxOccupation() {
        return capacite > 0 ? (equipements.size() * 100.0 / capacite) : 0;
    }

    @Override
    public String toString() {
        return nom + " (" + getEquipementsCount() + "/" + capacite + ")";
    }
}
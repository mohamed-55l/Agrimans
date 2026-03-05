package modules.carte.models;

import modules.equipement.models.Equipement;
import java.time.LocalDateTime;

public class EquipementGeo extends Equipement {

    private int garageId;
    private String garageNom;
    private String positionGPS; // "latitude,longitude"
    private double latitude;
    private double longitude;
    private LocalDateTime derniereLocalisation;
    private String statutGarage; // "DANS_GARAGE", "EN_UTILISATION", "EN_DEPLACEMENT"

    public EquipementGeo() {
        super();
    }

    // Getters et Setters
    public int getGarageId() { return garageId; }
    public void setGarageId(int garageId) { this.garageId = garageId; }

    public String getGarageNom() { return garageNom; }
    public void setGarageNom(String garageNom) { this.garageNom = garageNom; }

    public String getPositionGPS() { return positionGPS; }
    public void setPositionGPS(String positionGPS) {
        this.positionGPS = positionGPS;
        if (positionGPS != null && positionGPS.contains(",")) {
            String[] parts = positionGPS.split(",");
            try {
                this.latitude = Double.parseDouble(parts[0].trim());
                this.longitude = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                // Ignorer
            }
        }
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public LocalDateTime getDerniereLocalisation() { return derniereLocalisation; }
    public void setDerniereLocalisation(LocalDateTime derniereLocalisation) { this.derniereLocalisation = derniereLocalisation; }

    public String getStatutGarage() { return statutGarage; }
    public void setStatutGarage(String statutGarage) { this.statutGarage = statutGarage; }

    public boolean isDansGarage() {
        return "DANS_GARAGE".equals(statutGarage);
    }

    public String getCouleurMarqueur() {
        switch(getDisponibilite()) {
            case "Disponible": return "green";
            case "Non disponible": return "red";
            case "En maintenance": return "orange";
            default: return "blue";
        }
    }

    public String getIcone() {
        switch(getType()) {
            case "Tracteur": return "🚜";
            case "Semoir": return "🌱";
            case "Moissonneuse": return "🌾";
            case "Pulvérisateur": return "💧";
            default: return "⚙️";
        }
    }
}
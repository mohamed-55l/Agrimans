package modules.meteo.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrevisionMeteo {

    private LocalDateTime date;
    private double temperature;
    private double temperatureRessentie;
    private int humidite;
    private double pression;
    private double vitesseVent;
    private int directionVent;
    private String description;
    private String icone;
    private double probabilitePluie;
    private double quantitePluie;
    private String ville;
    private int nuages;

    public PrevisionMeteo() {}

    // Getters et Setters existants...
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getTemperatureRessentie() { return temperatureRessentie; }
    public void setTemperatureRessentie(double temperatureRessentie) { this.temperatureRessentie = temperatureRessentie; }

    public int getHumidite() { return humidite; }
    public void setHumidite(int humidite) { this.humidite = humidite; }

    public double getPression() { return pression; }
    public void setPression(double pression) { this.pression = pression; }

    public double getVitesseVent() { return vitesseVent; }
    public void setVitesseVent(double vitesseVent) { this.vitesseVent = vitesseVent; }

    public int getDirectionVent() { return directionVent; }
    public void setDirectionVent(int directionVent) { this.directionVent = directionVent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public double getProbabilitePluie() { return probabilitePluie; }
    public void setProbabilitePluie(double probabilitePluie) { this.probabilitePluie = probabilitePluie; }

    public double getQuantitePluie() { return quantitePluie; }
    public void setQuantitePluie(double quantitePluie) { this.quantitePluie = quantitePluie; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public int getNuages() { return nuages; }
    public void setNuages(int nuages) { this.nuages = nuages; }

    // =====================================================
    // MÉTHODES AJOUTÉES (manquantes)
    // =====================================================

    /**
     * Retourne la date complète formatée (dd/MM/yyyy HH:mm)
     */
    public String getDateComplete() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    /**
     * Retourne l'heure formatée (HH:mm)
     */
    public String getHeureFormatee() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    /**
     * Retourne le jour formaté (EEEE dd/MM)
     */
    public String getJourFormatee() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("EEEE dd/MM")) : "";
    }

    /**
     * Convertit la vitesse du vent en km/h (m/s → km/h)
     */
    public double getVentKmh() {
        return vitesseVent * 3.6;
    }

    /**
     * Retourne la direction du vent en texte
     */
    public String getDirectionVentTexte() {
        if (directionVent >= 337.5 || directionVent < 22.5) return "Nord";
        if (directionVent >= 22.5 && directionVent < 67.5) return "Nord-Est";
        if (directionVent >= 67.5 && directionVent < 112.5) return "Est";
        if (directionVent >= 112.5 && directionVent < 157.5) return "Sud-Est";
        if (directionVent >= 157.5 && directionVent < 202.5) return "Sud";
        if (directionVent >= 202.5 && directionVent < 247.5) return "Sud-Ouest";
        if (directionVent >= 247.5 && directionVent < 292.5) return "Ouest";
        return "Nord-Ouest";
    }
}
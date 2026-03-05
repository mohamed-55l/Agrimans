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
    private double probabilitePluie; // 0-100%
    private double quantitePluie; // mm
    private String ville;

    // Constructeur
    public PrevisionMeteo() {}

    // Getters et Setters
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

    // Méthodes utilitaires
    public String getDateFormatee() {
        return date.format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy HH:mm"));
    }

    public String getHeureFormatee() {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getJourFormatee() {
        return date.format(DateTimeFormatter.ofPattern("EEEE dd/MM"));
    }

    public boolean vaPleuvoir() {
        return probabilitePluie > 50 || quantitePluie > 0.1;
    }

    public String getConseilAgricole() {
        StringBuilder conseil = new StringBuilder();

        if (vaPleuvoir()) {
            conseil.append("🌧️ Pluie prévue. ");
            if (quantitePluie > 10) {
                conseil.append("Évitez les traitements et reportez les semis. ");
            } else {
                conseil.append("Bonne occasion pour les semis. ");
            }
        }

        if (temperature < 5) {
            conseil.append("❄️ Risque de gel. Protégez les cultures sensibles. ");
        } else if (temperature > 35) {
            conseil.append("☀️ Canicule. Arrosez abondamment le soir. ");
        }

        if (vitesseVent > 50) {
            conseil.append("💨 Vent fort. Évitez les traitements phytosanitaires. ");
        }

        if (humidite > 80 && temperature > 15) {
            conseil.append("⚠️ Risque de maladies fongiques (mildiou). Traitez préventivement. ");
        }

        return conseil.toString();
    }
}
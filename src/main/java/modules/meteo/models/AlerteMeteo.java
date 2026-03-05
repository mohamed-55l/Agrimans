package modules.meteo.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlerteMeteo {

    private static int compteur = 0;

    private int id;
    private String titre;
    private String message;
    private String type;      // "PLUIE", "GEL", "CANICULE", "VENT", "MALADIE"
    private LocalDateTime datePrevue;
    private LocalDateTime dateCreation;
    private String niveau;     // "INFO", "WARNING", "DANGER"
    private boolean lue;

    public AlerteMeteo(String titre, String message, String type,
                       LocalDateTime datePrevue, String niveau) {
        this.id = ++compteur;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.datePrevue = datePrevue;
        this.niveau = niveau;
        this.dateCreation = LocalDateTime.now();
        this.lue = false;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getDatePrevue() { return datePrevue; }
    public void setDatePrevue(LocalDateTime datePrevue) { this.datePrevue = datePrevue; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public String getDateFormatee() {
        return datePrevue != null ?
                datePrevue.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) :
                dateCreation.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    public String getCouleur() {
        switch(niveau) {
            case "DANGER": return "#e74c3c";
            case "WARNING": return "#f39c12";
            default: return "#3498db";
        }
    }

    public String getIcone() {
        switch(type) {
            case "GEL": return "❄️";
            case "CANICULE": return "☀️";
            case "PLUIE": return "🌧️";
            case "VENT": return "💨";
            case "MALADIE": return "🦠";
            default: return "🌤️";
        }
    }
}
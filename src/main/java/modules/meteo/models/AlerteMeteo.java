package modules.meteo.models;

import java.time.LocalDateTime;

public class AlerteMeteo {

    private int id;
    private String titre;
    private String message;
    private String type; // "PLUIE", "GEL", "CANICULE", "VENT"
    private LocalDateTime datePrevue;
    private LocalDateTime dateCreation;
    private boolean lue;
    private String niveau; // "INFO", "WARNING", "DANGER"

    public AlerteMeteo() {
        this.dateCreation = LocalDateTime.now();
        this.lue = false;
    }

    public AlerteMeteo(String titre, String message, String type,
                       LocalDateTime datePrevue, String niveau) {
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

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getCouleur() {
        switch(niveau) {
            case "DANGER": return "#e74c3c";
            case "WARNING": return "#f39c12";
            default: return "#3498db";
        }
    }

    public String getIcone() {
        switch(type) {
            case "PLUIE": return "🌧️";
            case "GEL": return "❄️";
            case "CANICULE": return "☀️";
            case "VENT": return "💨";
            default: return "🌤️";
        }
    }
}
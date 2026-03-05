package modules.sentiment.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnalyseSentiment {

    private int id;
    private int reviewId;
    private String commentaire;
    private double scorePositif;
    private double scoreNegatif;
    private double scoreNeutre;
    private String sentiment; // "POSITIF", "NEGATIF", "NEUTRE", "WARNING"
    private List<String> motsClesNegatifs;
    private boolean warning;
    private LocalDateTime dateAnalyse;

    public AnalyseSentiment() {
        this.dateAnalyse = LocalDateTime.now();
        this.motsClesNegatifs = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public double getScorePositif() { return scorePositif; }
    public void setScorePositif(double scorePositif) { this.scorePositif = scorePositif; }

    public double getScoreNegatif() { return scoreNegatif; }
    public void setScoreNegatif(double scoreNegatif) { this.scoreNegatif = scoreNegatif; }

    public double getScoreNeutre() { return scoreNeutre; }
    public void setScoreNeutre(double scoreNeutre) { this.scoreNeutre = scoreNeutre; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public List<String> getMotsClesNegatifs() { return motsClesNegatifs; }
    public void setMotsClesNegatifs(List<String> motsClesNegatifs) { this.motsClesNegatifs = motsClesNegatifs; }

    public boolean isWarning() { return warning; }
    public void setWarning(boolean warning) { this.warning = warning; }

    public LocalDateTime getDateAnalyse() { return dateAnalyse; }
    public void setDateAnalyse(LocalDateTime dateAnalyse) { this.dateAnalyse = dateAnalyse; }

    public String getCouleur() {
        if (warning) return "#e74c3c";
        switch(sentiment) {
            case "POSITIF": return "#27ae60";
            case "NEGATIF": return "#e67e22";
            default: return "#3498db";
        }
    }

    public String getIcone() {
        if (warning) return "⚠️";
        switch(sentiment) {
            case "POSITIF": return "😊";
            case "NEGATIF": return "😞";
            default: return "😐";
        }
    }
}
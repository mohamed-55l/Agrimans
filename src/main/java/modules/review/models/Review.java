package modules.review.models;  // ← CHANGEMENT 1

import java.sql.Date;           // ← OK (garde)
import modules.equipement.models.Equipement;
// import java.time.LocalDate;  // ← À SUPPRIMER (pas utilisé)


public class Review {
    private int id;
    private String commentaire;
    private float note;  // C'est float dans votre code
    private Date dateReview;  // SQL Date
    private int equipementId;
    private Equipement equipement;  // Pour la jointure

    // Constructeurs
    public Review() {}

    public Review(int id, String commentaire, float note, Date dateReview, int equipementId) {
        this.id = id;
        this.commentaire = commentaire;
        this.note = note;
        this.dateReview = dateReview;
        this.equipementId = equipementId;
    }

    public Review(String commentaire, float note, Date dateReview, int equipementId) {
        this.commentaire = commentaire;
        this.note = note;
        this.dateReview = dateReview;
        this.equipementId = equipementId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public float getNote() {
        return note;
    }

    public void setNote(float note) {
        this.note = note;
    }

    public Date getDateReview() {
        return dateReview;
    }

    public void setDateReview(Date dateReview) {
        this.dateReview = dateReview;
    }

    public int getEquipementId() {
        return equipementId;
    }

    public void setEquipementId(int equipementId) {
        this.equipementId = equipementId;
    }

    public Equipement getEquipement() {
        return equipement;
    }

    public void setEquipement(Equipement equipement) {
        this.equipement = equipement;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", commentaire='" + commentaire + '\'' +
                ", note=" + note +
                ", dateReview=" + dateReview +
                ", equipementId=" + equipementId +
                '}';
    }
}
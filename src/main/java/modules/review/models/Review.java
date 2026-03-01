package modules.review.models;

import modules.equipement.models.Equipement;

import java.sql.Date;

/**
 * Modèle représentant une review (avis) sur un équipement
 *
 * RÔLE: Structure de données pour un avis
 * Correspond à la table 'review' dans la base de données
 */
public class Review {

    // =====================================================
    // ATTRIBUTS
    // =====================================================

    private int id;                 // Identifiant unique
    private String commentaire;      // Texte de l'avis
    private float note;              // Note sur 5
    private Date dateReview;         // Date de l'avis
    private int equipementId;        // ID de l'équipement concerné
    private Equipement equipement;   // Objet équipement complet (pour jointure)
    private int userId;              // ID de l'utilisateur qui a posté l'avis

    // =====================================================
    // CONSTRUCTEURS
    // =====================================================

    /**
     * Constructeur vide (nécessaire pour JavaFX)
     */
    public Review() {}

    /**
     * Constructeur complet
     */
    public Review(int id, String commentaire, float note, Date dateReview,
                  int equipementId, int userId) {
        this.id = id;
        this.commentaire = commentaire;
        this.note = note;
        this.dateReview = dateReview;
        this.equipementId = equipementId;
        this.userId = userId;
    }

    /**
     * Constructeur sans ID (pour création)
     */
    public Review(String commentaire, float note, Date dateReview,
                  int equipementId, int userId) {
        this.commentaire = commentaire;
        this.note = note;
        this.dateReview = dateReview;
        this.equipementId = equipementId;
        this.userId = userId;
    }

    // =====================================================
    // GETTERS ET SETTERS
    // =====================================================

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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // =====================================================
    // MÉTHODES UTILITAIRES
    // =====================================================

    /**
     * Représentation textuelle
     */
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", note=" + note +
                ", equipementId=" + equipementId +
                '}';
    }
}
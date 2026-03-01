package modules.equipement.models;

/**
 * Modèle représentant un équipement agricole
 *
 * RÔLE: Structure de données pour un équipement
 * Correspond à la table 'equipement' dans la base de données
 */
public class Equipement {

    // =====================================================
    // ATTRIBUTS - Correspondent aux colonnes de la table
    // =====================================================

    private int id;                 // Identifiant unique (PK)
    private String nom;              // Nom de l'équipement
    private String type;             // Type (Tracteur, Semoir, etc.)
    private float prix;              // Prix d'achat
    private String disponibilite;    // État: Disponible, Non disponible, En maintenance
    private int userId;              // ID de l'utilisateur qui possède cet équipement

    // =====================================================
    // CONSTRUCTEURS
    // =====================================================

    /**
     * Constructeur vide (nécessaire pour JavaFX)
     */
    public Equipement() {}

    /**
     * Constructeur complet avec tous les champs
     * @param id Identifiant
     * @param nom Nom
     * @param type Type
     * @param prix Prix
     * @param disponibilite État
     * @param userId ID du propriétaire
     */
    public Equipement(int id, String nom, String type, float prix,
                      String disponibilite, int userId) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.prix = prix;
        this.disponibilite = disponibilite;
        this.userId = userId;
    }

    /**
     * Constructeur sans ID (pour création)
     * @param nom Nom
     * @param type Type
     * @param prix Prix
     * @param disponibilite État
     * @param userId ID du propriétaire
     */
    public Equipement(String nom, String type, float prix,
                      String disponibilite, int userId) {
        this.nom = nom;
        this.type = type;
        this.prix = prix;
        this.disponibilite = disponibilite;
        this.userId = userId;
    }

    /**
     * Constructeur simplifié (pour tests)
     */
    public Equipement(String nom, String type, float prix, String disponibilite) {
        this.nom = nom;
        this.type = type;
        this.prix = prix;
        this.disponibilite = disponibilite;
        this.userId = 1; // Valeur par défaut
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public String getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
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
     * Vérifie si l'équipement est disponible
     */
    public boolean isDisponible() {
        return "Disponible".equals(disponibilite);
    }

    /**
     * Vérifie si l'équipement est en panne
     */
    public boolean isEnPanne() {
        return "Non disponible".equals(disponibilite);
    }

    /**
     * Vérifie si l'équipement est en maintenance
     */
    public boolean isEnMaintenance() {
        return "En maintenance".equals(disponibilite);
    }

    /**
     * Représentation textuelle (pour ComboBox)
     */
    @Override
    public String toString() {
        return nom + " (" + type + ") - " + disponibilite;
    }
}
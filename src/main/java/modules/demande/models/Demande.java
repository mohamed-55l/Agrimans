package modules.demande.models;

import modules.equipement.models.Equipement;
import modules.user.models.User;
import java.time.LocalDateTime;

public class Demande {

    private int id;
    private User agriculteur;
    private Equipement equipement;           // Si équipement existant
    private String nomEquipement;             // Si équipement saisi
    private String typeDemande;                // "EQUIPEMENT_EXISTANT" ou "EQUIPEMENT_NOUVEAU"
    private String description;
    private int quantite;
    private LocalDateTime dateDemande;
    private String statut;                     // EN_ATTENTE, ACCEPTE, REFUSE
    private String reponseChef;
    private LocalDateTime dateTraitement;

    // Constructeurs
    public Demande() {}

    public Demande(int id, User agriculteur, String description, int quantite) {
        this.id = id;
        this.agriculteur = agriculteur;
        this.description = description;
        this.quantite = quantite;
        this.dateDemande = LocalDateTime.now();
        this.statut = "EN_ATTENTE";
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getAgriculteur() { return agriculteur; }
    public void setAgriculteur(User agriculteur) { this.agriculteur = agriculteur; }

    public Equipement getEquipement() { return equipement; }
    public void setEquipement(Equipement equipement) {
        this.equipement = equipement;
        if (equipement != null) {
            this.nomEquipement = equipement.getNom();
        }
    }

    public String getNomEquipement() { return nomEquipement; }
    public void setNomEquipement(String nomEquipement) { this.nomEquipement = nomEquipement; }

    public String getTypeDemande() { return typeDemande; }
    public void setTypeDemande(String typeDemande) { this.typeDemande = typeDemande; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getReponseChef() { return reponseChef; }
    public void setReponseChef(String reponseChef) { this.reponseChef = reponseChef; }

    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }


    // Méthodes utilitaires
    public String getStatutLibelle() {
        switch(statut) {
            case "EN_ATTENTE": return "⏳ En attente";
            case "ACCEPTE": return "✅ Accepté";
            case "REFUSE": return "❌ Refusé";
            default: return statut;
        }
    }

    public String getStatutCouleur() {
        switch(statut) {
            case "EN_ATTENTE": return "#f39c12";
            case "ACCEPTE": return "#27ae60";
            case "REFUSE": return "#e74c3c";
            default: return "#666";
        }
    }

    public String getNomEquipementAffiche() {
        if (equipement != null) {
            return equipement.getNom() + " (" + equipement.getType() + ")";
        }
        return nomEquipement + " (à acheter)";
    }
}
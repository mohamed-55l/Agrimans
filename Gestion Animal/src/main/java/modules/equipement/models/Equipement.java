package modules.equipement.models;

import javafx.beans.value.ObservableValue;

public class Equipement  {

    private int id;
    private String nom;
    private String type;
    private float prix;
    private String disponibilite;
    private int userId;        // 👈 Pour la base de données

    // Constructeurs
    public Equipement() {}

    public Equipement(int id, String nom, String type, float prix,
                      String disponibilite, int userId) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.prix = prix;
        this.disponibilite = disponibilite;
        this.userId = userId;
    }

    public Equipement(String nom,
                      String type, float prix,
                      String disponibilite) {

        this.nom = nom;
        this.type = type;
        this.prix = prix;
        this.disponibilite = disponibilite;
    }

    // Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public float getPrix() { return prix; }
    public void setPrix(float prix) { this.prix = prix; }

    public String getDisponibilite() { return disponibilite; }
    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }





}

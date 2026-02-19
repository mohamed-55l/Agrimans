package org.example.parcelle_culture.entities;

public class Parcelle {

    private int idParcelle;
    private String nom;
    private double superficie;
    private String localisation;
    private String typeSol;
    private int utilisateurId;

    // Constructor without ID (for insert)
    public Parcelle(String nom, double superficie, String localisation, String typeSol, int utilisateurId) {
        this.nom = nom;
        this.superficie = superficie;
        this.localisation = localisation;
        this.typeSol = typeSol;
        this.utilisateurId = utilisateurId;
    }

    // Constructor with ID (for select)
    public Parcelle(int idParcelle, String nom, double superficie, String localisation, String typeSol, int utilisateurId) {
        this.idParcelle = idParcelle;
        this.nom = nom;
        this.superficie = superficie;
        this.localisation = localisation;
        this.typeSol = typeSol;
        this.utilisateurId = utilisateurId;
    }

    // Getters and Setters

    public int getIdParcelle() {
        return idParcelle;
    }

    public void setIdParcelle(int idParcelle) {
        this.idParcelle = idParcelle;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(double superficie) {
        this.superficie = superficie;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getTypeSol() {
        return typeSol;
    }

    public void setTypeSol(String typeSol) {
        this.typeSol = typeSol;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }


    @Override
    public String toString() {
        return nom;
    }

}

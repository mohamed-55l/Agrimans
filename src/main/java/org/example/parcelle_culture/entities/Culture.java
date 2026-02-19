package org.example.parcelle_culture.entities;

import java.time.LocalDate;

public class Culture {

    private int idCulture;
    private String nom;
    private String typeCulture;
    private LocalDate datePlantation;
    private LocalDate dateRecoltePrevue;
    private String etatCulture;
    private int parcelleId;

    // Constructor without ID (for insert)
    public Culture(String nom, String typeCulture,
                   LocalDate datePlantation,
                   LocalDate dateRecoltePrevue,
                   String etatCulture,
                   int parcelleId) {

        this.nom = nom;
        this.typeCulture = typeCulture;
        this.datePlantation = datePlantation;
        this.dateRecoltePrevue = dateRecoltePrevue;
        this.etatCulture = etatCulture;
        this.parcelleId = parcelleId;
    }

    public Culture(String nom, String typeCulture, LocalDate datePlantation, LocalDate dateRecoltePrevue, String etatCulture) {
        this.nom = nom;
        this.typeCulture = typeCulture;
        this.datePlantation = datePlantation;
        this.dateRecoltePrevue = dateRecoltePrevue;
        this.etatCulture = etatCulture;
    }

    // Constructor with ID (for select)
    public Culture(int idCulture, String nom, String typeCulture,
                   LocalDate datePlantation,
                   LocalDate dateRecoltePrevue,
                   String etatCulture,
                   int parcelleId) {

        this.idCulture = idCulture;
        this.nom = nom;
        this.typeCulture = typeCulture;
        this.datePlantation = datePlantation;
        this.dateRecoltePrevue = dateRecoltePrevue;
        this.etatCulture = etatCulture;
        this.parcelleId = parcelleId;
    }

    // Getters and Setters

    public int getIdCulture() {
        return idCulture;
    }

    public void setIdCulture(int idCulture) {
        this.idCulture = idCulture;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTypeCulture() {
        return typeCulture;
    }

    public void setTypeCulture(String typeCulture) {
        this.typeCulture = typeCulture;
    }

    public LocalDate getDatePlantation() {
        return datePlantation;
    }

    public void setDatePlantation(LocalDate datePlantation) {
        this.datePlantation = datePlantation;
    }

    public LocalDate getDateRecoltePrevue() {
        return dateRecoltePrevue;
    }

    public void setDateRecoltePrevue(LocalDate dateRecoltePrevue) {
        this.dateRecoltePrevue = dateRecoltePrevue;
    }

    public String getEtatCulture() {
        return etatCulture;
    }

    public void setEtatCulture(String etatCulture) {
        this.etatCulture = etatCulture;
    }

    public int getParcelleId() {
        return parcelleId;
    }

    public void setParcelleId(int parcelleId) {
        this.parcelleId = parcelleId;
    }

    @Override
    public String toString() {
        return "Culture{" +
                "id=" + idCulture +
                ", nom='" + nom + '\'' +
                ", type='" + typeCulture + '\'' +
                ", datePlantation=" + datePlantation +
                ", dateRecoltePrevue=" + dateRecoltePrevue +
                ", etat='" + etatCulture + '\'' +
                '}';
    }

}

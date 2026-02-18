package models;

public class Review {

    private int id;
    private int idEquipement;
    private int note;
    private String commentaire;

    public Review() {}

    public Review(int id, int idEquipement,
                  int note, String commentaire) {
        this.id = id;
        this.idEquipement = idEquipement;
        this.note = note;
        this.commentaire = commentaire;
    }

    // Getters & Setters
}

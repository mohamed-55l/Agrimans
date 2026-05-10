package modules.animal.models;

public class Animal {
    private int id;
    private String nom;
    private String espece;
    private String race;
    private float poids;
    private String etatSante;
    private int userId;

    public Animal() {
    }

    public Animal(int id, String nom, String espece, String race, float poids, String etatSante, int userId) {
        this.id = id;
        this.nom = nom;
        this.espece = espece;
        this.race = race;
        this.poids = poids;
        this.etatSante = etatSante;
        this.userId = userId;
    }

    public Animal(String nom, String espece, String race, float poids, String etatSante) {
        this.nom = nom;
        this.espece = espece;
        this.race = race;
        this.poids = poids;
        this.etatSante = etatSante;
    }
    
    public Animal(String nom, String espece, String race, float poids, String etatSante, int userId) {
        this.nom = nom;
        this.espece = espece;
        this.race = race;
        this.poids = poids;
        this.etatSante = etatSante;
        this.userId = userId;
    }

    // Getters and Setters
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

    public String getEspece() {
        return espece;
    }

    public void setEspece(String espece) {
        this.espece = espece;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public float getPoids() {
        return poids;
    }

    public void setPoids(float poids) {
        this.poids = poids;
    }

    public String getEtatSante() {
        return etatSante;
    }

    public void setEtatSante(String etatSante) {
        this.etatSante = etatSante;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "Animal{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", espece='" + espece + '\'' +
                ", race='" + race + '\'' +
                ", poids=" + poids +
                ", etatSante='" + etatSante + '\'' +
                ", userId=" + userId +
                '}';
    }
}

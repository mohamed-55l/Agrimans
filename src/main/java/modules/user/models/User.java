package modules.user.models;

public class User {

    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String passwordHash;
    private String createdAt;

    public User(int id, String fullName, String email, String phone, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Optionnel : constructeur complet
    public User(int id, String fullName, String email, String phone, String role, String passwordHash, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public User() {

    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
    public String getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPrenom() {
        return fullName;
    }

    public String getNom() {
        return fullName;
    }

    public void setNom(String nomComplet) {
        this.fullName = nomComplet;
    }

    public void setPrenom(String s) {
        this.fullName = s;
    }


}
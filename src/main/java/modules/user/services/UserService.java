package modules.user.services;

import modules.user.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static List<User> users = new ArrayList<>();

    static {
        // Créer des utilisateurs de test avec des rôles bien définis
        users.add(new User(1, "Admin", "Chef", "admin@agrimans.com", "ADMIN"));
        users.add(new User(2, "Jean", "Agriculteur", "jean@agrimans.com", "AGRICULTEUR"));
        users.add(new User(3, "Marie", "Cultivatrice", "marie@agrimans.com", "AGRICULTEUR"));
        users.add(new User(4, "Paul", "Éleveur", "paul@agrimans.com", "AGRICULTEUR"));

        System.out.println("👥 Utilisateurs de test chargés:");
        for (User u : users) {
            System.out.println("   - " + u.getEmail() + " (" + u.getRole() + ")");
        }
    }

    /**
     * Authentifier un utilisateur
     */
    public User login(String email, String password) {
        System.out.println("🔍 Recherche de l'utilisateur: " + email);

        // Simulation: mot de passe = "123" pour tous
        if (!"123".equals(password)) {
            System.out.println("❌ Mot de passe incorrect");
            return null;
        }

        for (User user : users) {
            if (user.getEmail().equals(email)) {
                System.out.println("✅ Utilisateur trouvé: " + user.getRole());
                return user;
            }
        }

        System.out.println("❌ Utilisateur non trouvé");
        return null;
    }

    /**
     * Récupérer tous les agriculteurs
     */
    public List<User> getAgriculteurs() {
        List<User> agriculteurs = new ArrayList<>();
        for (User user : users) {
            if ("AGRICULTEUR".equals(user.getRole())) {
                agriculteurs.add(user);
            }
        }
        return agriculteurs;
    }

    /**
     * Compter les agriculteurs
     */
    public int countAgriculteurs() {
        return getAgriculteurs().size();
    }
}
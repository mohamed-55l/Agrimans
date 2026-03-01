package core.session;

import modules.user.models.User;

/**
 * Gestionnaire de session utilisateur
 *
 * RÔLE: Garder en mémoire l'utilisateur connecté et son rôle
 * Pattern: Singleton
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Connecter un utilisateur
     */
    public static void login(User user) {
        getInstance().currentUser = user;
        System.out.println("✅ Connexion: " + user.getEmail() + " (" + user.getRole() + ")");
    }

    /**
     * Déconnecter
     */
    public static void logout() {
        getInstance().currentUser = null;
        System.out.println("✅ Déconnexion");
    }

    /**
     * Récupérer l'utilisateur connecté
     */
    public static User getCurrentUser() {
        return getInstance().currentUser;
    }

    /**
     * Vérifier si l'utilisateur est ADMIN
     */
    public static boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && "ADMIN".equals(user.getRole());
    }

    /**
     * Vérifier si l'utilisateur est AGRICULTEUR
     */
    public static boolean isAgriculteur() {
        User user = getCurrentUser();
        return user != null && "AGRICULTEUR".equals(user.getRole());
    }

    /**
     * Récupérer l'ID de l'utilisateur connecté
     */
    public static int getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : -1;
    }

    /**
     * Récupérer le nom complet
     */
    public static String getCurrentUserName() {
        User user = getCurrentUser();
        return user != null ? user.getPrenom() + " " + user.getNom() : "Inconnu";
    }
}
package core.session;

import modules.user.models.User;

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
        System.out.println("✅ CONNEXION RÉUSSIE: " + user.getEmail() + " (" + user.getRole() + ")");
        System.out.println("   ID: " + user.getId());
        System.out.println("   Nom: " + user.getPrenom() + " " + user.getNom());
    }

    /**
     * Déconnecter
     */
    public static void logout() {
        System.out.println("👋 Déconnexion de: " + getCurrentUserName());
        getInstance().currentUser = null;
    }

    /**
     * Récupérer l'utilisateur connecté
     */
    public static User getCurrentUser() {
        User user = getInstance().currentUser;
        if (user == null) {
            System.out.println("⚠️ Aucun utilisateur connecté");
        }
        return user;
    }

    /**
     * Vérifier si l'utilisateur est ADMIN
     */
    public static boolean isAdmin() {
        User user = getCurrentUser();
        boolean result = user != null && "ADMIN".equals(user.getRole());
        System.out.println("🔍 Vérification rôle ADMIN: " + result);
        return result;
    }

    /**
     * Vérifier si l'utilisateur est AGRICULTEUR
     */
    public static boolean isAgriculteur() {
        User user = getCurrentUser();
        boolean result = user != null && "AGRICULTEUR".equals(user.getRole());
        System.out.println("🔍 Vérification rôle AGRICULTEUR: " + result);
        return result;
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
        return user != null ? user.getPrenom() + " " + user.getNom() : "Non connecté";
    }
}
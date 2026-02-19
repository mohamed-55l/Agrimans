package org.example.model;

public class Session {
    private static user currentUser;

    public static user getCurrentUser() {
        if (currentUser == null) {
            System.out.println("⚠️ Warning: No user is currently logged in!");
        } else {
            System.out.println("Current session user: " + currentUser.getName() + " (ID: " + currentUser.getId() + ")");
        }
        return currentUser;
    }

    public static void setCurrentUser(user user) {
        currentUser = user;
        System.out.println("✅ User logged in: " + user.getName() + " (ID: " + user.getId() + ")");
    }

    public static void clear() {
        System.out.println("👋 User logged out: " + (currentUser != null ? currentUser.getName() : "unknown"));
        currentUser = null;
    }
}
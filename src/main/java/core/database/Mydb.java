package core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydb {

    private static Mydb instance;
    private Connection cnx;
    private final String URL = "jdbc:mysql://localhost:3306/Agrimans";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Mydb() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion réussie à la base de données");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL non trouvé: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Erreur de connexion: " + e.getMessage());
        }
    }

    public static Mydb getInstance() {
        if (instance == null) {
            instance = new Mydb();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Vérifie si la connexion est valide
     */
    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Ferme la connexion
     */
    public void closeConnection() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
                System.out.println("🔌 Connexion fermée");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la fermeture: " + e.getMessage());
        }
    }
}
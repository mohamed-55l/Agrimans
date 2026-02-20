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
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion réussie à la base de données");
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
}
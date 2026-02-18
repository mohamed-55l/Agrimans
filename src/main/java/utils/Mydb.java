package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydb {

    // 🔗 Paramètres de connexion
    private final String URL =
            "jdbc:mysql://localhost:3306/Agrimans?useSSL=false&serverTimezone=UTC";

    private final String USER = "root";
    private final String PASSWORD = "";

    private Connection cnx;

    // 🧠 Singleton instance
    private static Mydb instance;

    // 🔒 Constructeur privé
    private Mydb() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to database Agrimans successfully");
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed");
            System.out.println(e.getMessage());
        }
    }

    // 📌 Méthode pour créer une seule instance
    public static Mydb getInstance() {
        if (instance == null) {
            instance = new Mydb();
        }
        return instance;
    }

    // 📌 Retourner la connexion
    public Connection getCnx() {
        return cnx;
    }
}

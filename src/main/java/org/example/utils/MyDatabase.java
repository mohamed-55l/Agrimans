package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private static MyDatabase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/agrimans?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDatabase() {
        try {
            System.out.println("Loading MySQL driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL Driver loaded successfully");

            System.out.println("Attempting to connect to database...");
            System.out.println("URL: " + URL);
            System.out.println("User: " + USER);

            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully to: agrimans");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
            System.out.println("Make sure mysql-connector-java is in your dependencies");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Connection failed!");
            System.out.println("Error Message: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Connection is closed, reconnecting...");
                instance = new MyDatabase();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
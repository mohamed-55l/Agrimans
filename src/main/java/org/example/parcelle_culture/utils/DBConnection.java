package org.example.parcelle_culture.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    final String URL = "jdbc:mysql://localhost:3306/smartfarm";
    final String USER = "root";
    final String PASSWORD = "";

    private Connection connection;
    private static DBConnection instance;

    public DBConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static DBConnection getInstance() {
        if(instance == null){
            instance =  new DBConnection();
        }
        return instance;
    }
    public Connection getConnection() {
        return connection;
    }
}

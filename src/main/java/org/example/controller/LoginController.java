package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.Session;
import org.example.model.user;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Email and password are required!", Alert.AlertType.ERROR);
            return;
        }

        user user = userDAO.login(email, password);

        if (user != null) {
            System.out.println("=== Login Success ===");
            System.out.println("User ID: " + user.getId());
            System.out.println("User Name: " + user.getName());
            System.out.println("User Role: '" + user.getRole() + "'");

            Session.setCurrentUser(user);

            String welcomeMessage;
            switch (user.getRole()) {
                case "AGRIMAN":
                    welcomeMessage = "Welcome back, Agriman " + user.getName() + "! 🌾 You can buy and sell.";
                    break;
                case "SELLER":
                    welcomeMessage = "Welcome back, Seller " + user.getName() + "! 📦 Ready to sell your products?";
                    break;
                default:
                    welcomeMessage = "Welcome back, " + user.getName() + "! 🛒 Start shopping!";
                    break;
            }

            showAlert("Success", welcomeMessage, Alert.AlertType.INFORMATION);
            goToProducts();
        } else {
            showAlert("Error", "Invalid email or password!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agrimans Marketplace - Register");
        } catch (IOException e) {
            showAlert("Error", "Could not load registration page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void goToProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/products.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agrimans Marketplace - Products");
        } catch (IOException e) {
            showAlert("Error", "Could not load products page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
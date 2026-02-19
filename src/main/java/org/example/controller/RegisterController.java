package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.user;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private RadioButton buyerRadio;

    @FXML
    private RadioButton sellerRadio;

    @FXML
    private RadioButton agrimanRadio;

    @FXML
    private ToggleGroup roleGroup;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        System.out.println("=== Register Controller Initialized ===");
        agrimanRadio.setSelected(true);
    }

    @FXML
    public void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match!", Alert.AlertType.ERROR);
            return;
        }

        if (userDAO.emailExists(email)) {
            showAlert("Error", "Email already exists!", Alert.AlertType.ERROR);
            return;
        }

        String role = getSelectedRole();
        System.out.println("Selected role: " + role);

        user newUser = new user(name, email, password, role);

        boolean success = userDAO.register(newUser);

        if (success) {
            showAlert("Success",
                    "Registration successful! You can now login as a " + role + ".",
                    Alert.AlertType.INFORMATION);
            goToLogin();
        } else {
            showAlert("Error", "Registration failed. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private String getSelectedRole() {
        if (buyerRadio.isSelected()) {
            return "BUYER";
        } else if (sellerRadio.isSelected()) {
            return "SELLER";
        } else {
            return "AGRIMAN";
        }
    }

    @FXML
    public void handleBackToLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agrimans Marketplace - Login");
        } catch (IOException e) {
            showAlert("Error", "Could not load login page: " + e.getMessage(), Alert.AlertType.ERROR);
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
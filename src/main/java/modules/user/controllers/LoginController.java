package modules.user.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import core.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import modules.user.models.User;
import modules.user.services.UserService;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private Label lblError;

    private UserService userService = new UserService();

    @FXML
    private void handleLogin() throws SQLException {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();

        System.out.println("🔐 Tentative de connexion: " + email);

        // Validation de base
        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs");
            lblError.setVisible(true);
            return;
        }

        // Tentative d'authentification
        User user = userService.login(email, password);

        if (user == null) {
            System.out.println("❌ Échec de connexion pour: " + email);
            lblError.setText("Email ou mot de passe incorrect");
            lblError.setVisible(true);
            return;
        }

        // Connexion réussie
        System.out.println("✅ Connexion réussie pour: " + user.getEmail());
        SessionManager.login(user);

        // Redirection selon le rôle
        try {
            String fxmlPath;
            if (user.isAdmin()) {
                fxmlPath = "/fxml/layout/admin_layout.fxml";
                System.out.println("➡️ Redirection vers interface ADMIN");
            } else {
                fxmlPath = "/fxml/layout/user_layout.fxml";
                System.out.println("➡️ Redirection vers interface USER");
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger l'interface: " + e.getMessage());
        }
    }
}
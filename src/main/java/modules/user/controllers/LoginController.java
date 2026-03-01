package modules.user.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import modules.user.models.User;
import modules.user.services.UserService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur pour la page de connexion
 */
public class LoginController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private javafx.scene.control.Label lblError;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Effacer les messages d'erreur au démarrage
        lblError.setVisible(false);

        // Optionnel: Ajouter des valeurs par défaut pour le développement
        // tfEmail.setText("admin@example.com");
        // pfPassword.setText("admin123");
    }

    @FXML
    private void handleLogin() {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText().trim();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            // Tentative d'authentification
            User user = userService.login(email, password);

            if (user != null) {
                // Connexion réussie - Utilisation de la méthode statique login()
                SessionManager.login(user);
                handleSuccessfulLogin(user);
            } else {
                // Échec de connexion
                showError("Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de connexion à la base de données");
        }
    }

    /**
     * Gère la redirection après une connexion réussie
     */
    private void handleSuccessfulLogin(User user) {
        try {
            // Déterminer la vue en fonction du rôle
            String fxmlPath;
            String title;

            if (user.isAdmin()) {
                fxmlPath = "/fxml/admin/Dashboard.fxml"; // À adapter selon votre structure
                title = "Tableau de bord Administrateur";
            } else {
                fxmlPath = "/fxml/agriculteur/Dashboard.fxml"; // À adapter selon votre structure
                title = "Tableau de bord Agriculteur";
            }

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            // Vérifier si le fichier FXML existe
            if (getClass().getResource(fxmlPath) == null) {
                showError("Fichier interface non trouvé: " + fxmlPath);
                return;
            }

            Parent root = loader.load();

            // Récupérer la stage actuelle
            Stage stage = (Stage) tfEmail.getScene().getWindow();

            // Changer la scène
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

            // Afficher un message de bienvenue
            AlertUtils.showInfo("Connexion réussie", "Bienvenue " + user.getPrenom() + " !");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'interface: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            String fxmlPath = "/fxml/user/ForgotPassword.fxml"; // À adapter

            if (getClass().getResource(fxmlPath) == null) {
                AlertUtils.showError("Erreur", "Page de récupération non disponible");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Récupération de mot de passe");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la page de récupération");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            String fxmlPath = "/fxml/user/Register.fxml"; // À adapter

            if (getClass().getResource(fxmlPath) == null) {
                AlertUtils.showError("Erreur", "Page d'inscription non disponible");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la page d'inscription");
        }
    }

    /**
     * Affiche un message d'erreur dans le label
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    @FXML
    private void clearError() {
        lblError.setVisible(false);
    }

    /**
     * Gestionnaire pour la touche Entrée (à lier dans le FXML)
     */
    @FXML
    private void onEnterPressed() {
        handleLogin();
    }

    /**
     * Réinitialiser les champs du formulaire
     */
    @FXML
    private void resetForm() {
        tfEmail.clear();
        pfPassword.clear();
        lblError.setVisible(false);
    }
}
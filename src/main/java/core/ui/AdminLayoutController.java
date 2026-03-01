package core.ui;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Contrôleur pour le layout ADMIN
 *
 * RÔLE: Menu de navigation pour l'administrateur
 */
public class AdminLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUserInfo;

    @FXML
    public void initialize() {
        // Afficher les infos de l'utilisateur
        lblUserInfo.setText(SessionManager.getCurrentUserName() + " (Admin)");

        // Charger le dashboard par défaut
        try {
            chargerPage("/fxml/dashboard/admin_dashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goDashboard() {
        try {
            chargerPage("/fxml/dashboard/admin_dashboard.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le dashboard");
        }
    }

    @FXML
    private void goEquipements() {
        try {
            chargerPage("/fxml/equipement/equipement.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la gestion des équipements");
        }
    }

    @FXML
    private void goReviews() {
        try {
            chargerPage("/fxml/review/review.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la gestion des reviews");
        }
    }

    @FXML
    private void logout() {
        if (AlertUtils.showConfirmation("Déconnexion", "Voulez-vous vraiment vous déconnecter ?")) {
            SessionManager.logout();

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                contentArea.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void chargerPage(String fxml) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource(fxml));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }
}
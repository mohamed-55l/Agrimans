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
 * Contrôleur pour le layout USER
 *
 * RÔLE: Menu de navigation pour l'agriculteur
 */
public class UserLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUserInfo;

    @FXML
    public void initialize() {
        // Afficher les infos de l'utilisateur
        lblUserInfo.setText(SessionManager.getCurrentUserName());

        // Charger le dashboard par défaut
        try {
            chargerPage("/fxml/dashboard/user_dashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goDashboard() {
        try {
            chargerPage("/fxml/dashboard/user_dashboard.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le dashboard");
        }
    }

    @FXML
    private void goMesEquipements() {
        try {
            chargerPage("/fxml/equipement/user_equipement.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger mes équipements");
        }
    }

    @FXML
    private void goAjouterReview() {
        try {
            chargerPage("/fxml/review/user_review.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le formulaire");
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
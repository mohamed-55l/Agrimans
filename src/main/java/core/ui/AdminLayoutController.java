package core.ui;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUserInfo;

    @FXML
    public void initialize() {
        lblUserInfo.setText(SessionManager.getCurrentUserName() + " (Admin)");

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
    private void goDemandes() {
        try {
            chargerPage("/fxml/demande/admin_demandes.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la gestion des demandes");
        }
    }

    @FXML
    private void goUtilisateurs() {
        try {
            chargerPage("/fxml/user/user_management.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la gestion des utilisateurs");
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

    @FXML
    void handleButtonEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #7DBF6C; -fx-text-fill: #2E3D27; -fx-font-size: 14; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-background-radius: 5;");
    }

    @FXML
    void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #4B8B3B; -fx-text-fill: #F0E8D8; -fx-font-size: 14; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-background-radius: 5;");
    }

    private void chargerPage(String fxml) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource(fxml));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }
}
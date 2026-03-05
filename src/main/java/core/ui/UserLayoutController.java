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

public class UserLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUserInfo;

    @FXML
    public void initialize() {
        lblUserInfo.setText(SessionManager.getCurrentUserName());

        try {
            // Dashboard par défaut
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
    private void goDemandes() {
        try {
            chargerPage("/fxml/demande/user_demande.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la page des demandes");
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
    private void goChatbot() {
        try {
            chargerPage("/fxml/chatbot.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le chatbot");
        }
    }


    @FXML
    private void goSentiment() {
        try {
            chargerPage("/fxml/sentiment.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger l'analyse de sentiments");
        }
    }


    @FXML
    private void goCarte() {
        try {
            chargerPage("/fxml/carte/user_garage.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la liste des garages");
        }
    }

    @FXML
    private void goMeteo() {
        try {
            chargerPage("/fxml/meteo.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la météo");
        }
    }

    private void chargerPage(String fxml) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource(fxml));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }
}
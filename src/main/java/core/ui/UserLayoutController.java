package core.ui;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    // ── MARKETPLACE (user can browse & buy) ──────────────────────────────────
    @FXML
    private void goMarketplace() {
        try {
            chargerPage("/fxml/marketplace/products.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le Marketplace : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goCart() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/marketplace/cart.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Mon Panier — Agrimans");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le panier : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        if (AlertUtils.showConfirmation("Déconnexion", "Voulez-vous vraiment vous déconnecter ?")) {
            SessionManager.logout();

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/user/login-view.fxml"));
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

    @FXML
    private void goGestionTerres() {
        try {
            chargerPage("/fxml/parcelle/GestionTerres.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la gestion des terres");
        }
    }

    @FXML
    private void goAnimaux() {
        try {
            chargerPage("/fxml/animal/user_animal.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la gestion des animaux");
        }
    }

    @FXML
    private void goProduction() {
        try {
            chargerPage("/fxml/production/production.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la production");
        }
    }

    @FXML
    private void goStock() {
        try {
            chargerPage("/fxml/stock/stock.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le stock");
        }
    }

    @FXML
    private void goVentes() {
        try {
            chargerPage("/fxml/ventes/ventes.fxml");
        } catch (IOException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les ventes");
        }
    }

    private void chargerPage(String fxml) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource(fxml));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }
}
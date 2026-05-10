package core.ui;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
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

    // ── VUE D'ENSEMBLE ───────────────────────────────────────────────────────
    @FXML private void goDashboard() {
        charger("/fxml/dashboard/admin_dashboard.fxml", "le dashboard");
    }

    @FXML private void goUtilisateurs() {
        charger("/fxml/user/admin-dashboard.fxml", "la gestion des utilisateurs");
    }

    // ── GESTION AGRICOLE ─────────────────────────────────────────────────────
    @FXML private void goEquipements() {
        charger("/fxml/equipement/equipement.fxml", "la gestion des équipements");
    }

    @FXML private void goAnimaux() {
        charger("/fxml/animal/animal.fxml", "la gestion des animaux");
    }

    @FXML private void goParcelles() {
        charger("/fxml/parcelle/GestionTerres.fxml", "la gestion des parcelles");
    }

    // ── OPÉRATIONS ───────────────────────────────────────────────────────────
    @FXML private void goDemandes() {
        charger("/fxml/demande/admin_demandes.fxml", "la gestion des demandes");
    }

    @FXML private void goReviews() {
        charger("/fxml/review/review.fxml", "la gestion des évaluations");
    }

    @FXML private void goProduction() {
        charger("/fxml/production/production.fxml", "la production");
    }

    @FXML private void goStock() {
        charger("/fxml/stock/stock.fxml", "le stock");
    }

    @FXML private void goVentes() {
        charger("/fxml/ventes/ventes.fxml", "les ventes");
    }

    @FXML private void goMarketplaceDashboard() {
        charger("/fxml/marketplace/dashboard.fxml", "le Marketplace");
    }

    // ── INTELLIGENCE & OUTILS ────────────────────────────────────────────────
    @FXML private void goMeteo() {
        charger("/fxml/meteo.fxml", "la météo");
    }

    @FXML private void goCarte() {
        charger("/fxml/carte.fxml", "la carte");
    }

    @FXML private void goChatbot() {
        charger("/fxml/chatbot.fxml", "le chatbot");
    }

    @FXML private void goSentiment() {
        charger("/fxml/sentiment.fxml", "l'analyse de sentiments");
    }

    // ── DÉCONNEXION ──────────────────────────────────────────────────────────
    @FXML private void logout() {
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

    // ── UTILITAIRES ──────────────────────────────────────────────────────────
    private void charger(String fxmlPath, String nom) {
        try {
            chargerPage(fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger " + nom + "\n" + e.getMessage());
        }
    }

    private void chargerPage(String fxml) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource(fxml));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }
}
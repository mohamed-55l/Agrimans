package modules.user.controllers;

import modules.user.models.User;
import core.database.DBConnection;
import core.session.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReclamationController extends BaseController {

    @FXML
    private TextField txtSujet;

    @FXML
    private TextArea txtMessage;

    @FXML
    private Label lblError;





    // ===================== SEND RECLAMATION =====================
    @FXML
    private void handleSend(ActionEvent event) {

        lblError.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        lblError.setText("");

        String sujet = txtSujet.getText().trim();
        String message = txtMessage.getText().trim();

        // ====== VALIDATION ======
        if (sujet.isEmpty()) {
            lblError.setText("⚠ Sujet obligatoire !");
            txtSujet.requestFocus();
            return;
        }

        if (sujet.length() < 4) {
            lblError.setText("⚠ Sujet trop court (min 4 caractères).");
            txtSujet.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            lblError.setText("⚠ Message obligatoire !");
            txtMessage.requestFocus();
            return;
        }

        if (message.length() < 10) {
            lblError.setText("⚠ Message trop court (min 10 caractères).");
            txtMessage.requestFocus();
            return;
        }

        // ====== GET CURRENT USER ID ======
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            lblError.setText("⚠ Aucun utilisateur connecté !");
            return;
        }

        int userId = user.getId();

        // ====== INSERT INTO DATABASE ======
        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                    INSERT INTO reclamations (user_id, sujet, message, status, created_at)
                    VALUES (?, ?, ?, 'EN_ATTENTE', NOW())
                    """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, sujet);
            ps.setString(3, message);

            ps.executeUpdate();

            lblError.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            lblError.setText("✔ Réclamation envoyée avec succès !");

            txtSujet.clear();
            txtMessage.clear();

        } catch (Exception e) {
            lblError.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            lblError.setText("❌ Erreur lors de l'envoi !");
            e.printStackTrace();
        }
    }

    // ===================== CANCEL =====================
    @FXML
    private void handleCancel(ActionEvent event) {
        txtSujet.clear();
        txtMessage.clear();
        lblError.setText("");
    }

    // ===================== LOGOUT =====================
    @FXML
    private void handleLogout(ActionEvent event) {

        try {
            SessionManager.clearSession();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user/login-view.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

            System.out.println("Déconnexion réussie ✔");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

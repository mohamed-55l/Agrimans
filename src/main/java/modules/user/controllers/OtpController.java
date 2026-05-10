package modules.user.controllers;

import modules.user.services.OtpService;
import core.ui.SceneSwitcher;
import core.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class OtpController extends BaseController {

    @FXML
    private TextField otpField;

    @FXML
    private Label messageLabel;

    @FXML
    private void verifyOtp(ActionEvent event) {

        try {

            if (SessionManager.getCurrentUser() == null) {
                messageLabel.setStyle("-fx-text-fill:red;");
                messageLabel.setText("Session expirée. Veuillez vous reconnecter.");
                return;
            }

            String enteredOtp = otpField.getText();

            if (enteredOtp == null || enteredOtp.trim().isEmpty()) {
                messageLabel.setStyle("-fx-text-fill:red;");
                messageLabel.setText("Veuillez entrer le code OTP.");
                return;
            }

            int userId = SessionManager.getCurrentUser().getId();

            boolean valid = OtpService.verifyOTP(userId, enteredOtp);

            if (!valid) {
                messageLabel.setStyle("-fx-text-fill:red;");
                messageLabel.setText("Code invalide, expiré ou trop de tentatives.");
                return;
            }

            messageLabel.setStyle("-fx-text-fill:green;");
            messageLabel.setText("Vérification réussie.");

            redirect(event);

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill:red;");
            messageLabel.setText("Erreur système.");
            e.printStackTrace();
        }
    }

    private void redirect(ActionEvent event) {

        String role = SessionManager.getCurrentUser().getRole();

        if ("Administrateur".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {

            SceneSwitcher.switchTo(
                    "/fxml/layout/admin_layout.fxml",
                    (Node) event.getSource()
            );

        } else {

            SceneSwitcher.switchTo(
                    "/fxml/layout/user_layout.fxml",
                    (Node) event.getSource()
            );
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {

        try {

            Node source = (Node) event.getSource();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user/login-view.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

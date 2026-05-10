package modules.user.controllers;

import modules.user.services.EmailService;
import modules.user.services.OtpService;
import core.database.DBConnection;
import core.utils.PasswordCryp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResetPasswordController extends BaseController {

    // ================= FXML =================
    @FXML private TextField emailField;
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // ================= OTP =================
    private String generatedOTP;
    private long otpTime;

    // ================= SEND OTP =================
    @FXML
    private void handleSendOTP(ActionEvent event) {

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            messageLabel.setText("Entrer votre email.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            String checkSql = "SELECT id FROM user WHERE email=?";
            PreparedStatement check = con.prepareStatement(checkSql);
            check.setString(1, email);

            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                messageLabel.setText("Email introuvable.");
                return;
            }

            generatedOTP = OtpService.generateOTP();
            otpTime = System.currentTimeMillis();

            EmailService.sendOTP(email, generatedOTP);

            messageLabel.setText("OTP envoyé avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur base de données.");
        }
    }

    // ================= RESET PASSWORD =================
    @FXML
    private void handleResetPassword(ActionEvent event) {

        String email = emailField.getText().trim();
        String otp = otpField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Tous les champs sont obligatoires.");
            return;
        }

        // OTP jamais généré
        if (generatedOTP == null) {
            messageLabel.setText("Veuillez envoyer OTP d'abord.");
            return;
        }

        // expiration OTP 3 minutes
        if (System.currentTimeMillis() - otpTime > 180000) {
            messageLabel.setText("OTP expiré.");
            return;
        }

        if (!otp.equals(generatedOTP)) {
            messageLabel.setText("OTP incorrect.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        if (newPassword.length() < 6) {
            messageLabel.setText("Mot de passe trop court.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            String hashedPassword = PasswordCryp.hashPassword(newPassword);

            String sql = "UPDATE user SET password_hash=? WHERE email=?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, hashedPassword);
            ps.setString(2, email);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                messageLabel.setText("Mot de passe modifié avec succès.");

                // reset OTP
                generatedOTP = null;

            } else {
                messageLabel.setText("Email introuvable.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur base de données.");
        }
    }

    // ================= BACK LOGIN =================
    @FXML
    private void goToLogin(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/user/login-view.fxml")
            );

            Stage stage = (Stage)((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

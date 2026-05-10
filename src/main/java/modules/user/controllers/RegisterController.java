package modules.user.controllers;


import core.database.DBConnection;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.time.temporal.ChronoUnit;
import modules.user.services.EmailService;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterController extends BaseController {
    @FXML
    public Label timerLabel;
    @FXML
    private Label errGeneral;
    @FXML
    private TextField verificationCodeField;

    private String generatedCode;
    private Timeline countdownTimeline;
    private LocalDateTime expiryTime;
    private String otpEmail; // email utilisé pour générer le code
    @FXML
    private Label errCode;
    // ================== Fields ==================
    @FXML private TextField nameField, emailField, phoneField;
    @FXML private PasswordField passwordField, confirmepasswordField;

    // ================== Error Labels ==================
    @FXML private Label errName, errEmail, errPhone, errPassword, errConfirm;




    @FXML
    private void handleRegister(ActionEvent event) {

        if (!validateForm()) return;

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String code = verificationCodeField.getText().trim();

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            // 1️⃣ Vérifier le code OTP
            String otpSql = """
            SELECT code, expiry
            FROM email_otp
            WHERE email = ?
        """;

            PreparedStatement otpPs = conn.prepareStatement(otpSql);
            otpPs.setString(1, email);
            ResultSet rs = otpPs.executeQuery();

            if (!rs.next()) {
                errCode.setText("Veuillez demander un code.");
                return;
            }

            if (!code.equals(rs.getString("code"))) {
                errCode.setText("Code incorrect.");
                return;
            }

            if (rs.getTimestamp("expiry").toLocalDateTime().isBefore(LocalDateTime.now())) {
                errCode.setText("Code expiré.");
                return;
            }

            // 2️⃣ Insérer utilisateur
            String insertUserSql = """
            INSERT INTO user (full_name, email, phone, password_hash, role)
            VALUES (?, ?, ?, ?, 'USER')
        """;

            PreparedStatement insertUser = conn.prepareStatement(insertUserSql);
            insertUser.setString(1, name);
            insertUser.setString(2, email);
            insertUser.setString(3, phone);
            insertUser.setString(4, BCrypt.hashpw(password, BCrypt.gensalt()));
            insertUser.executeUpdate();

            // 3️⃣ Supprimer OTP
            PreparedStatement deleteOtp =
                    conn.prepareStatement("DELETE FROM email_otp WHERE email = ?");
            deleteOtp.setString(1, email);
            deleteOtp.executeUpdate();

            conn.commit();

            errGeneral.setText("Inscription réussie !");
            errGeneral.setStyle("-fx-text-fill: green;");
            goToLogin(event);

        } catch (Exception e) {
            e.printStackTrace();
            errGeneral.setText("Erreur technique.");
        }
    }
    // ================== Validation ==================
    private boolean validateForm() {

        clearErrors();
        boolean valid = true;

        if (nameField.getText().trim().isEmpty()) {
            errName.setText("Nom obligatoire");
            markError(nameField);
            valid = false;
        }

        if (!emailField.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errEmail.setText("Email invalide");
            markError(emailField);
            valid = false;
        }

        if (!phoneField.getText().matches("\\d{8}")) {
            errPhone.setText("Téléphone invalide (8 chiffres)");
            markError(phoneField);
            valid = false;
        }

        if (passwordField.getText().length() < 6) {
            errPassword.setText("Mot de passe ≥ 6 caractères");
            markError(passwordField);
            valid = false;
        }

        if (!confirmepasswordField.getText()
                .equals(passwordField.getText())) {
            errConfirm.setText("Les mots de passe ne correspondent pas");
            markError(confirmepasswordField);
            valid = false;
        }

        return valid;
    }

    // ================== Helpers ==================
    private void clearErrors() {
        errName.setText("");
        errEmail.setText("");
        errPhone.setText("");
        errPassword.setText("");
        errConfirm.setText("");

        resetField(nameField);
        resetField(emailField);
        resetField(phoneField);
        resetField(passwordField);
        resetField(confirmepasswordField);
    }

    private void markError(Control field) {
        field.setStyle("""
                -fx-border-color: red;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);
    }

    private void resetField(Control field) {
        field.setStyle("""
                -fx-border-color: #d9d9d9;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);
    }

    // ================== Navigation ==================
    @FXML
    public void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user/login-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void saveOTPToDB(String email, String code, LocalDateTime expiry) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
            INSERT INTO email_otp (email, code, expiry)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                code = VALUES(code),
                expiry = VALUES(expiry)
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, code);
            ps.setTimestamp(3, Timestamp.valueOf(expiry));

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleSendCode() {

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            errEmail.setText("Email obligatoire.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errEmail.setText("Format email invalide.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // 🔎 Vérifier que l’email n’existe PAS déjà
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM user WHERE email = ?");
            check.setString(1, email);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                errEmail.setText("Cet email est déjà utilisé.");
                return;
            }

            // Générer OTP
            String generatedCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(2);

            PreparedStatement upsert = conn.prepareStatement("""
            INSERT INTO email_otp (email, code, expiry)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
            code = VALUES(code),
            expiry = VALUES(expiry)
        """);

            upsert.setString(1, email);
            upsert.setString(2, generatedCode);
            upsert.setTimestamp(3, Timestamp.valueOf(expiry));
            upsert.executeUpdate();

            EmailService.sendOTP(email, generatedCode);

            errCode.setStyle("-fx-text-fill: green;");
            errCode.setText("Code envoyé.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void startCountdown() {

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {

                    long secondsLeft = ChronoUnit.SECONDS.between(
                            LocalDateTime.now(), expiryTime);

                    if (secondsLeft <= 0) {
                        timerLabel.setText("Code expiré !");
                        countdownTimeline.stop();
                    } else {
                        timerLabel.setText("Expire dans : " + secondsLeft + " sec");
                    }
                })
        );

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
    private void markUserVerified(String email) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
            UPDATE user
            SET is_verified = TRUE
            WHERE email = ?
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isCodeValidFromDB(String email, String code) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
            SELECT verification_code, verification_expiry
            FROM user
            WHERE email = ?
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String dbCode = rs.getString("verification_code");
                LocalDateTime expiry =
                        rs.getTimestamp("verification_expiry").toLocalDateTime();

                return dbCode.equals(code)
                        && LocalDateTime.now().isBefore(expiry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}

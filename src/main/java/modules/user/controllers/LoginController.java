package modules.user.controllers;

import core.database.DBConnection;
import core.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modules.user.models.User;
import modules.user.services.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.util.Random;

public class LoginController {

    // ── Champs liés à login-view.fxml ───────────────────────────────────────
    @FXML private TextField   emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label       captchaLabel;
    @FXML private TextField   captchaField;
    @FXML private VBox        otpBox;
    @FXML private TextField   otpField;
    @FXML private Label       messageLabel;
    @FXML private Button      loginButton;

    // ── Window drag ──────────────────────────────────────────────────────────
    private double xOffset, yOffset;

    private final UserService userService = new UserService();
    private String captchaAnswer;
    private String generatedOtp;

    @FXML
    public void initialize() {
        generateCaptcha();
    }

    // ── Génération CAPTCHA ───────────────────────────────────────────────────
    private void generateCaptcha() {
        int a = new Random().nextInt(10) + 1;
        int b = new Random().nextInt(10) + 1;
        captchaAnswer = String.valueOf(a + b);
        if (captchaLabel != null) captchaLabel.setText("Combien font  " + a + " + " + b + " ?");
    }

    // ── Connexion ────────────────────────────────────────────────────────────
    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation basique
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        // Vérification CAPTCHA
        if (captchaField != null && captchaLabel != null) {
            String rep = captchaField.getText().trim();
            if (!rep.equals(captchaAnswer)) {
                showError("Réponse CAPTCHA incorrecte.");
                generateCaptcha();
                captchaField.clear();
                return;
            }
        }

        // Authentification en base
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM user WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                showError("Email ou mot de passe incorrect.");
                generateCaptcha();
                return;
            }

            String hash = rs.getString("password_hash");
            boolean passwordOk = false;

            if (hash != null) {
                // jBCrypt ne supporte pas $2y$ (PHP) → on convertit en $2a$
                String normalizedHash = hash.startsWith("$2y$")
                    ? "$2a$" + hash.substring(4)
                    : hash;

                if (normalizedHash.startsWith("$2a$") || normalizedHash.startsWith("$2b$")) {
                    try { passwordOk = BCrypt.checkpw(password, normalizedHash); }
                    catch (Exception ignored) { passwordOk = false; }
                } else {
                    // Texte clair (anciens comptes de test)
                    passwordOk = hash.equals(password);
                }
            }

            if (!passwordOk) {
                showError("Email ou mot de passe incorrect.");
                generateCaptcha();
                return;
            }

            // Utilisateur authentifié
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setRole(rs.getString("role"));

            System.out.println("✅ Connexion: " + user.getEmail() + " [" + user.getRole() + "]");
            SessionManager.login(user);

            // Redirection
            String fxmlPath = user.isAdmin()
                ? "/fxml/layout/admin_layout.fxml"
                : "/fxml/layout/user_layout.fxml";

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, user.isAdmin() ? 1366 : 1200, user.isAdmin() ? 768 : 700));
            stage.centerOnScreen();
            stage.show();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showError("Erreur technique : " + e.getMessage());
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────
    @FXML
    private void goToRegister() {
        navigateTo("/fxml/user/register-view.fxml");
    }

    @FXML
    private void goToResetPassword() {
        navigateTo("/fxml/user/reset-password-view.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de charger la page.");
        }
    }

    // ── Window controls ──────────────────────────────────────────────────────
    @FXML private void handleMinimize() {
        getStage().setIconified(true);
    }

    @FXML private void handleMaximize() {
        Stage s = getStage();
        s.setMaximized(!s.isMaximized());
    }

    @FXML private void handleClose() {
        getStage().close();
    }

    @FXML private void handleMousePressed(MouseEvent e) {
        xOffset = e.getSceneX();
        yOffset = e.getSceneY();
    }

    @FXML private void handleMouseDragged(MouseEvent e) {
        Stage s = getStage();
        s.setX(e.getScreenX() - xOffset);
        s.setY(e.getScreenY() - yOffset);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void showError(String msg) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            messageLabel.setVisible(true);
        }
    }

    private Stage getStage() {
        return (Stage) emailField.getScene().getWindow();
    }
}
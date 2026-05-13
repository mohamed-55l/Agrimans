package modules.user.controllers;

import modules.user.models.User;
import modules.user.utils.DBConnection;
import modules.user.utils.PasswordCryp;
import core.session.SessionManager;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class LoginController extends BaseController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField captchaField;
    @FXML private Label captchaLabel;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    private int captchaResult;
    private int failedAttempts = 0;
    private boolean isBlocked = false;

    @FXML
    public void initialize() {
        generateCaptcha();
        messageLabel.setText("");
        loginButton.setDisable(true);

        // Listeners to validate form in real-time
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        captchaField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        boolean valid = !emailField.getText().trim().isEmpty()
                && !passwordField.getText().isEmpty()
                && !captchaField.getText().trim().isEmpty();
        loginButton.setDisable(!valid);
    }

    private void generateCaptcha() {
        Random random = new Random();
        int a = random.nextInt(9) + 1;
        int b = random.nextInt(9) + 1;
        captchaResult = a + b;
        captchaLabel.setText("Combien font " + a + " + " + b + " ?");
    }

    private boolean validateCaptcha() {
        try {
            return Integer.parseInt(captchaField.getText()) == captchaResult;
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        if (isBlocked) {
            showError("Bloqué 30 secondes.");
            return;
        }

        if (!validateCaptcha()) {
            showError("Captcha incorrect");
            generateCaptcha();
            return;
        }

        setLoading(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                authenticate(event);
                return null;
            }
        };
        new Thread(task).start();
    }

    private void authenticate(ActionEvent event) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, password_hash, role, phone FROM user WHERE email=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, emailField.getText());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                loginFailed();
                return;
            }

            String hash = rs.getString("password_hash");

            // Check password compatibility with PasswordCryp (supports PHP $2y$)
            if (!PasswordCryp.checkPassword(passwordField.getText(), hash)) {
                loginFailed();
                return;
            }

            // Successful Login
            failedAttempts = 0;
            String role = rs.getString("role");

            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(emailField.getText());
            user.setRole(role);
            SessionManager.setCurrentUser(user);

            // Redirection based on Role
            javafx.application.Platform.runLater(() -> {
                String targetFxml;
                if ("ADMIN".equalsIgnoreCase(role)) {
                    targetFxml = "/fxml/user/admin-dashboard.fxml";
                } else {
                    targetFxml = "/fxml/layout/user_layout.fxml";
                }
                switchScene(targetFxml, (Node) event.getSource());
            });

        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> showError("Erreur système: " + e.getMessage()));
        } finally {
            javafx.application.Platform.runLater(() -> setLoading(false));
        }
    }

    private void loginFailed() {
        failedAttempts++;
        javafx.application.Platform.runLater(() -> {
            showError("Email ou mot de passe incorrect (" + failedAttempts + "/3)");
            setLoading(false);
        });
        if (failedAttempts >= 3) block();
    }

    private void block() {
        isBlocked = true;
        PauseTransition pause = new PauseTransition(Duration.seconds(30));
        pause.setOnFinished(e -> {
            isBlocked = false;
            failedAttempts = 0;
            messageLabel.setText("");
        });
        pause.play();
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Connexion..." : "Se connecter");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill:red;");
        messageLabel.setText(msg);
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        switchScene("/fxml/user/register-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void goToResetPassword(ActionEvent event) {
        switchScene("/fxml/user/reset-password-view.fxml", (Node) event.getSource());
    }
}
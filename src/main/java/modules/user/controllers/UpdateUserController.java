package modules.user.controllers;

import modules.user.models.User;
import core.database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;

public class UpdateUserController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> comboRole;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label errName;
    @FXML private Label errEmail;
    @FXML private Label errPhone;
    @FXML private Label errRole;
    @FXML private Label errPassword;
    @FXML private Label errConfirm;

    private User user;
    private AdminDashboardController dashboardController;

    @FXML
    public void initialize() {
        comboRole.getItems().addAll("ADMIN", "USER");
    }

    public void setUser(User user) {

        this.user = user;

        txtFullName.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone());
        comboRole.setValue(user.getRole());

        passwordField.clear();
        confirmPasswordField.clear();
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void handleUpdate(ActionEvent event) {

        clearErrors();

        if (!validateFields())
            return;

        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String role = comboRole.getValue();
        String newPassword = passwordField.getText();

        boolean updatePassword = !newPassword.isEmpty();

        try (Connection conn = DBConnection.getConnection()) {

            String sql;
            PreparedStatement ps;

            if (updatePassword) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                sql = "UPDATE user SET full_name=?, email=?, phone=?, role=?, password_hash=? WHERE id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, role);
                ps.setString(5, hashedPassword);
                ps.setInt(6, user.getId());

            } else {
                sql = "UPDATE user SET full_name=?, email=?, phone=?, role=? WHERE id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, role);
                ps.setInt(5, user.getId());
            }

            ps.executeUpdate();

            showAlert("Succès", "Utilisateur mis à jour avec succès");

            // ⚡ Retour automatique au dashboard
            dashboardController.returnToList();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleCancel(ActionEvent event) {
        goBack(event);
    }

    private void goBack(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user/admin-dashboard.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateFields() {

        boolean ok = true;

        if (txtFullName.getText().isEmpty()) {
            errName.setText("Nom obligatoire");
            ok = false;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", txtEmail.getText())) {
            errEmail.setText("Email invalide");
            ok = false;
        }

        if (!txtPhone.getText().matches("\\d{8,15}")) {
            errPhone.setText("Téléphone invalide");
            ok = false;
        }

        if (comboRole.getValue() == null) {
            errRole.setText("Choisir un rôle");
            ok = false;
        }

        if (!passwordField.getText().isEmpty()) {

            if (!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$", passwordField.getText())) {
                errPassword.setText("Mot de passe faible");
                ok = false;
            }

            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                errConfirm.setText("Mot de passe différent");
                ok = false;
            }
        }

        return ok;
    }

    private void clearErrors() {

        errName.setText("");
        errEmail.setText("");
        errPhone.setText("");
        errRole.setText("");
        errPassword.setText("");
        errConfirm.setText("");
    }
}

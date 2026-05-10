package core.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class LayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        try {
            loadPage("/fxml/dashboard/dashboard.fxml");  // ← غير هنا
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le dashboard");
        }
    }

    @FXML
    void handleButtonEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #7DBF6C; -fx-text-fill: #2E3D27; -fx-font-size: 14; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-background-radius: 5;");
    }

    @FXML
    void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #4B8B3B; -fx-text-fill: #F0E8D8; -fx-font-size: 14; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-background-radius: 5;");
    }

    private void loadPage(String page) throws IOException {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource(page));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @FXML
    void goDashboard() {
        try {
            loadPage("/fxml/dashboard/dashboard.fxml");  // ← غير هنا
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le dashboard");
        }
    }

    @FXML
    void goEquipement() {
        try {
            loadPage("/fxml/equipement/equipement.fxml");  // ← غير هنا
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la gestion des équipements");
        }
    }

    @FXML
    void goReview() {
        try {
            loadPage("/fxml/review/review.fxml");  // ← غير هنا
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la gestion des reviews");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
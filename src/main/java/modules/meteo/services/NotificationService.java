package modules.meteo.services;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotificationService {

    private Stage ownerStage;
    private static NotificationService instance;

    public NotificationService() {
        // Constructeur par défaut
    }

    public NotificationService(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    /**
     * Notification toast temporaire
     */
    public void envoyerNotification(String titre, String message, String niveau) {
        Platform.runLater(() -> {
            Popup popup = new Popup();

            VBox content = new VBox(5);
            content.setStyle(getStyleForNiveau(niveau));
            content.setAlignment(Pos.CENTER);
            content.setPrefWidth(300);
            content.setPrefHeight(80);
            content.setOpacity(0.95);

            Label lblTitre = new Label(titre);
            lblTitre.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");

            Label lblMessage = new Label(message);
            lblMessage.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
            lblMessage.setWrapText(true);
            lblMessage.setMaxWidth(280);

            content.getChildren().addAll(lblTitre, lblMessage);
            popup.getContent().add(content);

            if (ownerStage != null) {
                popup.show(ownerStage);
                popup.setX(ownerStage.getX() + ownerStage.getWidth() - 320);
                popup.setY(ownerStage.getY() + 50);
            }

            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> popup.hide());
            pause.play();
        });
    }

    /**
     * Alerte popup importante
     */
    public void envoyerAlerte(String titre, String message, String niveau) {
        Platform.runLater(() -> {
            AlertType type;
            switch(niveau) {
                case "DANGER": type = AlertType.ERROR; break;
                case "WARNING": type = AlertType.WARNING; break;
                default: type = AlertType.INFORMATION;
            }

            Alert alert = new Alert(type);
            alert.setTitle("Alerte Météo");
            alert.setHeaderText(titre);
            alert.setContentText(message);
            alert.show();
        });
    }

    private String getStyleForNiveau(String niveau) {
        String baseStyle = "-fx-padding: 15; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);";

        switch(niveau) {
            case "DANGER": return baseStyle + " -fx-background-color: #e74c3c;";
            case "WARNING": return baseStyle + " -fx-background-color: #f39c12;";
            default: return baseStyle + " -fx-background-color: #3498db;";
        }
    }
}
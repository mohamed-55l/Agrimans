package modules.meteo.services;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class NotificationService {

    /**
     * Envoie une notification toast
     */
    public void envoyerNotification(String titre, String message, String niveau) {
        Platform.runLater(() -> {
            Notifications notification = Notifications.create()
                    .title(titre)
                    .text(message)
                    .hideAfter(Duration.seconds(8))
                    .position(Pos.TOP_RIGHT);

            switch(niveau) {
                case "DANGER":
                    notification.showError();
                    break;
                case "WARNING":
                    notification.showWarning();
                    break;
                default:
                    notification.showInformation();
            }
        });

        // Logger la notification
        System.out.println("🔔 " + titre + " - " + message);
    }

    /**
     * Envoie une alerte popup
     */
    public void envoyerAlertePopup(String titre, String message, AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
}
package core.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneSwitcher {

    public static void switchTo(String fxmlPath, Node source) {

        try {

            URL url = SceneSwitcher.class.getResource(fxmlPath);

            if (url == null) {
                throw new RuntimeException("FXML NON TROUVÉ : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement FXML : " + fxmlPath, e);
        }
    }
}
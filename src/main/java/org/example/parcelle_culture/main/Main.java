package org.example.parcelle_culture.main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/MainLayout.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 900, 650);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Gestion des Cultures");
        stage.setScene(scene);
        stage.setResizable(true); // Permet de redimensionner
        stage.show();
    }

}

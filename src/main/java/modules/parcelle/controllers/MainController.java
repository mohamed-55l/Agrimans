package modules.parcelle.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class MainController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    public void initialize() {
        // Chargement automatique au démarrage
        loadView("ListeParcelle.fxml");
    }

    @FXML
    private void loadAjouter() {
        loadView("AjoutCulture.fxml");
    }

    @FXML
    private void loadListe() {
        loadView("ListeCulture.fxml");
    }

    @FXML
    public void loadAjouterP(ActionEvent actionEvent) {
        loadView("AjouterParcelle.fxml");
    }

    @FXML
    public void loadListeP(ActionEvent actionEvent) {
        loadView("ListeParcelle.fxml");
    }

    @FXML
    private void loadAI() {
        loadView("AIChat.fxml");
    }

    // 🔥 NOUVEAU
    @FXML
    private void loadStats() {
        loadView("Statistique.fxml");
    }

    private void loadView(String fxml) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/parcelle/" + fxml));
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

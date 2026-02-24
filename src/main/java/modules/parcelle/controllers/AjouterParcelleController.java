package modules.parcelle.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import modules.parcelle.models.Parcelle;
import modules.parcelle.services.ParcelleService;

public class AjouterParcelleController {

    @FXML private TextField nomField;
    @FXML private TextField superficieField;
    @FXML private TextField localisationField;
    @FXML private TextField typeSolField;
    @FXML private TextField utilisateurIdField;

    private ParcelleService service = new ParcelleService();

    @FXML
    private void ajouterParcelle() {

        try {
            Parcelle p = new Parcelle(
                    nomField.getText(),
                    Double.parseDouble(superficieField.getText()),
                    localisationField.getText(),
                    typeSolField.getText(),
                    Integer.parseInt(utilisateurIdField.getText())
            );

            service.ajouterParcelle(p);

            showAlert("Succès", "Parcelle ajoutée !");
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Vérifiez les champs !");
        }
    }

    private void clearFields() {
        nomField.clear();
        superficieField.clear();
        localisationField.clear();
        typeSolField.clear();
        utilisateurIdField.clear();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

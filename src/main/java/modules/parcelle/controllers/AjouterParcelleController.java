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

    // 🔴 nouveaux champs
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;

    private ParcelleService service = new ParcelleService();

    @FXML
    public void initialize() {
        if (utilisateurIdField != null) {
            int currentId = core.session.SessionManager.getCurrentUserId();
            utilisateurIdField.setText(String.valueOf(currentId));
            utilisateurIdField.setEditable(false);
            utilisateurIdField.setStyle("-fx-background-color: #e9ecef;");
        }
    }

    @FXML
    private void ajouterParcelle() {

        try {

            String nom = nomField.getText();
            double superficie = Double.parseDouble(superficieField.getText());
            String localisation = localisationField.getText();
            String typeSol = typeSolField.getText();
            int utilisateurId = Integer.parseInt(utilisateurIdField.getText());

            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());

            Parcelle p = new Parcelle(
                    nom,
                    superficie,
                    localisation,
                    typeSol,
                    utilisateurId,
                    latitude,
                    longitude
            );

            service.ajouterParcelle(p);

            showAlert("Succès", "Parcelle ajoutée !");
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Vérifiez les champs ! Détail : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nomField.clear();
        superficieField.clear();
        localisationField.clear();
        typeSolField.clear();
        latitudeField.clear();
        longitudeField.clear();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
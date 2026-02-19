package org.example.parcelle_culture.contoller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.parcelle_culture.entities.Parcelle;
import org.example.parcelle_culture.services.ParcelleService;

public class ModifierParcelleController {

    @FXML private TextField nomField;
    @FXML private TextField superficieField;
    @FXML private TextField localisationField;
    @FXML private TextField typeSolField;

    private Parcelle parcelle;
    private ParcelleService service = new ParcelleService();

    public void setParcelle(Parcelle p) {
        this.parcelle = p;

        nomField.setText(p.getNom());
        superficieField.setText(String.valueOf(p.getSuperficie()));
        localisationField.setText(p.getLocalisation());
        typeSolField.setText(p.getTypeSol());
    }

    @FXML
    private void modifierParcelle() {

        parcelle.setNom(nomField.getText());
        parcelle.setSuperficie(Double.parseDouble(superficieField.getText()));
        parcelle.setLocalisation(localisationField.getText());
        parcelle.setTypeSol(typeSolField.getText());

        service.modifierParcelle(parcelle);

        showAlert("Succès", "Parcelle modifiée !");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

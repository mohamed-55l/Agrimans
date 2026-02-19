package org.example.parcelle_culture.contoller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.parcelle_culture.entities.Culture;
import org.example.parcelle_culture.entities.Parcelle;
import org.example.parcelle_culture.services.CultureService;
import org.example.parcelle_culture.services.ParcelleService;

import java.time.LocalDate;
import java.util.List;

public class Add_Culture_Controller {

    @FXML private TextField nomField;
    @FXML private TextField typeCultureField;
    @FXML private DatePicker datePlantationPicker;
    @FXML private DatePicker dateRecoltePicker;
    @FXML private ComboBox<String> etatCultureCombo;
    @FXML private ComboBox<Parcelle> parcelleCombo;

    private CultureService cultureService = new CultureService();
    private ParcelleService parcelleService = new ParcelleService();

    @FXML
    public void initialize() {

        etatCultureCombo.getItems().addAll(
                "En croissance",
                "Récoltée",
                "Malade",
                "En préparation"
        );

        loadParcelles();
    }

    private void loadParcelles() {
        List<Parcelle> parcelles = parcelleService.afficherParcelles();
        parcelleCombo.getItems().addAll(parcelles);
    }

    @FXML
    private void ajouterCulture() {

        try {
            String nom = nomField.getText();
            String type = typeCultureField.getText();
            LocalDate datePlantation = datePlantationPicker.getValue();
            LocalDate dateRecolte = dateRecoltePicker.getValue();
            String etat = etatCultureCombo.getValue();

            Parcelle selectedParcelle = parcelleCombo.getValue();

            if (selectedParcelle == null) {
                showAlert("Erreur", "Veuillez choisir une parcelle !");
                return;
            }

            int parcelleId = selectedParcelle.getIdParcelle();

            Culture culture = new Culture(
                    nom,
                    type,
                    datePlantation,
                    dateRecolte,
                    etat,
                    parcelleId
            );

            cultureService.ajouterCulture(culture);

            showAlert("Succès", "Culture ajoutée avec succès !");
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Vérifiez les champs !");
        }
    }

    private void clearFields() {
        nomField.clear();
        typeCultureField.clear();
        datePlantationPicker.setValue(null);
        dateRecoltePicker.setValue(null);
        etatCultureCombo.setValue(null);
        parcelleCombo.setValue(null);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

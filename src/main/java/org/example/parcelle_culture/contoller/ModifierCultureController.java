package org.example.parcelle_culture.contoller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.parcelle_culture.entities.Culture;
import org.example.parcelle_culture.services.CultureService;

import java.time.LocalDate;

public class ModifierCultureController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField typeCultureField;

    @FXML
    private DatePicker datePlantationPicker;

    @FXML
    private DatePicker dateRecoltePicker;

    @FXML
    private ComboBox<String> etatCultureCombo;

    private Culture cultureSelectionnee;

    private CultureService cultureService = new CultureService();

    @FXML
    public void initialize() {
        etatCultureCombo.getItems().addAll(
                "En croissance",
                "Récoltée",
                "Malade",
                "En préparation"
        );
    }

    // 🔥 Cette méthode sera appelée depuis la TableView
    public void setCulture(Culture culture) {
        this.cultureSelectionnee = culture;

        nomField.setText(culture.getNom());
        typeCultureField.setText(culture.getTypeCulture());
        datePlantationPicker.setValue(culture.getDatePlantation());
        dateRecoltePicker.setValue(culture.getDateRecoltePrevue());
        etatCultureCombo.setValue(culture.getEtatCulture());
    }

    @FXML
    private void modifierCulture() {

        if (cultureSelectionnee == null) {
            showAlert("Erreur", "Aucune culture sélectionnée !");
            return;
        }

        String nom = nomField.getText();
        String type = typeCultureField.getText();
        LocalDate datePlantation = datePlantationPicker.getValue();
        LocalDate dateRecolte = dateRecoltePicker.getValue();
        String etat = etatCultureCombo.getValue();

        if (nom.isEmpty() || type.isEmpty() || datePlantation == null
                || dateRecolte == null || etat == null) {

            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        // Mise à jour objet
        cultureSelectionnee.setNom(nom);
        cultureSelectionnee.setTypeCulture(type);
        cultureSelectionnee.setDatePlantation(datePlantation);
        cultureSelectionnee.setDateRecoltePrevue(dateRecolte);
        cultureSelectionnee.setEtatCulture(etat);

        cultureService.modifierCulture(cultureSelectionnee);

        showAlert("Succès", "Culture modifiée avec succès !");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

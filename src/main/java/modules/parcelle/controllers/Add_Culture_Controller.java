package modules.parcelle.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import modules.parcelle.models.Culture;
import modules.parcelle.models.Parcelle;
import modules.parcelle.services.CultureService;
import modules.parcelle.services.ParcelleService;
import modules.parcelle.services.NotificationService;

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

            // 🔎 Validation
            if (nom.isEmpty() || type.isEmpty()
                    || datePlantation == null
                    || dateRecolte == null
                    || etat == null
                    || selectedParcelle == null) {

                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            if (dateRecolte.isBefore(datePlantation)) {
                showAlert("Erreur", "La date de récolte doit être après la plantation !");
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

            // 🔔 NOTIFICATION API si récolte proche (3 jours)
            if (dateRecolte.isBefore(LocalDate.now().plusDays(3))) {

                String sujet = "⚠ Récolte proche";
                String message = "La culture '" + nom +
                        "' sera prête pour récolte le " + dateRecolte;

                NotificationService.envoyerNotification(
                        "destinataire@email.com",  // 🔥 change ici
                        sujet,
                        message
                );
            }

            showAlert("Succès", "Culture ajoutée avec succès !");
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue !");
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
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
package modules.carte.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import modules.carte.models.Garage;
import modules.carte.services.CarteService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GarageFormController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfAdresse;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextField tfCapacite;
    @FXML private TextField tfResponsable;
    @FXML private TextField tfTelephone;
    @FXML private Label lblError;
    @FXML private Label lblTitre;        // À AJOUTER dans le FXML
    @FXML private Button btnValider;      // À AJOUTER dans le FXML

    private CarteService carteService;
    private Garage garageEnCours;         // Pour la modification

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        carteService = new CarteService();
    }

    /**
     * Pour la MODIFICATION d'un garage existant
     */
    public void setGarage(Garage garage) {
        this.garageEnCours = garage;

        if (garage != null) {
            tfNom.setText(garage.getNom());
            tfAdresse.setText(garage.getAdresse());
            tfLatitude.setText(String.valueOf(garage.getLatitude()));
            tfLongitude.setText(String.valueOf(garage.getLongitude()));
            tfCapacite.setText(String.valueOf(garage.getCapacite()));
            tfResponsable.setText(garage.getResponsable());
            tfTelephone.setText(garage.getTelephone());

            // Changer le titre et le bouton
            if (lblTitre != null) {
                lblTitre.setText("✏️ Modifier un garage");
            }
            if (btnValider != null) {
                btnValider.setText("✅ Modifier");
            }
        }
    }

    @FXML
    private void ajouterGarage() {
        if (!validerChamps()) return;

        try {
            if (garageEnCours != null) {
                // MODIFICATION
                garageEnCours.setNom(tfNom.getText().trim());
                garageEnCours.setAdresse(tfAdresse.getText().trim());
                garageEnCours.setLatitude(Double.parseDouble(tfLatitude.getText().trim()));
                garageEnCours.setLongitude(Double.parseDouble(tfLongitude.getText().trim()));
                garageEnCours.setCapacite(Integer.parseInt(tfCapacite.getText().trim()));
                garageEnCours.setResponsable(tfResponsable.getText().trim());
                garageEnCours.setTelephone(tfTelephone.getText().trim());

                carteService.updateGarage(garageEnCours);
                AlertUtils.showInfo("Succès", "Garage modifié avec succès");

            } else {
                // CRÉATION
                Garage garage = new Garage();
                garage.setNom(tfNom.getText().trim());
                garage.setAdresse(tfAdresse.getText().trim());
                garage.setLatitude(Double.parseDouble(tfLatitude.getText().trim()));
                garage.setLongitude(Double.parseDouble(tfLongitude.getText().trim()));
                garage.setCapacite(Integer.parseInt(tfCapacite.getText().trim()));
                garage.setResponsable(tfResponsable.getText().trim());
                garage.setTelephone(tfTelephone.getText().trim());

                carteService.createGarage(garage);
                AlertUtils.showInfo("Succès", "Garage ajouté avec succès");
            }

            fermer();

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Erreur: " + e.getMessage());
        } catch (NumberFormatException e) {
            lblError.setText("Format de nombre invalide");
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
    }

    private boolean validerChamps() {
        if (tfNom.getText().isEmpty()) {
            lblError.setText("Le nom est requis");
            return false;
        }
        if (tfAdresse.getText().isEmpty()) {
            lblError.setText("L'adresse est requise");
            return false;
        }
        try {
            Double.parseDouble(tfLatitude.getText());
            Double.parseDouble(tfLongitude.getText());
            Integer.parseInt(tfCapacite.getText());
        } catch (NumberFormatException e) {
            lblError.setText("Latitude, longitude et capacité doivent être des nombres");
            return false;
        }
        return true;
    }
}
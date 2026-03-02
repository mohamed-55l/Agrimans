package modules.equipement.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ModifEquipementController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfType;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<String> cbEtat;
    @FXML private Label lblError;
    @FXML private Label lblTitre;

    private EquipementService equipementService = new EquipementService();
    private Equipement equipementEnCours;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        cbEtat.getItems().addAll("Disponible", "Non disponible", "En maintenance");
    }

    /**
     * Reçoit l'équipement à modifier depuis le dashboard
     */
    public void setEquipement(Equipement equipement) {
        this.equipementEnCours = equipement;

        if (equipement != null) {
            lblTitre.setText("Modifier : " + equipement.getNom());
            tfNom.setText(equipement.getNom());
            tfType.setText(equipement.getType());
            tfPrix.setText(String.valueOf(equipement.getPrix()));
            cbEtat.setValue(equipement.getDisponibilite());
        }
    }

    @FXML
    private void modifierEquipement() {
        if (!validerChamps()) return;

        try {
            equipementEnCours.setNom(tfNom.getText().trim());
            equipementEnCours.setType(tfType.getText().trim());
            equipementEnCours.setPrix(Float.parseFloat(tfPrix.getText()));
            equipementEnCours.setDisponibilite(cbEtat.getValue());

            equipementService.update(equipementEnCours);

            AlertUtils.showInfo("Succès", "Équipement modifié avec succès");
            retourDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Erreur lors de la modification");
        }
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard/admin_dashboard.fxml"));
            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au dashboard");
        }
    }

    private boolean validerChamps() {
        if (tfNom.getText().isEmpty()) {
            lblError.setText("Le nom est requis");
            return false;
        }
        if (tfType.getText().isEmpty()) {
            lblError.setText("Le type est requis");
            return false;
        }
        if (tfPrix.getText().isEmpty()) {
            lblError.setText("Le prix est requis");
            return false;
        }

        try {
            Float.parseFloat(tfPrix.getText());
        } catch (NumberFormatException e) {
            lblError.setText("Prix invalide");
            return false;
        }

        lblError.setText("");
        return true;
    }
}
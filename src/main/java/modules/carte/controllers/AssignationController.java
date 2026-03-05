package modules.carte.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import modules.carte.models.Garage;
import modules.carte.services.CarteService;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AssignationController implements Initializable {

    @FXML private ComboBox<Equipement> cbEquipement;
    @FXML private ComboBox<Garage> cbGarage;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblError;

    private CarteService carteService;
    private EquipementService equipementService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        carteService = new CarteService();
        equipementService = new EquipementService();

        cbStatut.getItems().addAll("DANS_GARAGE", "EN_UTILISATION", "EN_DEPLACEMENT");
        cbStatut.setValue("DANS_GARAGE");

        chargerEquipements();
        chargerGarages();
    }

    private void chargerEquipements() {
        try {
            List<Equipement> equipements = equipementService.getAll();
            cbEquipement.setItems(FXCollections.observableArrayList(equipements));

            cbEquipement.setCellFactory(param -> new ListCell<Equipement>() {
                @Override
                protected void updateItem(Equipement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " (" + item.getType() + ")");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les équipements");
        }
    }

    private void chargerGarages() {
        try {
            List<Garage> garages = carteService.getAllGarages();
            cbGarage.setItems(FXCollections.observableArrayList(garages));

            cbGarage.setCellFactory(param -> new ListCell<Garage>() {
                @Override
                protected void updateItem(Garage item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " (" + item.getAdresse() + ")");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les garages");
        }
    }

    @FXML
    private void assigner() {
        if (cbEquipement.getValue() == null) {
            lblError.setText("Sélectionnez un équipement");
            return;
        }
        if (cbGarage.getValue() == null) {
            lblError.setText("Sélectionnez un garage");
            return;
        }

        try {
            carteService.assignerEquipementAGarage(
                    cbEquipement.getValue().getId(),
                    cbGarage.getValue().getId()
            );

            carteService.changerStatutEquipement(
                    cbEquipement.getValue().getId(),
                    cbStatut.getValue()
            );

            AlertUtils.showInfo("Succès", "Équipement assigné avec succès");
            fermer();

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) cbEquipement.getScene().getWindow();
        stage.close();
    }
}
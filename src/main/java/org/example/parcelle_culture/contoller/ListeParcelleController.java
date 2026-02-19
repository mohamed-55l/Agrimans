package org.example.parcelle_culture.contoller;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.parcelle_culture.entities.Parcelle;
import org.example.parcelle_culture.services.ParcelleService;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

public class ListeParcelleController {

    @FXML private TableView<Parcelle> tableParcelle;
    @FXML private TableColumn<Parcelle, Integer> colId;
    @FXML private TableColumn<Parcelle, String> colNom;
    @FXML private TableColumn<Parcelle, Double> colSuperficie;
    @FXML private TableColumn<Parcelle, String> colLocalisation;
    @FXML private TableColumn<Parcelle, String> colTypeSol;
    @FXML private TableColumn<Parcelle, Integer> colUserId;

    private ParcelleService service = new ParcelleService();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("idParcelle"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSuperficie.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colTypeSol.setCellValueFactory(new PropertyValueFactory<>("typeSol"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));

        refreshTable();
    }

    @FXML
    private void refreshTable() {
        List<Parcelle> list = service.afficherParcelles();
        tableParcelle.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void supprimerParcelle() {
        Parcelle selected = tableParcelle.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        service.supprimerParcelle(selected.getIdParcelle());
        refreshTable();
    }

    @FXML
    private void openModifier() {
        Parcelle selected = tableParcelle.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierParcelle.fxml"));
            AnchorPane root = loader.load();

            ModifierParcelleController controller = loader.getController();
            controller.setParcelle(selected);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 500, 400));
            stage.setTitle("Modifier Parcelle");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

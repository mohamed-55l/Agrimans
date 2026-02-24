package modules.parcelle.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import modules.parcelle.models.Culture;
import modules.parcelle.services.CultureService;

import java.util.List;

public class ListeCultureController {

    @FXML
    private TableView<Culture> tableCulture;

    @FXML
    private TableColumn<Culture, Integer> colId;

    @FXML
    private TableColumn<Culture, String> colNom;

    @FXML
    private TableColumn<Culture, String> colType;

    @FXML
    private TableColumn<Culture, Object> colDatePlantation;

    @FXML
    private TableColumn<Culture, Object> colDateRecolte;

    @FXML
    private TableColumn<Culture, String> colEtat;

    @FXML
    private TableColumn<Culture, Integer> colParcelle;

    private CultureService cultureService = new CultureService();

    @FXML
    public void initialize() {

        // Liaison colonnes avec attributs de Culture
        colId.setCellValueFactory(new PropertyValueFactory<>("idCulture"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeCulture"));
        colDatePlantation.setCellValueFactory(new PropertyValueFactory<>("datePlantation"));
        colDateRecolte.setCellValueFactory(new PropertyValueFactory<>("dateRecoltePrevue"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatCulture"));
        colParcelle.setCellValueFactory(new PropertyValueFactory<>("parcelleId"));

        loadData();
    }

    private void loadData() {
        List<Culture> cultures = cultureService.afficherCultures();
        ObservableList<Culture> observableList = FXCollections.observableArrayList(cultures);
        tableCulture.setItems(observableList);
    }


    @FXML
    private void openModifier() {
        try {
            Culture selected = tableCulture.getSelectionModel().getSelectedItem();

            if (selected == null) {
                System.out.println("Aucune culture sélectionnée");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCulture.fxml"));
            AnchorPane pane = loader.load();

            ModifierCultureController controller = loader.getController();
            controller.setCulture(selected);

            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            stage.setTitle("Modifier Culture");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshTable() {
        loadData();
    }

    @FXML
    private void supprimerCulture() {

        Culture selected = tableCulture.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une culture !");
            return;
        }

        // 🔥 Confirmation avant suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la culture ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette culture ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {

            cultureService.supprimerCulture(selected.getIdCulture());

            showAlert("Succès", "Culture supprimée avec succès !");

            refreshTable(); // 🔄 rafraîchir la table
        }
    }

    private void showAlert(String succès, String s) {
    }

}

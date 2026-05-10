package modules.parcelle.controllers;

import javafx.collections.*;
import javafx.collections.transformation.*;
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

    @FXML private TableView<Culture> tableCulture;
    @FXML private TableColumn<Culture, String> colNom;
    @FXML private TableColumn<Culture, String> colType;
    @FXML private TableColumn<Culture, Object> colDatePlantation;
    @FXML private TableColumn<Culture, Object> colDateRecolte;
    @FXML private TableColumn<Culture, String> colEtat;
    @FXML private TableColumn<Culture, Integer> colParcelle;

    @FXML private TextField searchField;

    private CultureService cultureService = new CultureService();
    private ObservableList<Culture> masterData;

    @FXML
    public void initialize() {

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeCulture"));
        colDatePlantation.setCellValueFactory(new PropertyValueFactory<>("datePlantation"));
        colDateRecolte.setCellValueFactory(new PropertyValueFactory<>("dateRecoltePrevue"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatCulture"));
        colParcelle.setCellValueFactory(new PropertyValueFactory<>("parcelleId"));

        refreshTable();
    }

    @FXML
    private void refreshTable() {

        List<Culture> cultures = cultureService.afficherCultures();
        masterData = FXCollections.observableArrayList(cultures);

        activerRecherche();
    }

    // 🔍 Méthode recherche
    private void activerRecherche() {

        FilteredList<Culture> filteredData =
                new FilteredList<>(masterData, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(culture -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return culture.getNom().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Culture> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableCulture.comparatorProperty());

        tableCulture.setItems(sortedData);
    }

    @FXML
    private void openModifier() {

        try {
            Culture selected = tableCulture.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Erreur", "Veuillez sélectionner une culture !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/parcelle/ModifierCulture.fxml"));
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
    private void supprimerCulture() {

        Culture selected = tableCulture.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une culture !");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la culture ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette culture ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {

            cultureService.supprimerCulture(selected.getIdCulture());

            showAlert("Succès", "Culture supprimée avec succès !");
            refreshTable();
        }
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
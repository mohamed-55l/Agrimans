package modules.parcelle.controllers;

import javafx.collections.*;
import javafx.collections.transformation.*;
import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

import modules.parcelle.models.Parcelle;
import modules.parcelle.services.ParcelleService;
import modules.parcelle.services.MeteoService;
import modules.parcelle.services.RecommendationService;
import modules.parcelle.services.RendementService;

import java.io.IOException;
import java.util.List;

public class ListeParcelleController {

    @FXML private TableView<Parcelle> tableParcelle;
    @FXML private TableColumn<Parcelle, String> colNom;
    @FXML private TableColumn<Parcelle, Double> colSuperficie;
    @FXML private TableColumn<Parcelle, String> colLocalisation;
    @FXML private TableColumn<Parcelle, String> colTypeSol;
    @FXML private TableColumn<Parcelle, String> colUserNom;

    @FXML private Label meteoLabel;
    @FXML private TextField searchField;
    @FXML private WebView mapView;

    private ParcelleService service = new ParcelleService();
    private ObservableList<Parcelle> masterData;

    @FXML
    public void initialize() {

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSuperficie.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colTypeSol.setCellValueFactory(new PropertyValueFactory<>("typeSol"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("utilisateurNom"));

        refreshTable();

        tableParcelle.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue != null) {

                        String ville = newValue.getLocalisation();

                        String meteo = MeteoService.getMeteo(ville);

                        String cultureReco =
                                RecommendationService.recommanderCulture(newValue.getTypeSol());

                        double rendement =
                                RendementService.calculerRendement(
                                        cultureReco,
                                        newValue.getSuperficie()
                                );

                        meteoLabel.setText(
                                "📍 " + ville +
                                        "\n🌦 Météo : " + meteo +
                                        "\n🌱 Culture recommandée : " + cultureReco +
                                        "\n📊 Production estimée : " + rendement + " tonnes"
                        );

                        afficherCarte(ville);
                    }
                }
        );
    }

    @FXML
    private void refreshTable() {
        List<Parcelle> list;
        if (SessionManager.isAdmin()) {
            list = service.afficherParcelles();
        } else {
            list = service.afficherParcellesByUserId(SessionManager.getCurrentUserId());
        }
        masterData = FXCollections.observableArrayList(list);
        activerRecherche();
    }

    @FXML
    private void openAjouterParcelle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/parcelle/AjouterParcelle.fxml"));
            AnchorPane root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une parcelle");
            stage.show();
            stage.setOnHidden(event -> refreshTable());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void activerRecherche() {

        FilteredList<Parcelle> filteredData =
                new FilteredList<>(masterData, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(parcelle -> {

                if (newValue == null || newValue.isEmpty()) return true;

                String filter = newValue.toLowerCase();

                String userNom = parcelle.getUtilisateurNom() != null ? parcelle.getUtilisateurNom() : "";
                return parcelle.getNom().toLowerCase().contains(filter)
                        || parcelle.getLocalisation().toLowerCase().contains(filter)
                        || parcelle.getTypeSol().toLowerCase().contains(filter)
                        || String.valueOf(parcelle.getSuperficie()).contains(filter)
                        || userNom.toLowerCase().contains(filter);
            });
        });

        SortedList<Parcelle> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableParcelle.comparatorProperty());

        tableParcelle.setItems(sortedData);
    }

    private void afficherCarte(String ville) {

        WebEngine webEngine = mapView.getEngine();

        String url = "https://www.openstreetmap.org/search?query=" + ville;

        webEngine.load(url);
    }

    @FXML
    private void supprimerParcelle() {
        Parcelle selected = tableParcelle.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!SessionManager.isAdmin() && selected.getUtilisateurId() != SessionManager.getCurrentUserId()) {
            AlertUtils.showError("Erreur", "Vous ne pouvez pas supprimer une parcelle qui n'est pas à vous.");
            return;
        }

        service.supprimerParcelle(selected.getIdParcelle());
        refreshTable();
    }

    @FXML
    private void openModifier() {
        Parcelle selected = tableParcelle.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!SessionManager.isAdmin() && selected.getUtilisateurId() != SessionManager.getCurrentUserId()) {
            AlertUtils.showError("Erreur", "Vous ne pouvez pas modifier une parcelle qui n'est pas à vous.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/parcelle/ModifierParcelle.fxml"));
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
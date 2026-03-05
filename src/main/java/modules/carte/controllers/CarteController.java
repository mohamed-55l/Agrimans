package modules.carte.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import modules.carte.models.EquipementGeo;
import modules.carte.models.Garage;
import modules.carte.services.CarteService;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

public class CarteController implements Initializable {

    @FXML private WebView webView;
    @FXML private ComboBox<Garage> cbGarages;
    @FXML private TableView<EquipementGeo> tableEquipements;
    @FXML private TableColumn<EquipementGeo, String> colIcone;
    @FXML private TableColumn<EquipementGeo, String> colNom;
    @FXML private TableColumn<EquipementGeo, String> colType;
    @FXML private TableColumn<EquipementGeo, String> colStatut;
    @FXML private TableColumn<EquipementGeo, String> colLocalisation;

    @FXML private Label lblGarageNom;
    @FXML private Label lblGarageAdresse;
    @FXML private Label lblGarageCapacite;
    @FXML private Label lblGarageResponsable;
    @FXML private Label lblGarageTelephone;
    @FXML private Label lblTauxOccupation;

    // =====================================================
    // NOUVEAU : Tableau des garages
    // =====================================================

    @FXML private TableView<Garage> tableGarages;
    @FXML private TableColumn<Garage, Integer> colGarageId;
    @FXML private TableColumn<Garage, String> colGarageNom;
    @FXML private TableColumn<Garage, String> colGarageAdresse;
    @FXML private TableColumn<Garage, Integer> colGarageCapacite;
    @FXML private TableColumn<Garage, Integer> colGarageOccupation;
    @FXML private TableColumn<Garage, String> colGarageResponsable;
    @FXML private TableColumn<Garage, String> colGarageTelephone;
    @FXML private TableColumn<Garage, Void> colGarageAction;

    @FXML private TabPane tabPane;
    @FXML private Tab tabCarte;
    @FXML private Tab tabListe;
    @FXML private Tab tabStats;

    // =====================================================
// COMPOSANTS POUR LES STATISTIQUES
// =====================================================

    @FXML private PieChart pieChartOccupation;
    @FXML private BarChart<String, Number> barChartGarages;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label lblTotalGarages;
    @FXML private Label lblTotalEquipementsGarages;
    @FXML private Label lblMoyenneOccupation;
    @FXML private Label lblGaragePlusPlein;
    @FXML private Label lblGaragePlusVide;
    @FXML private Label lblCapaciteTotale;


    private CarteService carteService;
    private EquipementService equipementService;
    private WebEngine webEngine;
    private ObservableList<Garage> garageList;
    private ObservableList<EquipementGeo> equipementsList;

    private boolean cartePret = false;
    private boolean donneesChargees = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        carteService = new CarteService();
        equipementService = new EquipementService();
        garageList = FXCollections.observableArrayList();
        equipementsList = FXCollections.observableArrayList();

        webEngine = webView.getEngine();

        configurerTableEquipements();
        configurerTableGarages();  // NOUVEAU

        // Désactiver les contrôles
        cbGarages.setDisable(true);

        // Charger la carte HTML
        webEngine.loadContent(getMapHTML());

        // Surveiller le chargement de la carte
        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("✅ Carte HTML chargée, attente de l'initialisation...");

                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> {
                    Boolean mapExiste = (Boolean) webEngine.executeScript(
                            "typeof map !== 'undefined' && map !== null"
                    );

                    if (mapExiste) {
                        cartePret = true;
                        System.out.println("✅ Carte prête à utiliser");
                        chargerDonnees();
                    } else {
                        System.out.println("⚠️ Carte pas encore prête, nouvel essai...");
                        PauseTransition pause2 = new PauseTransition(Duration.millis(500));
                        pause2.setOnFinished(e2 -> {
                            Boolean mapExiste2 = (Boolean) webEngine.executeScript(
                                    "typeof map !== 'undefined' && map !== null"
                            );
                            if (mapExiste2) {
                                cartePret = true;
                                System.out.println("✅ Carte prête (2ème essai)");
                                chargerDonnees();
                            }
                        });
                        pause2.play();
                    }
                });
                pause.play();
            }
        });

        // Listener sur la sélection du garage
        cbGarages.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && cartePret && donneesChargees) {
                afficherInfosGarage(selected);
                centrerCarteSur(selected.getLatitude(), selected.getLongitude(), 15);
                chargerEquipementsGarage(selected.getId());
            }
        });
    }

    private void configurerTableEquipements() {
        colIcone.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIcone()));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutGarage"));
        colLocalisation.setCellValueFactory(cellData -> {
            EquipementGeo e = cellData.getValue();
            if (e.isDansGarage()) {
                return new javafx.beans.property.SimpleStringProperty("📍 Au garage");
            } else if (e.getPositionGPS() != null) {
                return new javafx.beans.property.SimpleStringProperty("🚜 En déplacement");
            }
            return new javafx.beans.property.SimpleStringProperty("❓ Non localisé");
        });

        colStatut.setCellFactory(tc -> new TableCell<EquipementGeo, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                } else {
                    setText(statut);
                    switch(statut) {
                        case "DANS_GARAGE":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "EN_UTILISATION":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "EN_DEPLACEMENT":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        tableEquipements.setItems(equipementsList);
    }

    // =====================================================
    // NOUVELLE MÉTHODE : Configuration du tableau des garages
    // =====================================================

    private void configurerTableGarages() {
        colGarageId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGarageNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colGarageAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colGarageCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colGarageOccupation.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getEquipementsCount()).asObject());
        colGarageResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        colGarageTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Colonne action avec boutons Modifier/Supprimer
        colGarageAction.setCellFactory(param -> new TableCell<Garage, Void>() {
            private final HBox box = new HBox(5);
            private final Button btnModifier = new Button("✏️");
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");

                btnModifier.setOnAction(event -> {
                    Garage garage = getTableView().getItems().get(getIndex());
                    modifierGarage(garage);
                });

                btnSupprimer.setOnAction(event -> {
                    Garage garage = getTableView().getItems().get(getIndex());
                    supprimerGarage(garage);
                });

                box.getChildren().addAll(btnModifier, btnSupprimer);
                box.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Double-clic pour voir sur la carte
        tableGarages.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Garage garage = tableGarages.getSelectionModel().getSelectedItem();
                if (garage != null) {
                    tabPane.getSelectionModel().select(tabCarte);
                    cbGarages.getSelectionModel().select(garage);
                }
            }
        });

        tableGarages.setItems(garageList);
    }

    private void chargerDonnees() {
        try {
            List<Garage> garages = carteService.getAllGarages();
            garageList.setAll(garages);
            cbGarages.setItems(garageList);

            donneesChargees = true;

            // Ajouter tous les marqueurs
            ajouterTousLesMarqueurs();

            // Charger les statistiques
            chargerStatistiques();  // 👈 NOUVELLE LIGNE

            // Réactiver le ComboBox
            cbGarages.setDisable(false);

            // Sélectionner le premier garage si disponible
            if (!garages.isEmpty()) {
                javafx.application.Platform.runLater(() -> {
                    cbGarages.getSelectionModel().select(0);
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les garages");
        }
    }

    private void chargerEquipementsGarage(int garageId) {
        garageList.stream()
                .filter(g -> g.getId() == garageId)
                .findFirst()
                .ifPresent(garage -> {
                    equipementsList.setAll(garage.getEquipements());
                });
    }

    private void afficherInfosGarage(Garage garage) {
        lblGarageNom.setText(garage.getNom());
        lblGarageAdresse.setText(garage.getAdresse());
        lblGarageCapacite.setText(garage.getEquipementsCount() + " / " + garage.getCapacite());
        lblGarageResponsable.setText(garage.getResponsable());
        lblGarageTelephone.setText(garage.getTelephone());

        double taux = garage.getTauxOccupation();
        lblTauxOccupation.setText(String.format("%.1f%%", taux));

        if (taux > 90) {
            lblTauxOccupation.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (taux > 70) {
            lblTauxOccupation.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            lblTauxOccupation.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    private void centrerCarteSur(double lat, double lng, int zoom) {
        if (!cartePret) return;

        try {
            String script = String.format("map.setView([%f, %f], %d);", lat, lng, zoom);
            webEngine.executeScript(script);
        } catch (Exception e) {
            System.err.println("Erreur centrage carte: " + e.getMessage());
        }
    }

    private void ajouterTousLesMarqueurs() {
        if (!cartePret) return;

        try {
            Boolean mapExiste = (Boolean) webEngine.executeScript(
                    "typeof map !== 'undefined' && map !== null"
            );

            if (!mapExiste) return;

            for (Garage garage : garageList) {
                ajouterMarqueurGarage(garage);

                for (EquipementGeo equipement : garage.getEquipements()) {
                    equipement.setLatitude(garage.getLatitude());
                    equipement.setLongitude(garage.getLongitude());
                    ajouterMarqueurEquipement(equipement);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur ajout marqueurs: " + e.getMessage());
        }
    }

    private void ajouterMarqueurGarage(Garage garage) {
        try {
            String popupContent = String.format(
                    "<b>🏠 %s</b><br>" +
                            "📍 %s<br>" +
                            "📊 %d/%d équipements<br>" +
                            "👤 %s<br>" +
                            "📞 %s",
                    garage.getNom(),
                    garage.getAdresse(),
                    garage.getEquipementsCount(),
                    garage.getCapacite(),
                    garage.getResponsable(),
                    garage.getTelephone()
            );

            String script = String.format(
                    "L.marker([%f, %f], {icon: garageIcon}).addTo(map).bindPopup('%s');",
                    garage.getLatitude(), garage.getLongitude(), popupContent
            );

            webEngine.executeScript(script);
        } catch (Exception e) {
            System.err.println("Erreur ajout marqueur garage: " + e.getMessage());
        }
    }

    private void ajouterMarqueurEquipement(EquipementGeo equipement) {
        try {
            if (equipement.getLatitude() == 0 || equipement.getLongitude() == 0) return;

            String popupContent = String.format(
                    "<b>%s %s</b><br>" +
                            "Type: %s<br>" +
                            "État: %s<br>" +
                            "Statut: %s",
                    equipement.getIcone(), equipement.getNom(),
                    equipement.getType(),
                    equipement.getDisponibilite(),
                    equipement.getStatutGarage()
            );

            String couleur = equipement.getCouleurMarqueur();

            String script = String.format(
                    "L.marker([%f, %f], {icon: getColoredIcon('%s')}).addTo(map).bindPopup('%s');",
                    equipement.getLatitude(), equipement.getLongitude(), couleur, popupContent
            );

            webEngine.executeScript(script);
        } catch (Exception e) {
            System.err.println("Erreur ajout marqueur équipement: " + e.getMessage());
        }
    }

    // =====================================================
    // NOUVELLES MÉTHODES : Gestion des garages
    // =====================================================

    private void modifierGarage(Garage garage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/carte/garage_form.fxml"));
            Parent root = loader.load();

            GarageFormController controller = loader.getController();
            controller.setGarage(garage);  // À ajouter dans GarageFormController

            Stage stage = new Stage();
            stage.setTitle("Modifier garage");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recharger les données
            if (cartePret) {
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> chargerDonnees());
                pause.play();
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void supprimerGarage(Garage garage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le garage");
        confirm.setContentText("Voulez-vous vraiment supprimer le garage '" + garage.getNom() + "' ?\n" +
                "Les équipements seront désassignés mais pas supprimés.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                carteService.deleteGarage(garage.getId());
                AlertUtils.showInfo("Succès", "Garage supprimé");

                if (cartePret) {
                    PauseTransition pause = new PauseTransition(Duration.millis(500));
                    pause.setOnFinished(e -> chargerDonnees());
                    pause.play();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Impossible de supprimer le garage");
            }
        }
    }




    // =====================================================
// MÉTHODES POUR LES STATISTIQUES
// =====================================================

    private void chargerStatistiques() {
        if (garageList.isEmpty()) return;

        try {
            // Statistiques générales
            int totalGarages = garageList.size();
            int totalEquipements = garageList.stream()
                    .mapToInt(Garage::getEquipementsCount)
                    .sum();
            int capaciteTotale = garageList.stream()
                    .mapToInt(Garage::getCapacite)
                    .sum();

            double moyenneOccupation = capaciteTotale > 0 ?
                    (totalEquipements * 100.0 / capaciteTotale) : 0;

            Garage plusPlein = garageList.stream()
                    .max((g1, g2) -> Double.compare(g1.getTauxOccupation(), g2.getTauxOccupation()))
                    .orElse(null);

            Garage plusVide = garageList.stream()
                    .min((g1, g2) -> Double.compare(g1.getTauxOccupation(), g2.getTauxOccupation()))
                    .orElse(null);

            // Mettre à jour les labels
            lblTotalGarages.setText(String.valueOf(totalGarages));
            lblTotalEquipementsGarages.setText(String.valueOf(totalEquipements));
            lblMoyenneOccupation.setText(String.format("%.1f%%", moyenneOccupation));
            lblCapaciteTotale.setText(String.valueOf(capaciteTotale));

            if (plusPlein != null) {
                lblGaragePlusPlein.setText(plusPlein.getNom() + " (" +
                        String.format("%.1f%%", plusPlein.getTauxOccupation()) + ")");
            }

            if (plusVide != null) {
                lblGaragePlusVide.setText(plusVide.getNom() + " (" +
                        String.format("%.1f%%", plusVide.getTauxOccupation()) + ")");
            }

            // Charger le graphique circulaire (répartition des équipements par garage)
            chargerPieChart();

            // Charger le graphique à barres (taux d'occupation par garage)
            chargerBarChart();

        } catch (Exception e) {
            System.err.println("Erreur chargement statistiques: " + e.getMessage());
        }
    }

    private void chargerPieChart() {
        pieChartOccupation.getData().clear();

        for (Garage garage : garageList) {
            int occupes = garage.getEquipementsCount();
            int libres = garage.getCapacite() - occupes;

            if (occupes > 0) {
                PieChart.Data slice = new PieChart.Data(
                        garage.getNom() + " (" + occupes + ")", occupes);
                pieChartOccupation.getData().add(slice);
            }
        }

        pieChartOccupation.setTitle("Répartition des équipements par garage");
    }

    private void chargerBarChart() {
        barChartGarages.getData().clear();

        XYChart.Series<String, Number> seriesOccupation = new XYChart.Series<>();
        seriesOccupation.setName("Taux d'occupation (%)");

        XYChart.Series<String, Number> seriesCapacite = new XYChart.Series<>();
        seriesCapacite.setName("Capacité totale");

        for (Garage garage : garageList) {
            seriesOccupation.getData().add(new XYChart.Data<>(
                    garage.getNom(), garage.getTauxOccupation()));
            seriesCapacite.getData().add(new XYChart.Data<>(
                    garage.getNom(), garage.getCapacite()));
        }

        barChartGarages.getData().addAll(seriesOccupation, seriesCapacite);
        barChartGarages.setTitle("Taux d'occupation par garage");
    }

// Appelez cette méthode dans chargerDonnees() après avoir chargé les garages
// Ajoutez cette ligne à la fin de la méthode chargerDonnees() :
// chargerStatistiques();




    private String getMapHTML() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    #map { height: 100vh; width: 100%; }
                    .custom-div-icon {
                        background: transparent;
                        border: none;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([36.8065, 10.1815], 8);
                    
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap'
                    }).addTo(map);
                    
                    var garageIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: '<div style="background-color: #4B8B3B; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px;">🏠</div>',
                        iconSize: [30, 30],
                        popupAnchor: [0, -15]
                    });
                    
                    function getColoredIcon(color) {
                        return L.divIcon({
                            className: 'custom-div-icon',
                            html: '<div style="background-color: ' + color + '; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>',
                            iconSize: [20, 20]
                        });
                    }
                </script>
            </body>
            </html>
            """;
    }

    @FXML
    private void ajouterGarage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/carte/garage_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un garage");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (cartePret) {
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> chargerDonnees());
                pause.play();
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    private void assignerEquipement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/carte/assignation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Assigner un équipement");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (cartePret) {
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> chargerDonnees());
                pause.play();
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    private void actualiser() {
        if (cartePret) {
            webEngine.executeScript("location.reload()");
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                cartePret = false;
                donneesChargees = false;
                cbGarages.setDisable(true);

                PauseTransition pause2 = new PauseTransition(Duration.millis(500));
                pause2.setOnFinished(e2 -> {
                    Boolean mapExiste = (Boolean) webEngine.executeScript(
                            "typeof map !== 'undefined' && map !== null"
                    );
                    if (mapExiste) {
                        cartePret = true;
                        chargerDonnees();
                    }
                });
                pause2.play();
            });
            pause.play();
        }
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard/admin_dashboard.fxml"));
            Stage stage = (Stage) webView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au dashboard");
        }
    }
}
package modules.carte.controllers;

import core.utils.AlertUtils;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import modules.carte.models.EquipementGeo;
import modules.carte.models.Garage;
import modules.carte.services.CarteService;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserGarageController implements Initializable {

    @FXML private ComboBox<Garage> cbGarages;
    @FXML private TableView<EquipementGeo> tableEquipements;
    @FXML private TableColumn<EquipementGeo, String> colIcone;
    @FXML private TableColumn<EquipementGeo, String> colNom;
    @FXML private TableColumn<EquipementGeo, String> colType;
    @FXML private TableColumn<EquipementGeo, String> colStatut;
    @FXML private TableColumn<EquipementGeo, String> colLocalisation;
    @FXML private WebView webView;

    private EquipementService equipementService = new EquipementService();
    private CarteService carteService = new CarteService();
    private WebEngine webEngine;
    private boolean cartePret = false;
    private boolean donneesChargees = false;
    private List<Garage> garageList;
    private List<EquipementGeo> equipementsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        
        colIcone.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIcone()));

        cbGarages.setDisable(true);
        if (webView != null) {
            webEngine = webView.getEngine();
            webEngine.loadContent(getMapHTML());

            webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    PauseTransition pause = new PauseTransition(Duration.millis(500));
                    pause.setOnFinished(e -> {
                        Boolean mapExiste = (Boolean) webEngine.executeScript("typeof map !== 'undefined' && map !== null");
                        if (mapExiste) {
                            cartePret = true;
                            actualiser();
                        }
                    });
                    pause.play();
                }
            });
        }

        cbGarages.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && cartePret && donneesChargees) {
                centrerSurAdresse(newValue.getAdresse(), newValue.getLatitude(), newValue.getLongitude());
                chargerEquipementsGarage(newValue.getId());
            }
        });
    }

    private void chargerEquipementsGarage(int garageId) {
        garageList.stream().filter(g -> g.getId() == garageId).findFirst().ifPresent(garage -> {
            tableEquipements.setItems(FXCollections.observableArrayList(garage.getEquipements()));
        });
    }

    @FXML
    private void actualiser() {
        if (!cartePret) return;
        try {
            garageList = carteService.getAllGarages();
            cbGarages.setItems(FXCollections.observableArrayList(garageList));
            donneesChargees = true;
            cbGarages.setDisable(false);
            
            ajouterTousLesMarqueurs();

            if (!garageList.isEmpty()) {
                cbGarages.getSelectionModel().select(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les garages.");
        }
    }

    private void ajouterTousLesMarqueurs() {
        if (!cartePret || garageList == null) return;
        try {
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
            String popupContent = String.format("<b>🏠 %s</b><br>📍 %s", garage.getNom(), garage.getAdresse());
            String script = String.format(java.util.Locale.US,
                    "L.marker([%f, %f], {icon: garageIcon}).addTo(map).bindPopup('%s');",
                    garage.getLatitude(), garage.getLongitude(), popupContent
            );
            webEngine.executeScript(script);
        } catch (Exception e) {}
    }

    private void ajouterMarqueurEquipement(EquipementGeo equipement) {
        try {
            if (equipement.getLatitude() == 0 || equipement.getLongitude() == 0) return;
            String popupContent = String.format("<b>%s %s</b><br>Type: %s<br>État: %s<br>Statut: %s",
                    equipement.getIcone(), equipement.getNom(), equipement.getType(),
                    equipement.getDisponibilite(), equipement.getStatutGarage());
            String couleur = equipement.getCouleurMarqueur();
            String script = String.format(java.util.Locale.US,
                    "L.marker([%f, %f], {icon: getColoredIcon('%s')}).addTo(map).bindPopup('%s');",
                    equipement.getLatitude(), equipement.getLongitude(), couleur, popupContent
            );
            webEngine.executeScript(script);
        } catch (Exception e) {}
    }

    private void centrerCarteSur(double lat, double lng, int zoom) {
        if (!cartePret) return;
        try {
            webEngine.executeScript(String.format(java.util.Locale.US, "map.setView([%f, %f], %d);", lat, lng, zoom));
        } catch (Exception e) {}
    }

    private void centrerSurAdresse(String adresse, double fallbackLat, double fallbackLng) {
        if (!cartePret) return;
        try {
            String js = "geocodeAndCenter('" + adresse.replace("'", "\\'") + "', " + fallbackLat + ", " + fallbackLng + ");";
            webEngine.executeScript(js);
        } catch (Exception e) {}
    }

    private String getMapHTML() {
        return "<!DOCTYPE html><html><head>" +
               "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />" +
               "<script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>" +
               "<style>#map { height: 100vh; width: 100%; margin: 0; padding: 0; } .custom-div-icon { background: transparent; border: none; }</style>" +
               "</head><body style=\"margin:0;\"><div id=\"map\"></div><script>" +
               "var map = L.map('map').setView([36.8065, 10.1815], 8);" +
               "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OpenStreetMap' }).addTo(map);" +
               "var garageIcon = L.divIcon({ className: 'custom-div-icon', html: '<div style=\"background-color: #4B8B3B; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px;\">🏠</div>', iconSize: [30, 30], popupAnchor: [0, -15] });" +
               "function getColoredIcon(color) { return L.divIcon({ className: 'custom-div-icon', html: '<div style=\"background-color: ' + color + '; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;\"></div>', iconSize: [20, 20] }); }" +
               "function geocodeAndCenter(address, fallbackLat, fallbackLng) {" +
               "  fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(address))" +
               "  .then(function(res) { return res.json(); })" +
               "  .then(function(data) { " +
               "    if(data && data.length > 0) { map.setView([data[0].lat, data[0].lon], 15); }" +
               "    else { map.setView([fallbackLat, fallbackLng], 15); }" +
               "  }).catch(function() { map.setView([fallbackLat, fallbackLng], 15); });" +
               "}" +
               "</script></body></html>";
    }

    @FXML
    private void retourDashboard() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/dashboard/user_dashboard.fxml"));
            javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) cbGarages.getScene().getRoot();
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) borderPane.getCenter();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

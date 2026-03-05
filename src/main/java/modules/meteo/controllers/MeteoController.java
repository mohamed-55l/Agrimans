package modules.meteo.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import modules.meteo.models.AlerteMeteo;
import modules.meteo.models.PrevisionMeteo;
import modules.meteo.services.MeteoService;
import modules.meteo.services.NotificationService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MeteoController implements Initializable {

    @FXML private TextField tfVille;
    @FXML private Button btnRechercher;
    @FXML private Button btnActualiser;

    // Météo actuelle
    @FXML private Label lblVille;
    @FXML private Label lblDate;
    @FXML private Label lblTemperature;
    @FXML private Label lblDescription;
    @FXML private Label lblRessentie;
    @FXML private Label lblHumidite;
    @FXML private Label lblPression;
    @FXML private Label lblVent;
    @FXML private Label lblPluie;
    @FXML private Label lblConseil;

    // Prévisions
    @FXML private HBox previsionsContainer;

    // Graphique
    @FXML private LineChart<String, Number> chartTemperatures;
    @FXML private NumberAxis yAxis;

    // Alertes
    @FXML private TableView<AlerteMeteo> tableAlertes;
    @FXML private TableColumn<AlerteMeteo, String> colAlerteIcone;
    @FXML private TableColumn<AlerteMeteo, String> colAlerteTitre;
    @FXML private TableColumn<AlerteMeteo, String> colAlerteMessage;
    @FXML private TableColumn<AlerteMeteo, String> colAlerteDate;
    @FXML private TableColumn<AlerteMeteo, String> colAlerteNiveau;

    private MeteoService meteoService;
    private NotificationService notificationService;
    private ObservableList<AlerteMeteo> alertesList;
    private Timeline actualisationAuto;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        meteoService = new MeteoService();
        notificationService = new NotificationService();
        alertesList = FXCollections.observableArrayList();

        configurerTableAlertes();

        // Ville par défaut (Tunis)
        tfVille.setText("Tunis");
        rechercherMeteo();

        // Actualisation automatique toutes les 30 minutes
        actualisationAuto = new Timeline(
                new KeyFrame(Duration.minutes(30), e -> rechercherMeteo())
        );
        actualisationAuto.setCycleCount(Timeline.INDEFINITE);
        actualisationAuto.play();
    }

    private void configurerTableAlertes() {
        colAlerteIcone.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIcone()));
        colAlerteTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colAlerteMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colAlerteDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDatePrevue().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                ));
        colAlerteNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));

        colAlerteNiveau.setCellFactory(tc -> new TableCell<AlerteMeteo, String>() {
            @Override
            protected void updateItem(String niveau, boolean empty) {
                super.updateItem(niveau, empty);
                if (empty || niveau == null) {
                    setText(null);
                } else {
                    setText(niveau);
                    AlerteMeteo alerte = getTableView().getItems().get(getIndex());
                    setStyle("-fx-text-fill: " + alerte.getCouleur() + "; -fx-font-weight: bold;");
                }
            }
        });

        tableAlertes.setItems(alertesList);

        // Double-clic pour marquer comme lue
        tableAlertes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                AlerteMeteo alerte = tableAlertes.getSelectionModel().getSelectedItem();
                if (alerte != null) {
                    alerte.setLue(true);
                    tableAlertes.refresh();
                }
            }
        });
    }

    @FXML
    private void rechercherMeteo() {
        String ville = tfVille.getText().trim();
        if (ville.isEmpty()) {
            AlertUtils.showWarning("Attention", "Veuillez saisir une ville");
            return;
        }

        try {
            // Météo actuelle
            PrevisionMeteo actuelle = meteoService.getMeteoActuelle(ville);
            if (actuelle != null) {
                afficherMeteoActuelle(actuelle);
            }

            // Prévisions
            List<PrevisionMeteo> previsions = meteoService.getPrevisions(ville);
            if (!previsions.isEmpty()) {
                afficherPrevisions(previsions);
                afficherGraphique(previsions);
            }

            // Alertes
            alertesList.setAll(meteoService.getAlertesNonLues());

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de récupérer les données météo");
        }
    }

    private void afficherMeteoActuelle(PrevisionMeteo meteo) {
        lblVille.setText(meteo.getVille());
        lblDate.setText(meteo.getDateFormatee());
        lblTemperature.setText(String.format("%.1f°C", meteo.getTemperature()));
        lblDescription.setText(meteo.getDescription());
        lblRessentie.setText(String.format("Ressentie: %.1f°C", meteo.getTemperatureRessentie()));
        lblHumidite.setText(String.format("Humidité: %d%%", meteo.getHumidite()));
        lblPression.setText(String.format("Pression: %.0f hPa", meteo.getPression()));

        String direction = meteoService.getDirectionVentTexte(meteo.getDirectionVent());
        lblVent.setText(String.format("Vent: %.0f km/h (%s)",
                meteo.getVitesseVent() * 3.6, direction));

        if (meteo.getProbabilitePluie() > 0) {
            lblPluie.setText(String.format("Pluie: %.0f%% (%.1f mm)",
                    meteo.getProbabilitePluie(), meteo.getQuantitePluie()));
        } else {
            lblPluie.setText("Pluie: Aucune");
        }

        lblConseil.setText(meteo.getConseilAgricole());
    }

    private void afficherPrevisions(List<PrevisionMeteo> previsions) {
        previsionsContainer.getChildren().clear();

        // Prendre une prévision toutes les 3 heures pour les 24 prochaines heures
        for (int i = 0; i < 8 && i < previsions.size(); i++) {
            PrevisionMeteo prev = previsions.get(i);

            VBox carte = new VBox(5);
            carte.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
            carte.setPrefWidth(100);

            Label heure = new Label(prev.getHeureFormatee());
            heure.setStyle("-fx-font-weight: bold;");

            Label icone = new Label(getIconeEmoji(prev.getIcone()));
            icone.setStyle("-fx-font-size: 24px;");

            Label temp = new Label(String.format("%.1f°C", prev.getTemperature()));

            Label pluie = new Label(String.format("%.0f%%", prev.getProbabilitePluie()));
            if (prev.vaPleuvoir()) {
                pluie.setStyle("-fx-text-fill: #3498db;");
            }

            carte.getChildren().addAll(heure, icone, temp, pluie);
            previsionsContainer.getChildren().add(carte);
        }
    }

    private void afficherGraphique(List<PrevisionMeteo> previsions) {
        chartTemperatures.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Température (°C)");

        // Prendre une valeur toutes les 3 heures pour 24h
        for (int i = 0; i < 8 && i < previsions.size(); i++) {
            PrevisionMeteo prev = previsions.get(i);
            series.getData().add(new XYChart.Data<>(
                    prev.getHeureFormatee(),
                    prev.getTemperature()
            ));
        }

        chartTemperatures.getData().add(series);
    }

    private String getIconeEmoji(String iconeCode) {
        switch(iconeCode) {
            case "01d": return "☀️";
            case "01n": return "🌙";
            case "02d": case "02n": return "⛅";
            case "03d": case "03n": return "☁️";
            case "04d": case "04n": return "☁️☁️";
            case "09d": case "09n": return "🌧️";
            case "10d": case "10n": return "🌦️";
            case "11d": case "11n": return "⛈️";
            case "13d": case "13n": return "❄️";
            case "50d": case "50n": return "🌫️";
            default: return "☀️";
        }
    }

    @FXML
    private void actualiser() {
        rechercherMeteo();
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard/admin_dashboard.fxml"));
            Stage stage = (Stage) tfVille.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au dashboard");
        }
    }
}
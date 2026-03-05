package modules.meteo.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
import java.util.*;
import java.util.prefs.Preferences;

public class MeteoController implements Initializable {

    @FXML private TextField tfVille;
    @FXML private ComboBox<String> cbSuggestions;
    @FXML private ListView<String> listHistorique;
    @FXML private ListView<String> listFavoris;
    @FXML private Button btnRechercher;
    @FXML private Button btnMaPosition;
    @FXML private Button btnAjouterFavoris;
    @FXML private Button btnSupprimerFavoris;
    @FXML private Label lblStatus;

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

    @FXML private HBox previsionsContainer;
    @FXML private TableView<AlerteMeteo> tableAlertes;
    @FXML private TableColumn<AlerteMeteo, String> colIcone;
    @FXML private TableColumn<AlerteMeteo, String> colTitre;
    @FXML private TableColumn<AlerteMeteo, String> colMessage;
    @FXML private TableColumn<AlerteMeteo, String> colDate;

    private MeteoService meteoService;
    private NotificationService notificationService;
    private Timer timerMeteo;
    private Stage primaryStage;
    private Preferences prefs;
    private ObservableList<String> historiqueList;
    private ObservableList<String> favorisList;
    private ObservableList<AlerteMeteo> alertesList;
    private List<String> suggestionsVilles;

    private final String[] VILLES_AGRICOLES = {
            "Tunis", "Sfax", "Sousse", "Bizerte", "Béja", "Jendouba",
            "Kairouan", "Monastir", "Nabeul", "Gabès", "Gafsa", "Kef",
            "Mahdia", "Médenine", "Siliana", "Zaghouan"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        meteoService = new MeteoService();
        Platform.runLater(() -> {
            primaryStage = (Stage) tfVille.getScene().getWindow();
            notificationService = new NotificationService(primaryStage);
        });

        prefs = Preferences.userNodeForPackage(MeteoController.class);
        historiqueList = FXCollections.observableArrayList();
        favorisList = FXCollections.observableArrayList();
        alertesList = FXCollections.observableArrayList();
        suggestionsVilles = new ArrayList<>(Arrays.asList(VILLES_AGRICOLES));

        listHistorique.setItems(historiqueList);
        listFavoris.setItems(favorisList);
        tableAlertes.setItems(alertesList);

        configurerTableAlertes();
        chargerHistorique();
        chargerFavoris();
        configurerAutoCompletion();
        configurerRaccourcis();

        if (!favorisList.isEmpty()) {
            tfVille.setText(favorisList.get(0));
            rechercherMeteo();
        } else {
            tfVille.setText("Tunis");
            rechercherMeteo();
        }

        demarrerSurveillanceMeteo();
    }

    private void configurerTableAlertes() {
        colIcone.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIcone()));
        colTitre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitre()));
        colMessage.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMessage()));
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDateFormatee()));

        tableAlertes.setRowFactory(tv -> new TableRow<AlerteMeteo>() {
            @Override
            protected void updateItem(AlerteMeteo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: " + item.getCouleur() + "20;");
                }
            }
        });
    }

    private void configurerAutoCompletion() {
        cbSuggestions.setItems(FXCollections.observableArrayList(suggestionsVilles));
        cbSuggestions.setVisible(false);
        cbSuggestions.setManaged(false);

        tfVille.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.length() >= 2) {
                List<String> filtered = suggestionsVilles.stream()
                        .filter(v -> v.toLowerCase().contains(newValue.toLowerCase()))
                        .toList();

                if (!filtered.isEmpty()) {
                    cbSuggestions.setItems(FXCollections.observableArrayList(filtered));
                    cbSuggestions.setVisible(true);
                    cbSuggestions.setManaged(true);
                    cbSuggestions.show();
                } else {
                    cbSuggestions.setVisible(false);
                    cbSuggestions.setManaged(false);
                }
            } else {
                cbSuggestions.setVisible(false);
                cbSuggestions.setManaged(false);
            }
        });

        cbSuggestions.setOnAction(e -> {
            String selected = cbSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tfVille.setText(selected);
                cbSuggestions.setVisible(false);
                cbSuggestions.setManaged(false);
                rechercherMeteo();
            }
        });

        tfVille.focusedProperty().addListener((obs, old, newValue) -> {
            if (!newValue) {
                PauseTransition pause = new PauseTransition(Duration.millis(200));
                pause.setOnFinished(e -> {
                    cbSuggestions.setVisible(false);
                    cbSuggestions.setManaged(false);
                });
                pause.play();
            }
        });
    }

    private void configurerRaccourcis() {
        tfVille.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                rechercherMeteo();
            } else if (event.getCode() == KeyCode.DOWN && cbSuggestions.isVisible()) {
                cbSuggestions.requestFocus();
            }
        });

        cbSuggestions.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String selected = cbSuggestions.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tfVille.setText(selected);
                    cbSuggestions.setVisible(false);
                    rechercherMeteo();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cbSuggestions.setVisible(false);
                tfVille.requestFocus();
            }
        });
    }

    private void ajouterHistorique(String ville) {
        if (!historiqueList.contains(ville)) {
            historiqueList.add(0, ville);
            if (historiqueList.size() > 10) historiqueList.remove(10);
            sauvegarderHistorique();
        }
    }

    private void sauvegarderHistorique() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String v : historiqueList) {
                if (sb.length() > 0) sb.append(",");
                sb.append(v);
            }
            prefs.put("meteo.historique", sb.toString());
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde: " + e.getMessage());
        }
    }

    private void chargerHistorique() {
        try {
            String historique = prefs.get("meteo.historique", "");
            if (!historique.isEmpty()) {
                String[] villes = historique.split(",");
                historiqueList.addAll(Arrays.asList(villes));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement: " + e.getMessage());
        }
    }

    private void sauvegarderFavoris() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String v : favorisList) {
                if (sb.length() > 0) sb.append(",");
                sb.append(v);
            }
            prefs.put("meteo.favoris", sb.toString());
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde: " + e.getMessage());
        }
    }

    private void chargerFavoris() {
        try {
            String favoris = prefs.get("meteo.favoris", "");
            if (!favoris.isEmpty()) {
                String[] villes = favoris.split(",");
                favorisList.addAll(Arrays.asList(villes));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement: " + e.getMessage());
        }
    }

    @FXML
    private void rechercherVille(ActionEvent event) {
        Button source = (Button) event.getSource();
        tfVille.setText(source.getText());
        rechercherMeteo();
    }

    @FXML
    private void rechercherMeteo() {
        String ville = tfVille.getText().trim();
        if (ville.isEmpty()) {
            AlertUtils.showWarning("Attention", "Veuillez saisir une ville");
            return;
        }

        lblStatus.setText("🔍 Recherche de " + ville + "...");

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            PrevisionMeteo meteo = meteoService.getMeteoActuelle(ville);

            if (meteo != null) {
                lblVille.setText(meteo.getVille());
                lblDate.setText(meteo.getDateComplete());
                lblTemperature.setText(String.format("%.1f°C", meteo.getTemperature()));
                lblDescription.setText(meteo.getDescription());
                lblRessentie.setText(String.format("Ressentie: %.1f°C", meteo.getTemperatureRessentie()));
                lblHumidite.setText(String.format("Humidité: %d%%", meteo.getHumidite()));
                lblPression.setText(String.format("Pression: %.0f hPa", meteo.getPression()));
                lblVent.setText(String.format("Vent: %.0f km/h %s",
                        meteo.getVentKmh(), meteo.getDirectionVentTexte()));
                lblPluie.setText(String.format("Pluie: %.1f mm (%.0f%%)",
                        meteo.getQuantitePluie(), meteo.getProbabilitePluie()));

                genererConseil(meteo);
                genererPrevisions(ville);
                mettreAJourAlertes();

                ajouterHistorique(ville);
                lblStatus.setText("✅ Mise à jour: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                btnAjouterFavoris.setDisable(favorisList.contains(ville));
                btnSupprimerFavoris.setDisable(!favorisList.contains(ville));
            }
        });
        pause.play();
    }

    private void genererConseil(PrevisionMeteo meteo) {
        List<String> conseils = new ArrayList<>();
        double temp = meteo.getTemperature();
        int humidite = meteo.getHumidite();
        double vent = meteo.getVentKmh();
        double pluie = meteo.getQuantitePluie();

        if (temp < 5) conseils.add("❄️ Risque de gel - Protégez les cultures");
        if (temp > 35) conseils.add("☀️ Canicule - Arrosez le soir");
        if (pluie > 10) conseils.add("🌧️ Fortes pluies - Évitez les traitements");
        if (vent > 50) conseils.add("💨 Vent fort - Reportez les pulvérisations");
        if (humidite > 80 && temp > 15 && temp < 25)
            conseils.add("🦠 Risque mildiou - Traitement préventif");

        lblConseil.setText(conseils.isEmpty() ?
                "✅ Conditions favorables" : String.join("\n", conseils));
    }

    private void genererPrevisions(String ville) {
        previsionsContainer.getChildren().clear();
        List<PrevisionMeteo> previsions = meteoService.getPrevisions(ville);

        for (int i = 0; i < Math.min(5, previsions.size()); i++) {
            PrevisionMeteo prev = previsions.get(i);

            VBox box = new VBox(5);
            box.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 10;");
            box.setPrefWidth(100);
            box.setAlignment(javafx.geometry.Pos.CENTER);

            box.getChildren().addAll(
                    new Label(prev.getHeureFormatee()),
                    new Label(prev.getProbabilitePluie() > 30 ? "🌧️" : "☀️"),
                    new Label(String.format("%.1f°C", prev.getTemperature()))
            );

            previsionsContainer.getChildren().add(box);
        }
    }

    private void mettreAJourAlertes() {
        alertesList.setAll(meteoService.getAlertes());
    }

    private void demarrerSurveillanceMeteo() {
        timerMeteo = new Timer(true);
        timerMeteo.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (!tfVille.getText().isEmpty()) {
                        rechercherMeteo();
                    }
                });
            }
        }, 30 * 60 * 1000, 30 * 60 * 1000);
    }

    @FXML private void rechercherHistorique() {
        String selected = listHistorique.getSelectionModel().getSelectedItem();
        if (selected != null) { tfVille.setText(selected); rechercherMeteo(); }
    }

    @FXML private void rechercherFavori() {
        String selected = listFavoris.getSelectionModel().getSelectedItem();
        if (selected != null) { tfVille.setText(selected); rechercherMeteo(); }
    }

    @FXML private void ajouterFavoris() {
        String ville = tfVille.getText().trim();
        if (!ville.isEmpty() && !favorisList.contains(ville)) {
            favorisList.add(ville);
            sauvegarderFavoris();
            btnAjouterFavoris.setDisable(true);
            btnSupprimerFavoris.setDisable(false);
            AlertUtils.showInfo("Succès", ville + " ajouté aux favoris");
        }
    }

    @FXML private void supprimerFavoris() {
        String selected = listFavoris.getSelectionModel().getSelectedItem();
        if (selected != null) {
            favorisList.remove(selected);
            sauvegarderFavoris();
            btnAjouterFavoris.setDisable(favorisList.contains(tfVille.getText().trim()));
            btnSupprimerFavoris.setDisable(true);
            AlertUtils.showInfo("Succès", selected + " retiré des favoris");
        }
    }

    @FXML private void viderHistorique() {
        if (AlertUtils.showConfirmation("Confirmation", "Vider l'historique ?")) {
            historiqueList.clear();
            sauvegarderHistorique();
        }
    }

    @FXML private void marquerToutesLues() {
        meteoService.marquerToutesLues();
        tableAlertes.refresh();
    }

    @FXML private void utiliserMaPosition() {
        lblStatus.setText("📍 Recherche de votre position...");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> { tfVille.setText("Tunis"); rechercherMeteo(); });
        pause.play();
    }

    @FXML private void retourDashboard() {
        if (timerMeteo != null) timerMeteo.cancel();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard/admin_dashboard.fxml"));
            ((Stage) tfVille.getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner");
        }
    }
}
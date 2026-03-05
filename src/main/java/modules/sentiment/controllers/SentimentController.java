package modules.sentiment.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import modules.review.models.Review;
import modules.review.services.ReviewService;
import modules.sentiment.models.AnalyseSentiment;
import modules.sentiment.services.SentimentNotificationService;
import modules.sentiment.services.SentimentService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class SentimentController implements Initializable {

    @FXML private TableView<AnalyseSentiment> tableAnalyses;
    @FXML private TableColumn<AnalyseSentiment, Integer> colId;
    @FXML private TableColumn<AnalyseSentiment, String> colIcone;
    @FXML private TableColumn<AnalyseSentiment, String> colCommentaire;
    @FXML private TableColumn<AnalyseSentiment, String> colSentiment;
    @FXML private TableColumn<AnalyseSentiment, Double> colScorePositif;
    @FXML private TableColumn<AnalyseSentiment, Double> colScoreNegatif;
    @FXML private TableColumn<AnalyseSentiment, String> colMotsCles;
    @FXML private TableColumn<AnalyseSentiment, String> colDate;

    @FXML private Label lblTotalAnalyses;
    @FXML private Label lblPositifs;
    @FXML private Label lblNegatifs;
    @FXML private Label lblNeutres;
    @FXML private Label lblWarnings;
    @FXML private ComboBox<String> cbFiltre;
    @FXML private Button btnAnalyserToutes;

    private SentimentService sentimentService;
    private SentimentNotificationService notificationService;
    private ReviewService reviewService;
    private ObservableList<AnalyseSentiment> analyseList;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        sentimentService = new SentimentService();
        reviewService = new ReviewService();
        analyseList = FXCollections.observableArrayList();

        javafx.application.Platform.runLater(() -> {
            primaryStage = (Stage) btnAnalyserToutes.getScene().getWindow();
            notificationService = new SentimentNotificationService(primaryStage);
            
            chargerAnalyses();
        });

        configurerTableau();
        configurerFiltre();
    }

    private void configurerTableau() {
        colId.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        colIcone.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIcone()));
        colCommentaire.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        tronquer(cellData.getValue().getCommentaire(), 50)));
        colSentiment.setCellValueFactory(new PropertyValueFactory<>("sentiment"));
        colScorePositif.setCellValueFactory(new PropertyValueFactory<>("scorePositif"));
        colScoreNegatif.setCellValueFactory(new PropertyValueFactory<>("scoreNegatif"));
        colMotsCles.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.join(", ", cellData.getValue().getMotsClesNegatifs())));
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateAnalyse().toLocalDate().toString()));

        // Formatage des scores en pourcentage
        colScorePositif.setCellFactory(tc -> new TableCell<AnalyseSentiment, Double>() {
            @Override
            protected void updateItem(Double score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) setText(null);
                else setText(String.format("%.0f%%", score * 100));
            }
        });

        colScoreNegatif.setCellFactory(tc -> new TableCell<AnalyseSentiment, Double>() {
            @Override
            protected void updateItem(Double score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) setText(null);
                else setText(String.format("%.0f%%", score * 100));
            }
        });

        // Colorer les lignes selon le sentiment
        tableAnalyses.setRowFactory(tv -> new TableRow<AnalyseSentiment>() {
            @Override
            protected void updateItem(AnalyseSentiment item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: " + item.getCouleur() + "20;");
                }
            }
        });

        tableAnalyses.setItems(analyseList);
    }

    private void configurerFiltre() {
        cbFiltre.getItems().addAll("Tous", "Warnings ⚠️", "Positifs 😊", "Négatifs 😞", "Neutres 😐");
        cbFiltre.setValue("Tous");

        cbFiltre.valueProperty().addListener((obs, old, newValue) -> {
            filtrerAnalyses(newValue);
        });
    }

    private void chargerAnalyses() {
        try {
            List<Review> reviews = reviewService.getAll();
            List<AnalyseSentiment> analyses = sentimentService.analyserToutes(reviews);
            analyseList.setAll(analyses);

            mettreAJourStatistiques(analyses);

            // Vérifier les warnings
            List<AnalyseSentiment> warnings = analyses.stream()
                    .filter(AnalyseSentiment::isWarning)
                    .toList();

            if (!warnings.isEmpty()) {
                notificationService.notifierWarningsGroupes(warnings, reviews);

                // Notification individuelle pour chaque warning
                for (AnalyseSentiment warning : warnings) {
                    Review review = reviews.stream()
                            .filter(r -> r.getId() == warning.getReviewId())
                            .findFirst().orElse(null);

                    if (review != null) {
                        notificationService.notifierWarning(warning, review);

                        // Alerte popup pour les plus critiques
                        if (warning.getScoreNegatif() > 0.5) {
                            notificationService.alerterWarningCritique(warning, review);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les reviews");
        }
    }

    private void mettreAJourStatistiques(List<AnalyseSentiment> analyses) {
        long total = analyses.size();
        long positifs = analyses.stream().filter(a -> "POSITIF".equals(a.getSentiment())).count();
        long negatifs = analyses.stream().filter(a -> "NEGATIF".equals(a.getSentiment())).count();
        long neutres = analyses.stream().filter(a -> "NEUTRE".equals(a.getSentiment())).count();
        long warnings = analyses.stream().filter(AnalyseSentiment::isWarning).count();

        lblTotalAnalyses.setText(String.valueOf(total));
        lblPositifs.setText(positifs + " (😊)");
        lblNegatifs.setText(negatifs + " (😞)");
        lblNeutres.setText(neutres + " (😐)");
        lblWarnings.setText(warnings + " ⚠️");
    }

    private void filtrerAnalyses(String filtre) {
        if ("Tous".equals(filtre)) {
            tableAnalyses.setItems(analyseList);
        } else if ("Warnings ⚠️".equals(filtre)) {
            ObservableList<AnalyseSentiment> filtered = FXCollections.observableArrayList(
                    analyseList.stream().filter(AnalyseSentiment::isWarning).toList()
            );
            tableAnalyses.setItems(filtered);
        } else if ("Positifs 😊".equals(filtre)) {
            ObservableList<AnalyseSentiment> filtered = FXCollections.observableArrayList(
                    analyseList.stream().filter(a -> "POSITIF".equals(a.getSentiment())).toList()
            );
            tableAnalyses.setItems(filtered);
        } else if ("Négatifs 😞".equals(filtre)) {
            ObservableList<AnalyseSentiment> filtered = FXCollections.observableArrayList(
                    analyseList.stream().filter(a -> "NEGATIF".equals(a.getSentiment())).toList()
            );
            tableAnalyses.setItems(filtered);
        } else if ("Neutres 😐".equals(filtre)) {
            ObservableList<AnalyseSentiment> filtered = FXCollections.observableArrayList(
                    analyseList.stream().filter(a -> "NEUTRE".equals(a.getSentiment())).toList()
            );
            tableAnalyses.setItems(filtered);
        }
    }

    @FXML
    private void analyserToutes() {
        chargerAnalyses();
        AlertUtils.showInfo("Analyse terminée", "Toutes les reviews ont été analysées");
    }

    @FXML
    private void voirDetails() {
        AnalyseSentiment selected = tableAnalyses.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Afficher les détails dans une popup
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails de l'analyse");
            alert.setHeaderText("Analyse du commentaire");

            String content = String.format(
                    "Commentaire: \"%s\"\n\n" +
                            "Sentiment: %s %s\n" +
                            "Score positif: %.0f%%\n" +
                            "Score négatif: %.0f%%\n" +
                            "Score neutre: %.0f%%\n\n" +
                            "Mots négatifs détectés: %s\n" +
                            "Warning: %s",
                    selected.getCommentaire(),
                    selected.getIcone(), selected.getSentiment(),
                    selected.getScorePositif() * 100,
                    selected.getScoreNegatif() * 100,
                    selected.getScoreNeutre() * 100,
                    selected.getMotsClesNegatifs().isEmpty() ? "Aucun" :
                            String.join(", ", selected.getMotsClesNegatifs()),
                    selected.isWarning() ? "⚠️ OUI" : "NON"
            );

            alert.setContentText(content);
            alert.show();
        }
    }

    private String tronquer(String texte, int maxLength) {
        if (texte == null) return "";
        if (texte.length() <= maxLength) return texte;
        return texte.substring(0, maxLength) + "...";
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard/admin_dashboard.fxml"));
            Stage stage = (Stage) btnAnalyserToutes.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au dashboard");
        }
    }
}
package modules.dashboard.controllers;

import modules.equipement.models.Equipement;
import modules.review.models.Review;
import modules.equipement.services.EquipementService;
import modules.review.services.ReviewService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;

public class DashboardController {

    @FXML private TableView<Equipement> tableEquipement;
    @FXML private TableColumn<Equipement, String> colEquipNom;
    @FXML private TableColumn<Equipement, String> colEquipType;
    @FXML private TableColumn<Equipement, Float> colEquipPrix;
    @FXML private TableColumn<Equipement, String> colEquipEtat;

    @FXML private TableView<Review> tableReview;
    @FXML private TableColumn<Review, String> colReviewCommentaire;
    @FXML private TableColumn<Review, Float> colReviewNote;
    @FXML private TableColumn<Review, String> colReviewEquipement;
    @FXML private TableColumn<Review, String> colReviewDate;

    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();

    @FXML
    public void initialize() {
        try {
            // Configuration des colonnes Équipement
            colEquipNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colEquipType.setCellValueFactory(new PropertyValueFactory<>("type"));
            colEquipPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
            colEquipEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

            // Formatage du prix
            colEquipPrix.setCellFactory(tc -> new TableCell<Equipement, Float>() {
                @Override
                protected void updateItem(Float price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f DT", price));
                    }
                }
            });

            // Charger les données des équipements
            ObservableList<Equipement> equipements = FXCollections.observableArrayList();
            equipements.addAll(equipementService.getAll());
            tableEquipement.setItems(equipements);

            // Configuration des colonnes Review
            colReviewCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
            colReviewNote.setCellValueFactory(new PropertyValueFactory<>("note"));

            // Formatage de la note
            colReviewNote.setCellFactory(tc -> new TableCell<Review, Float>() {
                @Override
                protected void updateItem(Float note, boolean empty) {
                    super.updateItem(note, empty);
                    if (empty || note == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.1f ⭐", note));
                    }
                }
            });

            // Pour le nom de l'équipement
            colReviewEquipement.setCellValueFactory(cellData -> {
                Review review = cellData.getValue();
                if (review != null && review.getEquipement() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            review.getEquipement().getNom()
                    );
                } else {
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                }
            });

            // Pour la date
            colReviewDate.setCellValueFactory(cellData -> {
                Review review = cellData.getValue();
                if (review != null && review.getDateReview() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            review.getDateReview().toString()
                    );
                } else {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
            });

            // Charger les données des reviews
            ObservableList<Review> reviews = FXCollections.observableArrayList();
            reviews.addAll(reviewService.getAll());
            tableReview.setItems(reviews);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    @FXML
    void handleButtonEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #7DBF6C; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand;");
    }

    @FXML
    void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #4B8B3B; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
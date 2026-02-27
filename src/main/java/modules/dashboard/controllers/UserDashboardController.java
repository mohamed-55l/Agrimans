package modules.dashboard.controllers;

import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.controllers.ReviewController;
import modules.review.models.Review;
import modules.review.services.ReviewService;
// import modules.user.models.User;
// import modules.user.services.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class UserDashboardController {

    @FXML private Label lblUserName;
    @FXML private Label lblMesEquipements;
    @FXML private Label lblMesReviews;
    @FXML private Label lblMesPannes;
    @FXML private Label lblValeurTotale;

    @FXML private TableView<Equipement> tableMesEquipements;
    @FXML private TableView<Review> tableMesReviews;

    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();

    private int currentUserId = 1; // Temporaire - À remplacer par l'utilisateur connecté
    private String currentUserName = "Jean Agriculteur"; // Temporaire

    @FXML
    public void initialize() {
        // Afficher le nom
        lblUserName.setText(currentUserName);

        setupEquipementTable();
        setupReviewTable();
        loadStatistics();
        loadMesEquipements();
        loadMesReviews();
    }

    private void setupEquipementTable() {
        TableColumn<Equipement, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(200);

        TableColumn<Equipement, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(150);

        TableColumn<Equipement, Float> colPrix = new TableColumn<>("Prix");
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colPrix.setPrefWidth(120);
        colPrix.setCellFactory(tc -> new TableCell<Equipement, Float>() {
            @Override
            protected void updateItem(Float prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) setText(null);
                else setText(String.format("%.2f DT", prix));
            }
        });

        TableColumn<Equipement, String> colEtat = new TableColumn<>("État");
        colEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
        colEtat.setPrefWidth(100);

        TableColumn<Equipement, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(200);
        colAction.setCellFactory(param -> new TableCell<Equipement, Void>() {
            private final Button btnReview = new Button("⭐ Ajouter review");
            {
                btnReview.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnReview.setOnAction(event -> {
                    Equipement equipement = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireReview(equipement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnReview);
                }
            }
        });

        tableMesEquipements.getColumns().addAll(colNom, colType, colPrix, colEtat, colAction);
    }

    private void setupReviewTable() {
        TableColumn<Review, String> colEquip = new TableColumn<>("Équipement");
        colEquip.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            if (r.getEquipement() != null) {
                return new javafx.beans.property.SimpleStringProperty(r.getEquipement().getNom());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        colEquip.setPrefWidth(150);

        TableColumn<Review, String> colCommentaire = new TableColumn<>("Commentaire");
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colCommentaire.setPrefWidth(300);

        TableColumn<Review, Float> colNote = new TableColumn<>("Note");
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colNote.setPrefWidth(100);
        colNote.setCellFactory(tc -> new TableCell<Review, Float>() {
            @Override
            protected void updateItem(Float note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) setText(null);
                else setText(String.format("%.1f ⭐", note));
            }
        });

        TableColumn<Review, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            if (r.getDateReview() != null) {
                return new javafx.beans.property.SimpleStringProperty(r.getDateReview().toString());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colDate.setPrefWidth(120);

        tableMesReviews.getColumns().addAll(colEquip, colCommentaire, colNote, colDate);
    }

    private void loadStatistics() {
        try {
            List<Equipement> mesEquipements = equipementService.getByUserId(currentUserId);
            List<Review> mesReviews = reviewService.getByUserId(currentUserId); // À implémenter

            // Nombre d'équipements
            lblMesEquipements.setText(String.valueOf(mesEquipements.size()));

            // Nombre de reviews
            lblMesReviews.setText(String.valueOf(mesReviews.size()));

            // Équipements en panne
            long enPanne = mesEquipements.stream()
                    .filter(e -> "Non disponible".equals(e.getDisponibilite()) || "En maintenance".equals(e.getDisponibilite()))
                    .count();
            lblMesPannes.setText(String.valueOf(enPanne));

            // Valeur totale
            double total = mesEquipements.stream()
                    .mapToDouble(Equipement::getPrix)
                    .sum();
            lblValeurTotale.setText(String.format("%.2f DT", total));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMesEquipements() {
        try {
            ObservableList<Equipement> data = FXCollections.observableArrayList(
                    equipementService.getByUserId(currentUserId)
            );
            tableMesEquipements.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMesReviews() {
        try {
            // Maintenant que getByUserId() existe, ça fonctionne
            ObservableList<Review> data = FXCollections.observableArrayList(
                    reviewService.getByUserId(currentUserId)  // ← Plus d'erreur !
            );
            tableMesReviews.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger vos reviews: " + e.getMessage());
        }
    }

    // Ajouter cette méthode utilitaire pour les alertes
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void ouvrirFormulaireReview(Equipement equipement) {
        try {
            // Ouvrir la fenêtre d'ajout de review avec l'équipement présélectionné
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/review/review.fxml"));
            Parent root = loader.load();

            ReviewController controller = loader.getController();
            controller.setEquipement(equipement); // À implémenter

            Stage stage = new Stage();
            stage.setTitle("Ajouter une review - " + equipement.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAjoutEquipement() {
        try {
            // Naviguer vers la page d'ajout d'équipement
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/equipement/equipement.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblUserName.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
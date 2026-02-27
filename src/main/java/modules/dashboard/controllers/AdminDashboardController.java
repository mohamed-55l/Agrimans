package modules.dashboard.controllers;

import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.models.Review;
import modules.review.services.ReviewService;
// Importer le service User (quand il sera prêt)
// import modules.user.services.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label lblTotalEquipements;
    @FXML private Label lblEnPanne;
    @FXML private Label lblAgriculteurs;
    @FXML private Label lblNoteMoyenne;

    @FXML private TableView<Equipement> tableEquipement;
    @FXML private TableView<Review> tableReview;

    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();
    // private UserService userService = new UserService(); // Quand dispo

    @FXML
    public void initialize() {
        setupEquipementTable();
        setupReviewTable();
        loadStatistics();
        loadEquipements();
        loadReviews();
    }

    private void setupEquipementTable() {
        // Configurer les colonnes
        TableColumn<Equipement, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Equipement, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Equipement, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Equipement, Float> colPrix = new TableColumn<>("Prix");
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
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

        TableColumn<Equipement, String> colProprio = new TableColumn<>("Propriétaire");
        colProprio.setCellValueFactory(cellData -> {
            Equipement e = cellData.getValue();
            String proprio = "User #" + e.getUserId(); // Temporaire
            return new javafx.beans.property.SimpleStringProperty(proprio);
        });

        TableColumn<Equipement, String> colContact = new TableColumn<>("Contact");
        colContact.setCellValueFactory(cellData -> {
            // À remplacer quand User sera dispo
            return new javafx.beans.property.SimpleStringProperty("Non dispo");
        });

        tableEquipement.getColumns().addAll(colId, colNom, colType, colPrix, colEtat, colProprio, colContact);
    }

    private void setupReviewTable() {
        TableColumn<Review, String> colCommentaire = new TableColumn<>("Commentaire");
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

        TableColumn<Review, Float> colNote = new TableColumn<>("Note");
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colNote.setCellFactory(tc -> new TableCell<Review, Float>() {
            @Override
            protected void updateItem(Float note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) setText(null);
                else setText(String.format("%.1f ⭐", note));
            }
        });

        TableColumn<Review, String> colEquip = new TableColumn<>("Équipement");
        colEquip.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            if (r.getEquipement() != null) {
                return new javafx.beans.property.SimpleStringProperty(r.getEquipement().getNom());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        TableColumn<Review, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            if (r.getDateReview() != null) {
                return new javafx.beans.property.SimpleStringProperty(r.getDateReview().toString());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        TableColumn<Review, String> colAuteur = new TableColumn<>("Auteur");
        colAuteur.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("User #" + r.getUserId());
        });

        tableReview.getColumns().addAll(colCommentaire, colNote, colEquip, colDate, colAuteur);
    }

    private void loadStatistics() {
        try {
            List<Equipement> equipements = equipementService.getAll();
            List<Review> reviews = reviewService.getAll();

            // Total équipements
            lblTotalEquipements.setText(String.valueOf(equipements.size()));

            // Équipements en panne
            long enPanne = equipements.stream()
                    .filter(e -> "Non disponible".equals(e.getDisponibilite()) || "En maintenance".equals(e.getDisponibilite()))
                    .count();
            lblEnPanne.setText(String.valueOf(enPanne));

            // Note moyenne
            double moyenne = reviews.stream()
                    .mapToDouble(Review::getNote)
                    .average()
                    .orElse(0);
            lblNoteMoyenne.setText(String.format("%.1f", moyenne));

            // Agriculteurs (quand User sera dispo)
            // lblAgriculteurs.setText(String.valueOf(userService.countUsers()));
            lblAgriculteurs.setText("3"); // Temporaire

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEquipements() {
        try {
            ObservableList<Equipement> data = FXCollections.observableArrayList(equipementService.getAll());
            tableEquipement.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadReviews() {
        try {
            ObservableList<Review> data = FXCollections.observableArrayList(reviewService.getAll());
            tableReview.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
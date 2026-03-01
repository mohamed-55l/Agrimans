package modules.dashboard.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import modules.dashboard.services.DashboardService;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.models.Review;
import modules.review.services.ReviewService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Contrôleur pour le dashboard USER (Agriculteur)
 *
 * RÔLE: Afficher les données personnelles de l'agriculteur
 * Accès: Réservé aux agriculteurs
 */
public class UserDashboardController {

    // =====================================================
    // STATISTIQUES PERSONNELLES
    // =====================================================

    @FXML private Label lblBienvenue;
    @FXML private Label lblMesEquipements;
    @FXML private Label lblMesReviews;
    @FXML private Label lblMesPannes;
    @FXML private Label lblValeurTotale;

    // =====================================================
    // TABLEAUX PERSONNELS
    // =====================================================

    @FXML private TableView<Equipement> tableMesEquipements;
    @FXML private TableColumn<Equipement, String> colNom;
    @FXML private TableColumn<Equipement, String> colType;
    @FXML private TableColumn<Equipement, Float> colPrix;
    @FXML private TableColumn<Equipement, String> colEtat;
    @FXML private TableColumn<Equipement, Void> colAction;

    @FXML private TableView<Review> tableMesReviews;
    @FXML private TableColumn<Review, String> colReviewEquipement;
    @FXML private TableColumn<Review, String> colReviewCommentaire;
    @FXML private TableColumn<Review, Float> colReviewNote;
    @FXML private TableColumn<Review, String> colReviewDate;

    // =====================================================
    // SERVICES
    // =====================================================

    private DashboardService dashboardService = new DashboardService();
    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();

    private int userId;

    // =====================================================
    // INITIALISATION
    // =====================================================

    @FXML
    public void initialize() {
        // Vérifier les droits d'accès
        if (!SessionManager.isAgriculteur()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux agriculteurs");
            return;
        }

        // Récupérer l'ID de l'utilisateur connecté
        userId = SessionManager.getCurrentUserId();

        // Afficher le message de bienvenue personnalisé
        lblBienvenue.setText("🌾 Bonjour " + SessionManager.getCurrentUserName());

        // Configurer les tableaux
        configurerTableEquipements();
        configurerTableReviews();

        // Charger les données personnelles
        chargerStatistiques();
        chargerMesEquipements();
        chargerMesReviews();
    }

    /**
     * Configuration du tableau des équipements personnels
     */
    private void configurerTableEquipements() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        // Formatage du prix
        colPrix.setCellFactory(tc -> new javafx.scene.control.TableCell<Equipement, Float>() {
            @Override
            protected void updateItem(Float prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", prix));
                }
            }
        });

        // Colonne action avec bouton pour ajouter une review
        colAction.setCellFactory(param -> new TableCell<Equipement, Void>() {
            private final Button btnReview = new Button("⭐ Ajouter review");

            {
                btnReview.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5;");
                btnReview.setPrefWidth(120);

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
    }

    /**
     * Configuration du tableau des reviews personnelles
     */
    private void configurerTableReviews() {
        colReviewCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colReviewNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Formatage de la note
        colReviewNote.setCellFactory(tc -> new javafx.scene.control.TableCell<Review, Float>() {
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

        // Nom de l'équipement
        colReviewEquipement.setCellValueFactory(cellData -> {
            Review review = cellData.getValue();
            if (review != null && review.getEquipement() != null) {
                return new javafx.beans.property.SimpleStringProperty(review.getEquipement().getNom());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Date
        colReviewDate.setCellValueFactory(cellData -> {
            Review review = cellData.getValue();
            if (review != null && review.getDateReview() != null) {
                return new javafx.beans.property.SimpleStringProperty(review.getDateReview().toString());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
    }

    /**
     * Charger les statistiques personnelles
     */
    private void chargerStatistiques() {
        Map<String, Object> stats = dashboardService.getUserStats(userId);

        lblMesEquipements.setText(String.valueOf(stats.get("mesEquipements")));
        lblMesReviews.setText(String.valueOf(stats.get("mesReviews")));
        lblMesPannes.setText(String.valueOf(stats.get("mesPannes")));
        lblValeurTotale.setText(stats.get("valeurTotale") + " DT");
    }

    /**
     * Charger mes équipements
     */
    private void chargerMesEquipements() {
        try {
            ObservableList<Equipement> data = FXCollections.observableArrayList(
                    equipementService.getByUserId(userId)
            );
            tableMesEquipements.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger vos équipements");
        }
    }

    /**
     * Charger mes reviews
     */
    private void chargerMesReviews() {
        try {
            ObservableList<Review> data = FXCollections.observableArrayList(
                    reviewService.getByUserId(userId)
            );
            tableMesReviews.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger vos reviews");
        }
    }

    /**
     * Ouvrir le formulaire de review pour un équipement spécifique
     */
    private void ouvrirFormulaireReview(Equipement equipement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/review/user_review.fxml"));
            Parent root = loader.load();

            UserReviewController controller = loader.getController();
            controller.setEquipement(equipement);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une review - " + equipement.getNom());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    /**
     * Aller à la page d'ajout d'équipement
     */
    @FXML
    private void allerVersAjoutEquipement() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/equipement/user_equipement.fxml"));
            Stage stage = (Stage) lblBienvenue.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger la page");
        }
    }
}
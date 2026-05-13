package modules.dashboard.controllers;

import modules.user.controllers.BaseController;
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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import modules.dashboard.services.DashboardService;
import modules.demande.controllers.DemandeController;
import modules.demande.models.Demande;
import modules.demande.services.DemandeService;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.controllers.UserReviewController;
import modules.review.models.Review;
import modules.review.services.ReviewService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class UserDashboardController extends BaseController implements Initializable {

    // =====================================================
    // STATISTIQUES
    // =====================================================

    @FXML private Label lblBienvenue;
    @FXML private Label lblMesEquipements;
    @FXML private Label lblMesReviews;
    @FXML private Label lblMesPannes;
    @FXML private Label lblValeurTotale;
    @FXML private Label lblDemandesEnCours;

    // =====================================================
    // TABLEAUX
    // =====================================================

    // Mes équipements
    @FXML private TableView<Equipement> tableMesEquipements;
    @FXML private TableColumn<Equipement, String> colEquipNom;
    @FXML private TableColumn<Equipement, String> colEquipType;
    @FXML private TableColumn<Equipement, Float> colEquipPrix;
    @FXML private TableColumn<Equipement, String> colEquipEtat;
    @FXML private TableColumn<Equipement, Void> colEquipAction;

    // Mes reviews
    @FXML private TableView<Review> tableMesReviews;
    @FXML private TableColumn<Review, String> colReviewEquipement;
    @FXML private TableColumn<Review, String> colReviewCommentaire;
    @FXML private TableColumn<Review, Float> colReviewNote;
    @FXML private TableColumn<Review, String> colReviewDate;
    @FXML private TableColumn<Review, Void> colReviewAction;

    // =====================================================
    // SERVICES
    // =====================================================

    private DashboardService dashboardService = new DashboardService();
    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();
    private DemandeService demandeService = new DemandeService();

    private int userId;

    // =====================================================
    // INITIALISATION
    // =====================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAgriculteur()) {
            AlertUtils.showError("Accès refusé", "Page réservée aux agriculteurs");
            return;
        }

        userId = SessionManager.getCurrentUserId();
        lblBienvenue.setText("🌾 Bonjour " + SessionManager.getCurrentUserName());

        configurerTableEquipements();
        configurerTableReviews();

        chargerStatistiques();
        chargerMesEquipements();
        chargerMesReviews();
    }

    // =====================================================
    // CONFIGURATION DES TABLEAUX
    // =====================================================

    private void configurerTableEquipements() {
        colEquipNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEquipType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colEquipPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEquipEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        colEquipPrix.setCellFactory(tc -> new TableCell<Equipement, Float>() {
            @Override
            protected void updateItem(Float prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) setText(null);
                else setText(String.format("%.2f DT", prix));
            }
        });

        colEquipEtat.setCellFactory(tc -> new TableCell<Equipement, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setText(null);
                } else {
                    setText(etat);
                    switch(etat) {
                        case "Disponible": setStyle("-fx-text-fill: #27ae60;"); break;
                        case "Non disponible": setStyle("-fx-text-fill: #e74c3c;"); break;
                        case "En maintenance": setStyle("-fx-text-fill: #f39c12;"); break;
                    }
                }
            }
        });

        colEquipAction.setCellFactory(param -> new TableCell<Equipement, Void>() {
            private final Button btnDemander = new Button("📝 Demander");
            private final Button btnReview = new Button("⭐ Review");
            private final HBox box = new HBox(5);

            {
                btnDemander.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");
                btnReview.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");

                btnDemander.setOnAction(event -> {
                    Equipement equipement = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireDemande(equipement);
                });

                btnReview.setOnAction(event -> {
                    Equipement equipement = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireReview(equipement);
                });

                box.getChildren().addAll(btnDemander, btnReview);
                box.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configurerTableReviews() {
        colReviewEquipement.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    r.getEquipement() != null ? r.getEquipement().getNom() : "N/A"
            );
        });
        colReviewCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colReviewNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colReviewDate.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    r.getDateReview() != null ? r.getDateReview().toString() : ""
            );
        });

        colReviewNote.setCellFactory(tc -> new TableCell<Review, Float>() {
            @Override
            protected void updateItem(Float note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) setText(null);
                else setText(String.format("%.1f ⭐", note));
            }
        });

        colReviewAction.setCellFactory(param -> new TableCell<Review, Void>() {
            private final Button btnModifier = new Button("✏️");
            private final Button btnSupprimer = new Button("🗑️");
            private final HBox box = new HBox(5);

            {
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");

                btnModifier.setOnAction(event -> {
                    Review review = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireReview(review);
                });

                btnSupprimer.setOnAction(event -> {
                    Review review = getTableView().getItems().get(getIndex());
                    supprimerReview(review);
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
    }

    // =====================================================
    // CHARGEMENT DES DONNÉES
    // =====================================================

    private void chargerStatistiques() {
        try {
            Map<String, Object> stats = dashboardService.getUserStats(userId);

            lblMesEquipements.setText(String.valueOf(stats.get("mesEquipements")));
            lblMesReviews.setText(String.valueOf(stats.get("mesReviews")));
            lblMesPannes.setText(String.valueOf(stats.get("mesPannes")));
            lblValeurTotale.setText(stats.get("valeurTotale") + " DT");

            long demandesEnCours = demandeService.getByAgriculteurId(userId).stream()
                    .filter(d -> "EN_ATTENTE".equals(d.getStatut()))
                    .count();
            lblDemandesEnCours.setText(String.valueOf(demandesEnCours));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    // =====================================================
    // GESTION DES REVIEWS
    // =====================================================

    private void ouvrirFormulaireReview(Equipement equipement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/review/user_review.fxml"));
            Parent root = loader.load();

            UserReviewController controller = loader.getController();
            controller.setEquipement(equipement);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une review");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerMesReviews();
            chargerStatistiques();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void ouvrirFormulaireReview(Review review) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/review/user_review.fxml"));
            Parent root = loader.load();

            UserReviewController controller = loader.getController();
            controller.setReview(review);

            Stage stage = new Stage();
            stage.setTitle("Modifier la review");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerMesReviews();
            chargerStatistiques();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void supprimerReview(Review review) {
        if (AlertUtils.showConfirmation("Confirmation", "Supprimer cette review ?")) {
            try {
                reviewService.delete(review.getId());
                chargerMesReviews();
                chargerStatistiques();
                AlertUtils.showInfo("Succès", "Review supprimée");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Impossible de supprimer");
            }
        }
    }

    // =====================================================
    // GESTION DES DEMANDES
    // =====================================================

    private void ouvrirFormulaireDemande(Equipement equipement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/demande/user_demande.fxml"));
            Parent root = loader.load();

            DemandeController controller = loader.getController();
            controller.setEquipement(equipement);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle demande");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerStatistiques();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    // =====================================================
    // 👉 BOUTON "NOUVELLE DEMANDE" - NAVIGUE VERS LA PAGE MES DEMANDES
    // =====================================================

    @FXML
    private void nouvelleDemande() {
        try {
            // Charger la page des demandes dans la même fenêtre (pas une nouvelle fenêtre)
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/demande/user_demande.fxml"));

            // Remplacer le contenu de la fenêtre actuelle
            Stage stage = (Stage) lblBienvenue.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la page des demandes");
        }
    }
}
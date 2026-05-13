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
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import modules.dashboard.services.DashboardService;
import modules.demande.models.Demande;
import modules.demande.services.DemandeService;
import modules.equipement.controllers.ModifEquipementController;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.models.Review;
import modules.review.services.ReviewService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController extends BaseController implements Initializable {

    // =====================================================
    // STATISTIQUES
    // =====================================================

    @FXML private Label lblBienvenue;
    @FXML private Label lblTotalEquipements;
    @FXML private Label lblEquipementsDisponibles;
    @FXML private Label lblEquipementsEnPanne;
    @FXML private Label lblEquipementsMaintenance;
    @FXML private Label lblTotalReviews;
    @FXML private Label lblNoteMoyenne;
    @FXML private Label lblTotalAgriculteurs;
    @FXML private Label lblDemandesEnAttente;

    // =====================================================
    // TABLEAU DES ÉQUIPEMENTS
    // =====================================================

    @FXML private TableView<Equipement> tableEquipements;
    @FXML private TableColumn<Equipement, Integer> colEquipId;
    @FXML private TableColumn<Equipement, String> colEquipNom;
    @FXML private TableColumn<Equipement, String> colEquipType;
    @FXML private TableColumn<Equipement, Float> colEquipPrix;
    @FXML private TableColumn<Equipement, String> colEquipEtat;
    @FXML private TableColumn<Equipement, String> colEquipProprio;
    @FXML private TableColumn<Equipement, Void> colEquipAction;

    // =====================================================
    // TABLEAU DES REVIEWS
    // =====================================================

    @FXML private TableView<Review> tableReviews;
    @FXML private TableColumn<Review, String> colReviewCommentaire;
    @FXML private TableColumn<Review, Float> colReviewNote;
    @FXML private TableColumn<Review, String> colReviewEquipement;
    @FXML private TableColumn<Review, String> colReviewAuteur;
    @FXML private TableColumn<Review, String> colReviewDate;
    @FXML private TableColumn<Review, Void> colReviewAction;

    // =====================================================
    // TABLEAU DES DEMANDES
    // =====================================================

    @FXML private TableView<Demande> tableDemandes;
    @FXML private TableColumn<Demande, String> colDemandeEquipement;
    @FXML private TableColumn<Demande, String> colDemandeAgriculteur;
    @FXML private TableColumn<Demande, String> colDemandeDate;
    @FXML private TableColumn<Demande, String> colDemandeStatut;

    @FXML private PieChart pieChartEquipements;
    @FXML private Button btnAjouterEquipement;

    // =====================================================
    // SERVICES
    // =====================================================

    private DashboardService dashboardService = new DashboardService();
    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();
    private DemandeService demandeService = new DemandeService();

    private ObservableList<Equipement> equipementList = FXCollections.observableArrayList();
    private ObservableList<Review> reviewList = FXCollections.observableArrayList();

    // =====================================================
    // INITIALISATION
    // =====================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        lblBienvenue.setText("👑 Bonjour " + SessionManager.getCurrentUserName());

        configurerTableEquipements();
        configurerTableReviews();
        configurerTableDemandes();

        chargerStatistiques();
        chargerEquipements();
        chargerReviews();
        chargerDemandesRecentes();
        chargerGraphique();
    }

    private void configurerTableEquipements() {
        colEquipId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEquipNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEquipType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colEquipPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEquipEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
        colEquipProprio.setCellValueFactory(cellData -> {
            Equipement e = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("User #" + e.getUserId());
        });

        colEquipPrix.setCellFactory(tc -> new TableCell<Equipement, Float>() {
            @Override
            protected void updateItem(Float prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) setText(null);
                else setText(String.format("%.2f DT", prix));
            }
        });

        colEquipAction.setCellFactory(param -> new TableCell<Equipement, Void>() {
            private final HBox box = new HBox(10);
            private final Button btnModifier = new Button("✏️ Modifier");
            private final Button btnSupprimer = new Button("🗑️ Supprimer");

            {
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");

                btnModifier.setOnAction(event -> {
                    Equipement equipement = getTableView().getItems().get(getIndex());
                    ouvrirPageModification(equipement);
                });

                btnSupprimer.setOnAction(event -> {
                    Equipement equipement = getTableView().getItems().get(getIndex());
                    supprimerEquipement(equipement);
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

    private void configurerTableReviews() {
        colReviewCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colReviewNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colReviewDate.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    r.getDateReview() != null ? r.getDateReview().toString() : ""
            );
        });

        colReviewEquipement.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    r.getEquipement() != null ? r.getEquipement().getNom() : "N/A"
            );
        });

        colReviewAuteur.setCellValueFactory(cellData -> {
            Review r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("User #" + r.getUserId());
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
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");

                btnSupprimer.setOnAction(event -> {
                    Review review = getTableView().getItems().get(getIndex());
                    supprimerReview(review);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnSupprimer);
            }
        });
    }

    private void configurerTableDemandes() {
        colDemandeEquipement.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            String nom = d.getEquipement() != null ? d.getEquipement().getNom() : d.getNomEquipement();
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        colDemandeAgriculteur.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    d.getAgriculteur() != null ? d.getAgriculteur().getPrenom() + " " + d.getAgriculteur().getNom() : "N/A"
            );
        });

        colDemandeDate.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    d.getDateDemande() != null ? d.getDateDemande().toLocalDate().toString() : ""
            );
        });

        colDemandeStatut.setCellValueFactory(new PropertyValueFactory<>("statutLibelle"));

        colDemandeStatut.setCellFactory(tc -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                } else {
                    setText(statut);
                    Demande demande = getTableView().getItems().get(getIndex());
                    setStyle("-fx-text-fill: " + demande.getStatutCouleur() + "; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void chargerStatistiques() {
        try {
            Map<String, Object> stats = dashboardService.getAdminStats();

            lblTotalEquipements.setText(String.valueOf(stats.get("totalEquipements")));
            lblEquipementsDisponibles.setText(String.valueOf(stats.get("equipementsDisponibles")));
            lblEquipementsEnPanne.setText(String.valueOf(stats.get("equipementsEnPanne")));
            lblEquipementsMaintenance.setText(String.valueOf(stats.get("equipementsEnMaintenance")));
            lblTotalReviews.setText(String.valueOf(stats.get("totalReviews")));
            lblNoteMoyenne.setText(stats.get("noteMoyenne") + " ⭐");
            lblTotalAgriculteurs.setText(String.valueOf(stats.get("totalAgriculteurs")));

            long demandesEnAttente = demandeService.getByStatut("EN_ATTENTE").size();
            lblDemandesEnAttente.setText(String.valueOf(demandesEnAttente));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerEquipements() {
        try {
            equipementList.setAll(equipementService.getAll());
            tableEquipements.setItems(equipementList);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les équipements");
        }
    }

    private void chargerReviews() {
        try {
            reviewList.setAll(reviewService.getAll());
            tableReviews.setItems(reviewList);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les reviews");
        }
    }

    private void chargerDemandesRecentes() {
        try {
            List<Demande> demandes = demandeService.getAll();
            ObservableList<Demande> recentes = FXCollections.observableArrayList(
                    demandes.stream().limit(5).toList()
            );
            tableDemandes.setItems(recentes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerGraphique() {
        try {
            int disponibles = equipementService.countDisponibles();
            int enPanne = equipementService.countEnPanne();
            int maintenance = equipementService.countEnMaintenance();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Disponibles (" + disponibles + ")", disponibles),
                    new PieChart.Data("En panne (" + enPanne + ")", enPanne),
                    new PieChart.Data("Maintenance (" + maintenance + ")", maintenance)
            );

            pieChartEquipements.setData(pieData);
            pieChartEquipements.setTitle("État des équipements");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private void ouvrirPageModification(Equipement equipement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/equipement/modif_equipement.fxml"));
            Parent root = loader.load();

            ModifEquipementController controller = loader.getController();
            controller.setEquipement(equipement);

            Stage stage = (Stage) tableEquipements.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la page de modification");
        }
    }


    private void supprimerEquipement(Equipement equipement) {
        if (AlertUtils.showConfirmation("Confirmation",
                "Supprimer l'équipement " + equipement.getNom() + " ?")) {
            try {
                equipementService.delete(equipement.getId());
                chargerEquipements();
                chargerStatistiques();
                chargerGraphique();
                AlertUtils.showInfo("Succès", "Équipement supprimé");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Impossible de supprimer");
            }
        }
    }

    private void supprimerReview(Review review) {
        if (AlertUtils.showConfirmation("Confirmation", "Supprimer cette review ?")) {
            try {
                reviewService.delete(review.getId());
                chargerReviews();
                chargerStatistiques();
                AlertUtils.showInfo("Succès", "Review supprimée");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Impossible de supprimer");
            }
        }
    }

    @FXML
    private void allerVersAjoutEquipement() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/equipement/equipement.fxml"));
            Stage stage = (Stage) btnAjouterEquipement.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la page d'ajout");
        }
    }
}
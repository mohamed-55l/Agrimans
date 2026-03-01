package modules.dashboard.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modules.dashboard.services.DashboardService;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.models.Review;
import modules.review.services.ReviewService;

import java.sql.SQLException;
import java.util.Map;

/**
 * Contrôleur pour le dashboard ADMIN
 *
 * RÔLE: Afficher toutes les statistiques globales de la ferme
 * Accès: Réservé aux administrateurs
 */
public class AdminDashboardController {

    // =====================================================
    // STATISTIQUES (Cartes)
    // =====================================================

    @FXML private Label lblTotalEquipements;
    @FXML private Label lblEquipementsDisponibles;
    @FXML private Label lblEquipementsEnPanne;
    @FXML private Label lblEquipementsMaintenance;
    @FXML private Label lblTotalReviews;
    @FXML private Label lblNoteMoyenne;
    @FXML private Label lblTotalAgriculteurs;
    @FXML private Label lblBienvenue;

    // =====================================================
    // TABLEAUX
    // =====================================================

    @FXML private TableView<Equipement> tableEquipements;
    @FXML private TableColumn<Equipement, Integer> colId;
    @FXML private TableColumn<Equipement, String> colNom;
    @FXML private TableColumn<Equipement, String> colType;
    @FXML private TableColumn<Equipement, Float> colPrix;
    @FXML private TableColumn<Equipement, String> colEtat;
    @FXML private TableColumn<Equipement, Integer> colProprietaireId;

    @FXML private TableView<Review> tableReviews;
    @FXML private TableColumn<Review, String> colCommentaire;
    @FXML private TableColumn<Review, Float> colNote;
    @FXML private TableColumn<Review, String> colEquipement;
    @FXML private TableColumn<Review, String> colDate;
    @FXML private TableColumn<Review, Integer> colAuteurId;

    @FXML private PieChart pieChartEquipements;

    // =====================================================
    // SERVICES
    // =====================================================

    private DashboardService dashboardService = new DashboardService();
    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();

    // =====================================================
    // INITIALISATION
    // =====================================================

    @FXML
    public void initialize() {
        // Vérifier les droits d'accès
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return;
        }

        // Afficher le message de bienvenue
        lblBienvenue.setText("👋 Bonjour " + SessionManager.getCurrentUserName());

        // Configurer les tableaux
        configurerTableEquipements();
        configurerTableReviews();

        // Charger les données
        chargerStatistiques();
        chargerEquipements();
        chargerReviews();
        chargerGraphique();
    }

    /**
     * Configuration du tableau des équipements
     */
    private void configurerTableEquipements() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
        colProprietaireId.setCellValueFactory(new PropertyValueFactory<>("userId"));

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
    }

    /**
     * Configuration du tableau des reviews
     */
    private void configurerTableReviews() {
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReview"));
        colAuteurId.setCellValueFactory(new PropertyValueFactory<>("userId"));

        // Formatage de la note
        colNote.setCellFactory(tc -> new javafx.scene.control.TableCell<Review, Float>() {
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

        // Affichage du nom de l'équipement
        colEquipement.setCellValueFactory(cellData -> {
            Review review = cellData.getValue();
            if (review != null && review.getEquipement() != null) {
                return new javafx.beans.property.SimpleStringProperty(review.getEquipement().getNom());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    /**
     * Charger les statistiques dans les cartes
     */
    private void chargerStatistiques() {
        Map<String, Object> stats = dashboardService.getAdminStats();

        lblTotalEquipements.setText(String.valueOf(stats.get("totalEquipements")));
        lblEquipementsDisponibles.setText(String.valueOf(stats.get("equipementsDisponibles")));
        lblEquipementsEnPanne.setText(String.valueOf(stats.get("equipementsEnPanne")));
        lblEquipementsMaintenance.setText(String.valueOf(stats.get("equipementsEnMaintenance")));
        lblTotalReviews.setText(String.valueOf(stats.get("totalReviews")));
        lblNoteMoyenne.setText(stats.get("noteMoyenne") + " ⭐");
        lblTotalAgriculteurs.setText(String.valueOf(stats.get("totalAgriculteurs")));
    }

    /**
     * Charger tous les équipements
     */
    private void chargerEquipements() {
        try {
            ObservableList<Equipement> data = FXCollections.observableArrayList(
                    equipementService.getAll()
            );
            tableEquipements.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les équipements");
        }
    }

    /**
     * Charger toutes les reviews
     */
    private void chargerReviews() {
        try {
            ObservableList<Review> data = FXCollections.observableArrayList(
                    reviewService.getAll()
            );
            tableReviews.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les reviews");
        }
    }

    /**
     * Charger le graphique circulaire
     */
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
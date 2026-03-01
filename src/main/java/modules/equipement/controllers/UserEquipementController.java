package modules.equipement.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import modules.review.controllers.UserReviewController;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la consultation des équipements (version USER)
 *
 * RÔLE: Permettre à l'agriculteur de voir ses équipements
 * Accès: Réservé aux agriculteurs
 */
public class UserEquipementController implements Initializable {

    // =====================================================
    // COMPOSANTS FXML
    // =====================================================

    @FXML private TableView<Equipement> tableEquipements;

    // =====================================================
    // SERVICES
    // =====================================================

    private EquipementService equipementService = new EquipementService();

    // =====================================================
    // INITIALISATION
    // =====================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier les droits d'accès
        if (!SessionManager.isAgriculteur()) {
            AlertUtils.showError("Accès refusé", "Cette page est réservée aux agriculteurs");
            return;
        }

        // Configurer les colonnes
        configurerColonnes();

        // Charger les équipements de l'utilisateur connecté
        chargerEquipements();
    }

    /**
     * Configure les colonnes du tableau
     */
    private void configurerColonnes() {
        // Récupérer les colonnes existantes (elles sont définies dans le FXML)
        TableColumn<Equipement, String> colNom = (TableColumn<Equipement, String>) tableEquipements.getColumns().get(0);
        TableColumn<Equipement, String> colType = (TableColumn<Equipement, String>) tableEquipements.getColumns().get(1);
        TableColumn<Equipement, Float> colPrix = (TableColumn<Equipement, Float>) tableEquipements.getColumns().get(2);
        TableColumn<Equipement, String> colEtat = (TableColumn<Equipement, String>) tableEquipements.getColumns().get(3);
        TableColumn<Equipement, Void> colAction = (TableColumn<Equipement, Void>) tableEquipements.getColumns().get(4);

        // Lier les colonnes aux propriétés
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        // Formatage du prix
        colPrix.setCellFactory(tc -> new TableCell<Equipement, Float>() {
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

        // Colorisation de l'état
        colEtat.setCellFactory(tc -> new TableCell<Equipement, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(etat);
                    switch (etat) {
                        case "Disponible":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "Non disponible":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "En maintenance":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Colonne action (bouton pour ajouter une review)
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
     * Charge les équipements de l'utilisateur connecté
     */
    private void chargerEquipements() {
        try {
            int userId = SessionManager.getCurrentUserId();
            ObservableList<Equipement> data = FXCollections.observableArrayList(
                    equipementService.getByUserId(userId)
            );
            tableEquipements.setItems(data);

            if (data.isEmpty()) {
                AlertUtils.showInfo("Information", "Vous n'avez aucun équipement assigné pour le moment.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger vos équipements");
        }
    }

    /**
     * Ouvre le formulaire d'ajout de review
     */
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

            // Rafraîchir la liste des reviews si nécessaire

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }
}
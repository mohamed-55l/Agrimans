package modules.demande.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import modules.demande.models.Demande;
import modules.demande.services.DemandeService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDemandeController implements Initializable {

    @FXML private TableView<Demande> tableDemandes;
    @FXML private TableColumn<Demande, String> colId;
    @FXML private TableColumn<Demande, String> colEquipement;
    @FXML private TableColumn<Demande, String> colAgriculteur;
    @FXML private TableColumn<Demande, String> colDescription;
    @FXML private TableColumn<Demande, String> colDate;
    @FXML private TableColumn<Demande, String> colStatut;
    @FXML private TableColumn<Demande, String> colReponse;
    @FXML private TableColumn<Demande, Void> colAction;

    @FXML private ComboBox<String> cbFiltreStatut;
    @FXML private TextArea taReponse;
    @FXML private Label lblDemandeSelectionnee;

    private DemandeService demandeService = new DemandeService();
    private ObservableList<Demande> demandeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isAdmin()) {
            AlertUtils.showError("Accès refusé", "Page réservée aux administrateurs");
            return;
        }

        configurerTableau();
        configurerFiltre();
        chargerDemandes();
    }

    private void configurerTableau() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colEquipement.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            String nom = d.getEquipement() != null ? d.getEquipement().getNom() : d.getNomEquipement();
            return new SimpleStringProperty(nom);
        });

        colAgriculteur.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            return new SimpleStringProperty(
                    d.getAgriculteur() != null ? d.getAgriculteur().getPrenom() + " " + d.getAgriculteur().getNom() : "N/A"
            );
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colDate.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            return new SimpleStringProperty(
                    d.getDateDemande() != null ? d.getDateDemande().toLocalDate().toString() : ""
            );
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutLibelle"));

        colStatut.setCellFactory(tc -> new TableCell<Demande, String>() {
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

        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponseChef"));

        colAction.setCellFactory(param -> new TableCell<Demande, Void>() {
            private final HBox box = new HBox(5);
            private final Button btnAccepter = new Button("✅ Accepter");
            private final Button btnRefuser = new Button("❌ Refuser");

            {
                btnAccepter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");
                btnRefuser.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");

                btnAccepter.setOnAction(event -> {
                    Demande demande = getTableView().getItems().get(getIndex());
                    traiterDemande(demande, "ACCEPTE");
                });

                btnRefuser.setOnAction(event -> {
                    Demande demande = getTableView().getItems().get(getIndex());
                    traiterDemande(demande, "REFUSE");
                });

                box.getChildren().addAll(btnAccepter, btnRefuser);
                box.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Demande demande = getTableView().getItems().get(getIndex());
                    if ("EN_ATTENTE".equals(demande.getStatut())) {
                        setGraphic(box);
                    } else {
                        setGraphic(new Label("✓ Traitée"));
                    }
                }
            }
        });

        // Listener pour sélection
        tableDemandes.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                lblDemandeSelectionnee.setText("Demande #" + selected.getId() + " - " +
                        (selected.getEquipement() != null ? selected.getEquipement().getNom() : selected.getNomEquipement()));
                taReponse.clear();
            }
        });
    }

    private void configurerFiltre() {
        cbFiltreStatut.getItems().addAll("Toutes", "En attente", "Accepté", "Refusé");
        cbFiltreStatut.setValue("Toutes");

        cbFiltreStatut.valueProperty().addListener((obs, old, newValue) -> {
            filtrerDemandes(newValue);
        });
    }

    private void chargerDemandes() {
        try {
            List<Demande> demandes = demandeService.getAll();
            demandeList.setAll(demandes);
            tableDemandes.setItems(demandeList);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les demandes");
        }
    }

    private void filtrerDemandes(String filtre) {
        if ("Toutes".equals(filtre)) {
            tableDemandes.setItems(demandeList);
        } else {
            String statutFiltre = switch(filtre) {
                case "En attente" -> "EN_ATTENTE";
                case "Accepté" -> "ACCEPTE";
                case "Refusé" -> "REFUSE";
                default -> "";
            };

            ObservableList<Demande> filtered = FXCollections.observableArrayList(
                    demandeList.stream()
                            .filter(d -> d.getStatut().equals(statutFiltre))
                            .toList()
            );
            tableDemandes.setItems(filtered);
        }
    }

    private void traiterDemande(Demande demande, String nouveauStatut) {
        String reponse = taReponse.getText().trim();

        if (reponse.isEmpty()) {
            AlertUtils.showWarning("Attention", "Veuillez saisir une réponse pour l'agriculteur");
            return;
        }

        String message = nouveauStatut.equals("ACCEPTE") ?
                "Accepter cette demande ?" :
                "Refuser cette demande ?";

        if (AlertUtils.showConfirmation("Confirmation", message)) {
            try {
                demandeService.updateStatut(demande.getId(), nouveauStatut, reponse);
                chargerDemandes();
                taReponse.clear();
                lblDemandeSelectionnee.setText("Aucune demande sélectionnée");
                AlertUtils.showInfo("Succès", "Demande " +
                        (nouveauStatut.equals("ACCEPTE") ? "acceptée" : "refusée"));
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Impossible de traiter la demande");
            }
        }
    }

    // 👉 Méthodes appelées par les boutons du FXML
    @FXML
    private void traiterAccepte() {
        Demande selected = tableDemandes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            traiterDemande(selected, "ACCEPTE");
        } else {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner une demande");
        }
    }

    @FXML
    private void traiterRefus() {
        Demande selected = tableDemandes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            traiterDemande(selected, "REFUSE");
        } else {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner une demande");
        }
    }
}
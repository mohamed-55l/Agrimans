package modules.demande.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import modules.demande.models.Demande;
import modules.demande.services.DemandeService;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class DemandeController implements Initializable {

    // =====================================================
    // PARTIE 1: FORMULAIRE DE DEMANDE
    // =====================================================

    @FXML private ToggleGroup groupeTypeDemande;
    @FXML private RadioButton radioSelectionner;
    @FXML private RadioButton radioSaisir;

    @FXML private ComboBox<Equipement> cbEquipement;
    @FXML private TextField tfNomEquipement;
    @FXML private TextArea taDescription;
    @FXML private TextField tfQuantite;
    @FXML private Label lblError;

    // =====================================================
    // PARTIE 2: LISTE DES DEMANDES
    // =====================================================

    @FXML private TableView<Demande> tableMesDemandes;
    @FXML private TableColumn<Demande, String> colEquipement;
    @FXML private TableColumn<Demande, String> colDescription;
    @FXML private TableColumn<Demande, String> colDate;
    @FXML private TableColumn<Demande, String> colStatut;
    @FXML private TableColumn<Demande, String> colReponse;
    @FXML private TableColumn<Demande, Void> colAction;

    // =====================================================
    // SERVICES
    // =====================================================

    private DemandeService demandeService = new DemandeService();
    private EquipementService equipementService = new EquipementService();

    private ObservableList<Demande> demandeList = FXCollections.observableArrayList();
    private Demande demandeEnCoursDeModification;
    private Equipement equipementSelectionne;

    // =====================================================
    // INITIALISATION
    // =====================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer le groupe de radios
        groupeTypeDemande = new ToggleGroup();
        radioSelectionner.setToggleGroup(groupeTypeDemande);
        radioSaisir.setToggleGroup(groupeTypeDemande);
        radioSelectionner.setSelected(true);

        // Configurer les listeners
        configurerListeners();

        // Charger les équipements disponibles
        chargerEquipements();

        // Configurer le tableau
        configurerTableauDemandes();

        // Charger les demandes de l'utilisateur
        chargerMesDemandes();
    }

    /**
     * Reçoit un équipement depuis le Dashboard (quand on clique sur "Demander")
     */
    public void setEquipement(Equipement equipement) {
        this.equipementSelectionne = equipement;
        this.demandeEnCoursDeModification = null;

        if (equipement != null) {
            radioSelectionner.setSelected(true);
            cbEquipement.setValue(equipement);
            tfNomEquipement.clear();

            if (taDescription.getText().isEmpty()) {
                taDescription.setText("Demande pour: " + equipement.getNom());
            }
        }
    }

    /**
     * Reçoit une demande à modifier (quand on clique sur ✏️)
     */
    public void setDemande(Demande demande) {
        this.demandeEnCoursDeModification = demande;
        this.equipementSelectionne = null;

        if (demande != null) {
            taDescription.setText(demande.getDescription());
            tfQuantite.setText(String.valueOf(demande.getQuantite()));

            if (demande.getEquipement() != null) {
                radioSelectionner.setSelected(true);
                cbEquipement.setValue(demande.getEquipement());
                tfNomEquipement.clear();
            } else {
                radioSaisir.setSelected(true);
                tfNomEquipement.setText(demande.getNomEquipement());
                cbEquipement.setValue(null);
            }
        }
    }

    private void configurerListeners() {
        // Quand on choisit "Sélectionner"
        radioSelectionner.selectedProperty().addListener((obs, old, newValue) -> {
            cbEquipement.setDisable(!newValue);
            tfNomEquipement.setDisable(newValue);
            if (newValue) tfNomEquipement.clear();
        });

        // Quand on choisit "Saisir"
        radioSaisir.selectedProperty().addListener((obs, old, newValue) -> {
            cbEquipement.setDisable(newValue);
            tfNomEquipement.setDisable(!newValue);
            if (newValue) cbEquipement.setValue(null);
        });
    }

    private void configurerTableauDemandes() {
        colEquipement.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            String nom = d.getEquipement() != null ? d.getEquipement().getNom() : d.getNomEquipement();
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colDate.setCellValueFactory(cellData -> {
            Demande d = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
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

        // Colonne action avec boutons Modifier/Supprimer
        colAction.setCellFactory(param -> new TableCell<Demande, Void>() {
            private final HBox box = new HBox(5);
            private final Button btnModifier = new Button("✏️ Modifier");
            private final Button btnSupprimer = new Button("🗑️ Supprimer");

            {
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 3;");

                btnModifier.setOnAction(event -> {
                    Demande demande = getTableView().getItems().get(getIndex());
                    setDemande(demande);  // Remplir le formulaire pour modification
                });

                btnSupprimer.setOnAction(event -> {
                    Demande demande = getTableView().getItems().get(getIndex());
                    supprimerDemande(demande);
                });

                box.getChildren().addAll(btnModifier, btnSupprimer);
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
                        setGraphic(new Label("✓ Déjà traitée"));
                    }
                }
            }
        });
    }

    private void chargerEquipements() {
        try {
            List<Equipement> equipements = equipementService.getEquipementsDisponibles();
            cbEquipement.setItems(FXCollections.observableArrayList(equipements));

            // Si un équipement a été présélectionné
            if (equipementSelectionne != null) {
                cbEquipement.setValue(equipementSelectionne);
            }

            // Personnaliser l'affichage
            cbEquipement.setCellFactory(param -> new ListCell<Equipement>() {
                @Override
                protected void updateItem(Equipement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " (" + item.getType() + ")");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les équipements");
        }
    }

    private void chargerMesDemandes() {
        try {
            List<Demande> demandes = demandeService.getByAgriculteurId(
                    SessionManager.getCurrentUserId()
            );
            demandeList.setAll(demandes);
            tableMesDemandes.setItems(demandeList);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger vos demandes");
        }
    }

    @FXML
    private void validerDemande() {
        // Validation
        if (radioSelectionner.isSelected() && cbEquipement.getValue() == null) {
            lblError.setText("Veuillez sélectionner un équipement");
            return;
        }

        if (radioSaisir.isSelected() && tfNomEquipement.getText().trim().isEmpty()) {
            lblError.setText("Veuillez saisir le nom de l'équipement");
            return;
        }

        if (taDescription.getText().trim().isEmpty()) {
            lblError.setText("Veuillez décrire votre demande");
            return;
        }

        try {
            if (demandeEnCoursDeModification != null) {
                mettreAJourDemande();
            } else {
                creerNouvelleDemande();
            }

            viderFormulaire();
            chargerMesDemandes();

            AlertUtils.showInfo("Succès", "Demande enregistrée !");

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Erreur: " + e.getMessage());
        } catch (NumberFormatException e) {
            lblError.setText("Quantité invalide");
        }
    }

    private void creerNouvelleDemande() throws SQLException {
        Demande demande = new Demande();
        demande.setAgriculteur(SessionManager.getCurrentUser());
        demande.setDescription(taDescription.getText().trim());
        demande.setQuantite(Integer.parseInt(tfQuantite.getText()));

        if (radioSelectionner.isSelected()) {
            Equipement equip = cbEquipement.getValue();
            demande.setEquipement(equip);
            demande.setNomEquipement(equip.getNom());
            demande.setTypeDemande("EQUIPEMENT_EXISTANT");
        } else {
            demande.setNomEquipement(tfNomEquipement.getText().trim());
            demande.setTypeDemande("EQUIPEMENT_NOUVEAU");
        }

        demandeService.create(demande);
    }

    private void mettreAJourDemande() throws SQLException {
        demandeEnCoursDeModification.setDescription(taDescription.getText().trim());
        demandeEnCoursDeModification.setQuantite(Integer.parseInt(tfQuantite.getText()));

        if (radioSelectionner.isSelected()) {
            Equipement equip = cbEquipement.getValue();
            demandeEnCoursDeModification.setEquipement(equip);
            demandeEnCoursDeModification.setNomEquipement(equip.getNom());
            demandeEnCoursDeModification.setTypeDemande("EQUIPEMENT_EXISTANT");
        } else {
            demandeEnCoursDeModification.setNomEquipement(tfNomEquipement.getText().trim());
            demandeEnCoursDeModification.setTypeDemande("EQUIPEMENT_NOUVEAU");
            demandeEnCoursDeModification.setEquipement(null);
        }

        demandeService.update(demandeEnCoursDeModification);
        demandeEnCoursDeModification = null;
    }

    private void supprimerDemande(Demande demande) {
        if (AlertUtils.showConfirmation("Confirmation", "Supprimer cette demande ?")) {
            try {
                demandeService.delete(demande.getId());
                chargerMesDemandes();
                AlertUtils.showInfo("Succès", "Demande supprimée");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Impossible de supprimer");
            }
        }
    }

    @FXML
    private void annuler() {
        viderFormulaire();
        demandeEnCoursDeModification = null;
        equipementSelectionne = null;
    }

    private void viderFormulaire() {
        radioSelectionner.setSelected(true);
        cbEquipement.setValue(null);
        tfNomEquipement.clear();
        taDescription.clear();
        tfQuantite.setText("1");
        lblError.setText("");
    }
}
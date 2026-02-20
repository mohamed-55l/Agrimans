package modules.equipement.controllers;  // ← NOUVEAU package

import modules.equipement.models.Equipement;           // ← model déplacé
import modules.equipement.services.EquipementService;  // ← service déplacé
//import core.utils.AlertUtils;                           // ← si vous l'avez créé (optionnel)

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class EquipementController {

    @FXML private TextField tfNom;
    @FXML private TextField tfType;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<String> cbEtat;
    @FXML private TableView<Equipement> tableEquipement;
    @FXML private TableColumn<Equipement, Integer> colId;
    @FXML private TableColumn<Equipement, String> colNom;
    @FXML private TableColumn<Equipement, String> colType;
    @FXML private TableColumn<Equipement, Float> colPrix;
    @FXML private TableColumn<Equipement, String> colEtat;

    // Labels pour les messages d'erreur
    @FXML private Label lblNomError;
    @FXML private Label lblTypeError;
    @FXML private Label lblPrixError;
    @FXML private Label lblEtatError;

    private EquipementService service = new EquipementService();
    private ObservableList<Equipement> list = FXCollections.observableArrayList();

    // Patterns de validation
    private static final Pattern NOM_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ0-9\\s\\-]{2,50}$");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ0-9\\s\\-]{2,30}$");
    private static final Pattern PRIX_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        // Formatage du prix
        colPrix.setCellFactory(tc -> new TableCell<Equipement, Float>() {
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

        // Initialisation du ComboBox
        cbEtat.getItems().addAll("Disponible", "Non disponible", "En maintenance");
        cbEtat.setValue("Disponible");

        // Ajout des listeners pour la validation en temps réel
        addValidationListeners();

        loadData();

        // Sélection dans le tableau
        tableEquipement.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) {
                        tfNom.setText(selected.getNom());
                        tfType.setText(selected.getType());
                        tfPrix.setText(String.valueOf(selected.getPrix()));
                        cbEtat.setValue(selected.getDisponibilite());
                        clearErrors();
                    }
                });
    }

    private void addValidationListeners() {
        // Validation du nom
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                showError(lblNomError, "Le nom est requis");
            } else if (!NOM_PATTERN.matcher(newValue).matches()) {
                showError(lblNomError, "2-50 caractères, lettres, chiffres, espaces et tirets");
            } else {
                hideError(lblNomError);
            }
        });

        // Validation du type
        tfType.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                showError(lblTypeError, "Le type est requis");
            } else if (!TYPE_PATTERN.matcher(newValue).matches()) {
                showError(lblTypeError, "2-30 caractères, lettres, chiffres, espaces et tirets");
            } else {
                hideError(lblTypeError);
            }
        });

        // Validation du prix
        tfPrix.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                showError(lblPrixError, "Le prix est requis");
            } else if (!PRIX_PATTERN.matcher(newValue).matches()) {
                showError(lblPrixError, "Format invalide (ex: 100 ou 99.99)");
            } else {
                float prix = Float.parseFloat(newValue);
                if (prix <= 0) {
                    showError(lblPrixError, "Le prix doit être positif");
                } else if (prix > 1000000) {
                    showError(lblPrixError, "Prix trop élevé (max 1,000,000)");
                } else {
                    hideError(lblPrixError);
                }
            }
        });

        // Validation de l'état
        cbEtat.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                showError(lblEtatError, "Sélectionnez un état");
            } else {
                hideError(lblEtatError);
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validation Nom
        if (tfNom.getText().isEmpty()) {
            showError(lblNomError, "Le nom est requis");
            isValid = false;
        } else if (!NOM_PATTERN.matcher(tfNom.getText()).matches()) {
            showError(lblNomError, "Format de nom invalide");
            isValid = false;
        }

        // Validation Type
        if (tfType.getText().isEmpty()) {
            showError(lblTypeError, "Le type est requis");
            isValid = false;
        } else if (!TYPE_PATTERN.matcher(tfType.getText()).matches()) {
            showError(lblTypeError, "Format de type invalide");
            isValid = false;
        }

        // Validation Prix
        if (tfPrix.getText().isEmpty()) {
            showError(lblPrixError, "Le prix est requis");
            isValid = false;
        } else {
            try {
                float prix = Float.parseFloat(tfPrix.getText());
                if (prix <= 0) {
                    showError(lblPrixError, "Le prix doit être positif");
                    isValid = false;
                } else if (prix > 1000000) {
                    showError(lblPrixError, "Prix trop élevé");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showError(lblPrixError, "Format de prix invalide");
                isValid = false;
            }
        }

        // Validation État
        if (cbEtat.getValue() == null || cbEtat.getValue().isEmpty()) {
            showError(lblEtatError, "Sélectionnez un état");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        label.setVisible(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
    }

    private void clearErrors() {
        hideError(lblNomError);
        hideError(lblTypeError);
        hideError(lblPrixError);
        hideError(lblEtatError);
    }

    void loadData() {
        try {
            list.setAll(service.getAll());
            tableEquipement.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données");
        }
    }

    @FXML
    void ajouterEquipement() {
        if (!validateFields()) {
            showAlert("Validation", "Veuillez corriger les erreurs");
            return;
        }

        try {
            Equipement e = new Equipement();
            e.setNom(tfNom.getText().trim());
            e.setType(tfType.getText().trim());
            e.setPrix(Float.parseFloat(tfPrix.getText()));
            e.setDisponibilite(cbEtat.getValue());

            service.create(e);
            loadData();
            clearFields();
            showAlert("Succès", "Équipement ajouté avec succès");

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                showAlert("Erreur", "Cet équipement existe déjà");
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    @FXML
    void modifierEquipement() {
        Equipement selected = tableEquipement.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un équipement");
            return;
        }

        if (!validateFields()) {
            showAlert("Validation", "Veuillez corriger les erreurs");
            return;
        }

        try {
            selected.setNom(tfNom.getText().trim());
            selected.setType(tfType.getText().trim());
            selected.setPrix(Float.parseFloat(tfPrix.getText()));
            selected.setDisponibilite(cbEtat.getValue());

            service.update(selected);
            loadData();
            clearFields();
            showAlert("Succès", "Équipement modifié avec succès");

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification");
        }
    }

    @FXML
    void supprimerEquipement() {
        Equipement selected = tableEquipement.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un équipement");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'équipement");
        confirm.setContentText("Voulez-vous vraiment supprimer " + selected.getNom() + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.delete(selected.getId());
                loadData();
                clearFields();
                showAlert("Succès", "Équipement supprimé");
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key")) {
                    showAlert("Erreur", "Impossible de supprimer : l'équipement a des reviews associées");
                } else {
                    showAlert("Erreur", "Erreur lors de la suppression");
                }
            }
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
        String buttonText = button.getText();
        if (buttonText.contains("Supprimer")) {
            button.setStyle("-fx-background-color: #2E3D27; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        } else {
            button.setStyle("-fx-background-color: #4B8B3B; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        }
    }

    void clearFields() {
        tfNom.clear();
        tfType.clear();
        tfPrix.clear();
        cbEtat.setValue("Disponible");
        clearErrors();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package modules.review.controllers;  // ← CHANGEMENT 1

import modules.review.models.Review;              // ← CHANGEMENT 2
import modules.equipement.models.Equipement;       // ← CHANGEMENT 3
import modules.review.services.ReviewService;      // ← CHANGEMENT 4
import modules.equipement.services.EquipementService;  // ← CHANGEMENT 5

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReviewController {

    @FXML private TableView<Review> tableReview;
    @FXML private TableColumn<Review, String> colCommentaire;
    @FXML private TableColumn<Review, Float> colNote;
    @FXML private TableColumn<Review, String> colEquipement;
    @FXML private TableColumn<Review, Date> colDate;

    @FXML private TextField tfCommentaire;
    @FXML private TextField tfNote;
    @FXML private ComboBox<Equipement> cbEquipement;

    // Labels pour les messages d'erreur
    @FXML private Label lblCommentaireError;
    @FXML private Label lblNoteError;
    @FXML private Label lblEquipementError;

    private ReviewService reviewService = new ReviewService();
    private EquipementService equipementService = new EquipementService();

    private ObservableList<Review> reviewList = FXCollections.observableArrayList();

    // Patterns de validation
    private static final String COMMENTAIRE_PATTERN = "^[a-zA-ZÀ-ÿ0-9\\s\\-\\.,!?]{5,500}$";
    private static final String NOTE_PATTERN = "^[0-5](\\.\\d)?$";

    @FXML
    public void initialize() {
        try {
            // Configuration des colonnes
            colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
            colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateReview"));

            // Formatage de la note
            colNote.setCellFactory(tc -> new TableCell<Review, Float>() {
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

            // Pour afficher le nom de l'équipement
            colEquipement.setCellValueFactory(cellData -> {
                Review review = cellData.getValue();
                if (review != null && review.getEquipement() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            review.getEquipement().getNom()
                    );
                } else {
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                }
            });

            // Charger les équipements
            loadEquipements();

            // Ajout des listeners pour validation en temps réel
            addValidationListeners();

            // Charger les reviews
            loadReviews();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void loadEquipements() throws SQLException {
        ObservableList<Equipement> equipements =
                FXCollections.observableArrayList(equipementService.getAll());
        cbEquipement.setItems(equipements);

        // Personnalisation de l'affichage
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

        cbEquipement.setButtonCell(new ListCell<Equipement>() {
            @Override
            protected void updateItem(Equipement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });
    }

    private void addValidationListeners() {
        // Validation du commentaire
        tfCommentaire.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                showError(lblCommentaireError, "Le commentaire est requis");
            } else if (newValue.length() < 5) {
                showError(lblCommentaireError, "Minimum 5 caractères");
            } else if (newValue.length() > 500) {
                showError(lblCommentaireError, "Maximum 500 caractères");
            } else if (!newValue.matches(COMMENTAIRE_PATTERN)) {
                showError(lblCommentaireError, "Caractères non autorisés");
            } else {
                hideError(lblCommentaireError);
            }
        });

        // Validation de la note
        tfNote.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                showError(lblNoteError, "La note est requise");
            } else if (!newValue.matches(NOTE_PATTERN)) {
                showError(lblNoteError, "Note entre 0 et 5 (ex: 4.5)");
            } else {
                try {
                    float note = Float.parseFloat(newValue);
                    if (note < 0 || note > 5) {
                        showError(lblNoteError, "La note doit être entre 0 et 5");
                    } else {
                        hideError(lblNoteError);
                    }
                } catch (NumberFormatException e) {
                    showError(lblNoteError, "Format invalide");
                }
            }
        });

        // Validation de l'équipement
        cbEquipement.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue == null) {
                showError(lblEquipementError, "Sélectionnez un équipement");
            } else {
                hideError(lblEquipementError);
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validation Commentaire
        if (tfCommentaire.getText().isEmpty()) {
            showError(lblCommentaireError, "Le commentaire est requis");
            isValid = false;
        } else if (tfCommentaire.getText().length() < 5) {
            showError(lblCommentaireError, "Minimum 5 caractères");
            isValid = false;
        } else if (tfCommentaire.getText().length() > 500) {
            showError(lblCommentaireError, "Maximum 500 caractères");
            isValid = false;
        }

        // Validation Note
        if (tfNote.getText().isEmpty()) {
            showError(lblNoteError, "La note est requise");
            isValid = false;
        } else {
            try {
                float note = Float.parseFloat(tfNote.getText());
                if (note < 0 || note > 5) {
                    showError(lblNoteError, "La note doit être entre 0 et 5");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showError(lblNoteError, "Format de note invalide");
                isValid = false;
            }
        }

        // Validation Équipement
        if (cbEquipement.getValue() == null) {
            showError(lblEquipementError, "Sélectionnez un équipement");
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

    @FXML
    void ajouterReview() {
        if (!validateFields()) {
            showAlert("Validation", "Veuillez corriger les erreurs");
            return;
        }

        try {
            Review review = new Review();
            review.setCommentaire(tfCommentaire.getText().trim());
            review.setNote(Float.parseFloat(tfNote.getText()));
            review.setDateReview(Date.valueOf(LocalDate.now()));
            review.setEquipementId(cbEquipement.getValue().getId());
            review.setEquipement(cbEquipement.getValue());

            reviewService.create(review);

            loadReviews();
            clearFields();

            showAlert("Succès", "Review ajoutée avec succès");

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                showAlert("Erreur", "Cette review existe déjà");
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    private void loadReviews() throws SQLException {
        reviewList.clear();
        reviewList.addAll(reviewService.getAll());
        tableReview.setItems(reviewList);
    }

    private void clearFields() {
        tfCommentaire.clear();
        tfNote.clear();
        cbEquipement.setValue(null);
        hideError(lblCommentaireError);
        hideError(lblNoteError);
        hideError(lblEquipementError);
    }

    @FXML
    void handleButtonEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #7DBF6C; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 30; -fx-cursor: hand;");
    }

    @FXML
    void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #4B8B3B; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 30;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
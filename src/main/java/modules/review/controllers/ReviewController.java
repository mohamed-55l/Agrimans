package modules.review.controllers;

import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.models.Review;
import modules.review.services.ReviewService;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur pour la gestion des reviews
 *
 * RÔLE: Interface utilisateur pour le CRUD des reviews
 */
public class ReviewController {

    // =====================================================
    // COMPOSANTS FXML
    // =====================================================

    // Tableau
    @FXML private TableView<Review> tableReview;
    @FXML private TableColumn<Review, String> colCommentaire;
    @FXML private TableColumn<Review, Float> colNote;
    @FXML private TableColumn<Review, String> colEquipement;
    @FXML private TableColumn<Review, Date> colDate;

    // Champs de formulaire
    @FXML private TextField tfCommentaire;
    @FXML private TextField tfNote;
    @FXML private ComboBox<Equipement> cbEquipement;

    // Labels d'erreur
    @FXML private Label lblCommentaireError;
    @FXML private Label lblNoteError;
    @FXML private Label lblEquipementError;

    // =====================================================
    // SERVICES
    // =====================================================

    private ReviewService reviewService = new ReviewService();
    private EquipementService equipementService = new EquipementService();

    // =====================================================
    // DONNÉES
    // =====================================================

    private ObservableList<Review> reviewList = FXCollections.observableArrayList();
    private Equipement equipementSelectionne;  // Pour présélection (depuis UserDashboard)

    // =====================================================
    // PATTERNS DE VALIDATION
    // =====================================================

    private static final String COMMENTAIRE_PATTERN = "^[a-zA-ZÀ-ÿ0-9\\s\\-\\.,!?]{5,500}$";
    private static final String NOTE_PATTERN = "^[0-5](\\.\\d)?$";

    // =====================================================
    // INITIALISATION
    // =====================================================

    @FXML
    public void initialize() {
        try {
            // 1. Configuration des colonnes
            configurerColonnes();

            // 2. Formatage spécial pour la colonne note
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

            // 3. Configuration de la colonne équipement (affichage du nom)
            colEquipement.setCellValueFactory(cellData -> {
                Review review = cellData.getValue();
                if (review != null && review.getEquipement() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            review.getEquipement().getNom()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("N/A");
            });

            // 4. Charger les équipements dans la ComboBox
            chargerEquipements();

            // 5. Si un équipement a été présélectionné (depuis UserDashboard)
            if (equipementSelectionne != null) {
                cbEquipement.setValue(equipementSelectionne);
                // Optionnel: désactiver la ComboBox pour forcer la review sur cet équipement
                // cbEquipement.setDisable(true);
            }

            // 6. Ajouter les listeners de validation
            ajouterListenersValidation();

            // 7. Charger les reviews
            chargerReviews();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    /**
     * Configure les colonnes du tableau
     */
    private void configurerColonnes() {
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReview"));
        // colEquipement est configuré différemment (via CellValueFactory personnalisé)
    }

    /**
     * Charge les équipements dans la ComboBox
     */
    private void chargerEquipements() throws SQLException {
        // Récupérer tous les équipements
        ObservableList<Equipement> equipements =
                FXCollections.observableArrayList(equipementService.getAll());

        cbEquipement.setItems(equipements);

        // Personnalisation de l'affichage dans la liste déroulante
        cbEquipement.setCellFactory(param -> new ListCell<Equipement>() {
            @Override
            protected void updateItem(Equipement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " (" + item.getType() + ") - " + item.getDisponibilite());
                }
            }
        });

        // Personnalisation de l'affichage quand un item est sélectionné
        cbEquipement.setButtonCell(new ListCell<Equipement>() {
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
    }

    /**
     * Charge les reviews depuis la base de données
     */
    private void chargerReviews() throws SQLException {
        reviewList.clear();
        reviewList.addAll(reviewService.getAll());
        tableReview.setItems(reviewList);
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    /**
     * Ajoute des listeners pour la validation en temps réel
     */
    private void ajouterListenersValidation() {
        // Validation du commentaire
        tfCommentaire.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                afficherErreur(lblCommentaireError, "Le commentaire est requis");
            } else if (newValue.length() < 5) {
                afficherErreur(lblCommentaireError, "Minimum 5 caractères");
            } else if (newValue.length() > 500) {
                afficherErreur(lblCommentaireError, "Maximum 500 caractères");
            } else if (!newValue.matches(COMMENTAIRE_PATTERN)) {
                afficherErreur(lblCommentaireError, "Caractères non autorisés");
            } else {
                cacherErreur(lblCommentaireError);
            }
        });

        // Validation de la note
        tfNote.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                afficherErreur(lblNoteError, "La note est requise");
            } else if (!newValue.matches(NOTE_PATTERN)) {
                afficherErreur(lblNoteError, "Note entre 0 et 5 (ex: 4.5)");
            } else {
                try {
                    float note = Float.parseFloat(newValue);
                    if (note < 0 || note > 5) {
                        afficherErreur(lblNoteError, "La note doit être entre 0 et 5");
                    } else {
                        cacherErreur(lblNoteError);
                    }
                } catch (NumberFormatException e) {
                    afficherErreur(lblNoteError, "Format invalide");
                }
            }
        });

        // Validation de l'équipement
        cbEquipement.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue == null) {
                afficherErreur(lblEquipementError, "Sélectionnez un équipement");
            } else {
                cacherErreur(lblEquipementError);
            }
        });
    }

    /**
     * Valide tous les champs avant soumission
     */
    private boolean validerChamps() {
        boolean isValid = true;

        // Validation Commentaire
        if (tfCommentaire.getText().isEmpty()) {
            afficherErreur(lblCommentaireError, "Le commentaire est requis");
            isValid = false;
        } else if (tfCommentaire.getText().length() < 5) {
            afficherErreur(lblCommentaireError, "Minimum 5 caractères");
            isValid = false;
        } else if (tfCommentaire.getText().length() > 500) {
            afficherErreur(lblCommentaireError, "Maximum 500 caractères");
            isValid = false;
        }

        // Validation Note
        if (tfNote.getText().isEmpty()) {
            afficherErreur(lblNoteError, "La note est requise");
            isValid = false;
        } else {
            try {
                float note = Float.parseFloat(tfNote.getText());
                if (note < 0 || note > 5) {
                    afficherErreur(lblNoteError, "La note doit être entre 0 et 5");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                afficherErreur(lblNoteError, "Format de note invalide");
                isValid = false;
            }
        }

        // Validation Équipement
        if (cbEquipement.getValue() == null) {
            afficherErreur(lblEquipementError, "Sélectionnez un équipement");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Affiche une erreur
     */
    private void afficherErreur(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        label.setVisible(true);
    }

    /**
     * Cache une erreur
     */
    private void cacherErreur(Label label) {
        label.setVisible(false);
    }

    /**
     * Vide les champs du formulaire
     */
    private void viderFormulaire() {
        tfCommentaire.clear();
        tfNote.clear();
        cbEquipement.setValue(null);
        cacherErreur(lblCommentaireError);
        cacherErreur(lblNoteError);
        cacherErreur(lblEquipementError);

        // Désélectionner dans le tableau
        tableReview.getSelectionModel().clearSelection();
    }

    // =====================================================
    // ACTIONS SUR LES BOUTONS
    // =====================================================

    /**
     * Ajouter une review
     */
    @FXML
    void ajouterReview() {
        // 1. Valider les champs
        if (!validerChamps()) {
            AlertUtils.showWarning("Validation", "Veuillez corriger les erreurs");
            return;
        }

        try {
            // 2. Créer l'objet Review
            Review review = new Review();
            review.setCommentaire(tfCommentaire.getText().trim());
            review.setNote(Float.parseFloat(tfNote.getText()));
            review.setDateReview(Date.valueOf(LocalDate.now())); // Date du jour
            review.setEquipementId(cbEquipement.getValue().getId());

            // ⚠️ VALEUR TEMPORAIRE - À remplacer plus tard par l'ID du vrai utilisateur connecté
            review.setUserId(1);  // Pour l'instant, on met 1 (utilisateur par défaut)

            // 3. Sauvegarder
            reviewService.create(review);

            // 4. Recharger la liste
            chargerReviews();

            // 5. Vider le formulaire
            viderFormulaire();

            // 6. Message de succès
            AlertUtils.showInfo("Succès", "Review ajoutée avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    /**
     * Modifier une review (sélectionnée)
     */
    @FXML
    void modifierReview() {
        // 1. Vérifier la sélection
        Review selected = tableReview.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner une review");
            return;
        }

        // 2. Remplir le formulaire avec les données sélectionnées
        tfCommentaire.setText(selected.getCommentaire());
        tfNote.setText(String.valueOf(selected.getNote()));

        // Trouver l'équipement correspondant dans la ComboBox
        for (Equipement e : cbEquipement.getItems()) {
            if (e.getId() == selected.getEquipementId()) {
                cbEquipement.setValue(e);
                break;
            }
        }

        // 3. Changer le texte du bouton (optionnel - vous pouvez ajouter un bouton "Mettre à jour")
        // Pour l'instant, on va simplement montrer un message
        AlertUtils.showInfo("Info", "Vous pouvez maintenant modifier la review et cliquer sur 'Ajouter' pour mettre à jour.");

        // Note: Pour une vraie modification, il faudrait un bouton "Mettre à jour" séparé
        // et une méthode `updateReview()` qui appelle reviewService.update()
    }

    /**
     * Supprimer une review
     */
    @FXML
    void supprimerReview() {
        // 1. Vérifier la sélection
        Review selected = tableReview.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner une review");
            return;
        }

        // 2. Demander confirmation
        boolean confirm = AlertUtils.showConfirmation(
                "Confirmation",
                "Voulez-vous vraiment supprimer cette review ?"
        );

        if (confirm) {
            try {
                // 3. Supprimer
                reviewService.delete(selected.getId());

                // 4. Recharger la liste
                chargerReviews();

                // 5. Vider le formulaire
                viderFormulaire();

                // 6. Message de succès
                AlertUtils.showInfo("Succès", "Review supprimée");

            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Erreur lors de la suppression");
            }
        }
    }

    // =====================================================
    // MÉTHODES DE NAVIGATION/INTÉGRATION
    // =====================================================

    /**
     * Permet de présélectionner un équipement (appelé depuis UserDashboard)
     * @param equipement L'équipement à présélectionner
     */
    public void setEquipement(Equipement equipement) {
        this.equipementSelectionne = equipement;

        // Si le ComboBox est déjà initialisé, on sélectionne l'équipement
        if (cbEquipement != null && equipement != null) {
            cbEquipement.setValue(equipement);
            System.out.println("✅ Équipement présélectionné: " + equipement.getNom());
        }
    }

    /**
     * Effet de survol des boutons
     */
    @FXML
    void handleButtonEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #7DBF6C; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; " +
                "-fx-padding: 10 20; -fx-cursor: hand;");
    }

    /**
     * Effet de sortie des boutons
     */
    @FXML
    void handleButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        String buttonText = button.getText();

        if (buttonText.contains("Supprimer")) {
            button.setStyle("-fx-background-color: #2E3D27; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; " +
                    "-fx-padding: 10 20;");
        } else {
            button.setStyle("-fx-background-color: #4B8B3B; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; " +
                    "-fx-padding: 10 20;");
        }
    }
}
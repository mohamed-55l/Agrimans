package modules.review.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.models.Review;
import modules.review.services.ReviewService;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'ajout de reviews (version USER)
 *
 * RÔLE: Permettre à l'agriculteur d'ajouter une review sur un équipement
 * Accès: Réservé aux agriculteurs
 */
public class UserReviewController implements Initializable {

    // =====================================================
    // COMPOSANTS FXML
    // =====================================================

    @FXML private Label lblTitre;
    @FXML private Label lblEquipementInfo;
    @FXML private TextArea taCommentaire;
    @FXML private TextField tfNote;
    @FXML private Label lblErrorCommentaire;
    @FXML private Label lblErrorNote;
    @FXML private ComboBox<Equipement> cbEquipement;

    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;

    // =====================================================
    // SERVICES
    // =====================================================

    private ReviewService reviewService = new ReviewService();
    private EquipementService equipementService = new EquipementService();

    // =====================================================
    // DONNÉES
    // =====================================================

    private Equipement equipementSelectionne;
    private static final String NOTE_PATTERN = "^[0-5](\\.\\d)?$";

    // =====================================================
    // INITIALISATION
    // =====================================================
    private Review reviewEnCoursDeModification;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration des listeners de validation
        configurerValidation();
    }

    /**
     * Configure les listeners pour la validation en temps réel
     */
    private void configurerValidation() {
        // Validation de la note
        tfNote.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                afficherErreur(lblErrorNote, "La note est requise");
            } else if (!newValue.matches(NOTE_PATTERN)) {
                afficherErreur(lblErrorNote, "Note entre 0 et 5 (ex: 4.5)");
            } else {
                try {
                    float note = Float.parseFloat(newValue);
                    if (note < 0 || note > 5) {
                        afficherErreur(lblErrorNote, "La note doit être entre 0 et 5");
                    } else {
                        cacherErreur(lblErrorNote);
                    }
                } catch (NumberFormatException e) {
                    afficherErreur(lblErrorNote, "Format invalide");
                }
            }
        });

        // Validation du commentaire
        taCommentaire.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                afficherErreur(lblErrorCommentaire, "Le commentaire est requis");
            } else if (newValue.length() < 5) {
                afficherErreur(lblErrorCommentaire, "Minimum 5 caractères");
            } else if (newValue.length() > 500) {
                afficherErreur(lblErrorCommentaire, "Maximum 500 caractères");
            } else {
                cacherErreur(lblErrorCommentaire);
            }
        });
    }

    /**
     * Définit l'équipement pour lequel on ajoute une review
     * Cette méthode est appelée depuis UserDashboardController
     *
     * @param equipement L'équipement sélectionné
     */
    public void setEquipement(Equipement equipement) {
        this.equipementSelectionne = equipement;

        if (equipement != null) {
            lblTitre.setText("Ajouter une review pour : " + equipement.getNom());
            lblEquipementInfo.setText("Équipement: " + equipement.getNom() +
                    " (" + equipement.getType() + ") - " +
                    String.format("%.2f DT", equipement.getPrix()));
        }
    }

    // =====================================================
    // ACTIONS
    // =====================================================

    /**
     * Valide et sauvegarde la review
     */
    @FXML
    private void validerReview() {
        if (!validerChamps()) return;

        try {
            if (reviewEnCoursDeModification != null) {
                // C'est une modification
                reviewEnCoursDeModification.setCommentaire(taCommentaire.getText().trim());
                reviewEnCoursDeModification.setNote(Float.parseFloat(tfNote.getText()));
                reviewService.update(reviewEnCoursDeModification);
                AlertUtils.showInfo("Succès", "Review modifiée avec succès !");
            } else {
                // C'est une création
                Review review = new Review();
                review.setCommentaire(taCommentaire.getText().trim());
                review.setNote(Float.parseFloat(tfNote.getText()));
                review.setDateReview(Date.valueOf(LocalDate.now()));
                review.setEquipementId(equipementSelectionne.getId());
                review.setUserId(SessionManager.getCurrentUserId());
                reviewService.create(review);
                AlertUtils.showInfo("Succès", "Review ajoutée avec succès !");
            }

            fermerFenetre();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    /**
     * Annule et ferme la fenêtre
     */
    @FXML
    private void annuler() {
        if (AlertUtils.showConfirmation("Confirmation", "Voulez-vous vraiment annuler ?")) {
            fermerFenetre();
        }
    }

    /**
     * Ferme la fenêtre actuelle
     */
    private void fermerFenetre() {
        btnAnnuler.getScene().getWindow().hide();
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    /**
     * Valide tous les champs avant soumission
     */
    private boolean validerChamps() {
        boolean isValid = true;

        // Validation commentaire
        if (taCommentaire.getText().isEmpty()) {
            afficherErreur(lblErrorCommentaire, "Le commentaire est requis");
            isValid = false;
        } else if (taCommentaire.getText().length() < 5) {
            afficherErreur(lblErrorCommentaire, "Minimum 5 caractères");
            isValid = false;
        } else if (taCommentaire.getText().length() > 500) {
            afficherErreur(lblErrorCommentaire, "Maximum 500 caractères");
            isValid = false;
        }

        // Validation note
        if (tfNote.getText().isEmpty()) {
            afficherErreur(lblErrorNote, "La note est requise");
            isValid = false;
        } else {
            try {
                float note = Float.parseFloat(tfNote.getText());
                if (note < 0 || note > 5) {
                    afficherErreur(lblErrorNote, "La note doit être entre 0 et 5");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                afficherErreur(lblErrorNote, "Format de note invalide");
                isValid = false;
            }
        }

        return isValid;
    }

    private void afficherErreur(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        label.setVisible(true);
    }

    private void cacherErreur(Label label) {
        label.setVisible(false);
    }

    // Ajoutez cette méthode pour modifier une review existante
    public void setReview(Review review) {
        this.reviewEnCoursDeModification = review;
        this.equipementSelectionne = review.getEquipement();

        // Utiliser Platform.runLater pour attendre que les composants soient initialisés
        javafx.application.Platform.runLater(() -> {
            if (review != null && review.getEquipement() != null) {
                // Remplir le formulaire
                tfNote.setText(String.valueOf(review.getNote()));
                taCommentaire.setText(review.getCommentaire());

                lblTitre.setText("Modifier la review pour : " + review.getEquipement().getNom());
                lblEquipementInfo.setText("Équipement: " + review.getEquipement().getNom() +
                        " (" + review.getEquipement().getType() + ")");

                // Vérifier que cbEquipement n'est pas null avant de l'utiliser
                if (cbEquipement != null) {
                    cbEquipement.setValue(review.getEquipement());
                    cbEquipement.setDisable(true);
                }

                btnValider.setText("✅ Modifier");
            }
        });
    }
}
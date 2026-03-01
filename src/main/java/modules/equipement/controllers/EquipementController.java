package modules.equipement.controllers;

import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.review.models.Review;
import modules.review.services.ReviewService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Contrôleur pour la gestion des équipements
 *
 * RÔLE: Interface utilisateur pour le CRUD des équipements
 * Pattern MVC: Contrôleur
 */
public class EquipementController {

    // =====================================================
    // COMPOSANTS FXML (liés par le fichier .fxml)
    // =====================================================

    // Champs de formulaire
    @FXML private TextField tfNom;
    @FXML private TextField tfType;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<String> cbEtat;

    // Tableau
    @FXML private TableView<Equipement> tableEquipement;
    @FXML private TableColumn<Equipement, Integer> colId;
    @FXML private TableColumn<Equipement, String> colNom;
    @FXML private TableColumn<Equipement, String> colType;
    @FXML private TableColumn<Equipement, Float> colPrix;
    @FXML private TableColumn<Equipement, String> colEtat;

    // Labels d'erreur (validation)
    @FXML private Label lblNomError;
    @FXML private Label lblTypeError;
    @FXML private Label lblPrixError;
    @FXML private Label lblEtatError;

    // =====================================================
    // SERVICES
    // =====================================================

    private EquipementService service = new EquipementService();
    private ReviewService reviewService = new ReviewService(); // Pour vérifier les reviews avant suppression

    // =====================================================
    // DONNÉES
    // =====================================================

    private ObservableList<Equipement> list = FXCollections.observableArrayList();

    // =====================================================
    // PATTERNS DE VALIDATION (Regex)
    // =====================================================

    private static final Pattern NOM_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ0-9\\s\\-]{2,50}$");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ0-9\\s\\-]{2,30}$");
    private static final Pattern PRIX_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    // =====================================================
    // INITIALISATION
    // =====================================================

    @FXML
    public void initialize() {
        // 1. Configuration des colonnes du tableau
        configurerColonnes();

        // 2. Formatage spécial pour la colonne prix
        colPrix.setCellFactory(tc -> new TableCell<Equipement, Float>() {
            @Override
            protected void updateItem(Float price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", price)); // Affiche avec devise
                }
            }
        });

        // 3. Initialisation de la ComboBox des états
        cbEtat.getItems().addAll("Disponible", "Non disponible", "En maintenance");
        cbEtat.setValue("Disponible"); // Valeur par défaut

        // 4. Ajout des listeners pour validation en temps réel
        ajouterListenersValidation();

        // 5. Chargement des données
        chargerDonnees();

        // 6. Listener pour la sélection dans le tableau
        tableEquipement.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) {
                        // Remplir le formulaire avec l'équipement sélectionné
                        tfNom.setText(selected.getNom());
                        tfType.setText(selected.getType());
                        tfPrix.setText(String.valueOf(selected.getPrix()));
                        cbEtat.setValue(selected.getDisponibilite());
                        effacerErreurs();
                    }
                });
    }

    /**
     * Configure les colonnes du tableau
     */
    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
    }

    // =====================================================
    // VALIDATION EN TEMPS RÉEL
    // =====================================================

    /**
     * Ajoute des listeners pour valider les champs en temps réel
     */
    private void ajouterListenersValidation() {
        // Validation du nom
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                afficherErreur(lblNomError, "Le nom est requis");
            } else if (!NOM_PATTERN.matcher(newValue).matches()) {
                afficherErreur(lblNomError, "2-50 caractères: lettres, chiffres, espaces, tirets");
            } else {
                cacherErreur(lblNomError);
            }
        });

        // Validation du type
        tfType.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                afficherErreur(lblTypeError, "Le type est requis");
            } else if (!TYPE_PATTERN.matcher(newValue).matches()) {
                afficherErreur(lblTypeError, "2-30 caractères: lettres, chiffres, espaces, tirets");
            } else {
                cacherErreur(lblTypeError);
            }
        });

        // Validation du prix
        tfPrix.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.isEmpty()) {
                afficherErreur(lblPrixError, "Le prix est requis");
            } else if (!PRIX_PATTERN.matcher(newValue).matches()) {
                afficherErreur(lblPrixError, "Format invalide (ex: 100 ou 99.99)");
            } else {
                try {
                    float prix = Float.parseFloat(newValue);
                    if (prix <= 0) {
                        afficherErreur(lblPrixError, "Le prix doit être positif");
                    } else if (prix > 1000000) {
                        afficherErreur(lblPrixError, "Prix trop élevé (max 1,000,000)");
                    } else {
                        cacherErreur(lblPrixError);
                    }
                } catch (NumberFormatException e) {
                    afficherErreur(lblPrixError, "Format invalide");
                }
            }
        });

        // Validation de l'état
        cbEtat.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                afficherErreur(lblEtatError, "Sélectionnez un état");
            } else {
                cacherErreur(lblEtatError);
            }
        });
    }

    /**
     * Valide tous les champs avant soumission
     * @return true si tous les champs sont valides
     */
    private boolean validerChamps() {
        boolean isValid = true;

        // Validation Nom
        if (tfNom.getText().isEmpty()) {
            afficherErreur(lblNomError, "Le nom est requis");
            isValid = false;
        } else if (!NOM_PATTERN.matcher(tfNom.getText()).matches()) {
            afficherErreur(lblNomError, "Format de nom invalide");
            isValid = false;
        }

        // Validation Type
        if (tfType.getText().isEmpty()) {
            afficherErreur(lblTypeError, "Le type est requis");
            isValid = false;
        } else if (!TYPE_PATTERN.matcher(tfType.getText()).matches()) {
            afficherErreur(lblTypeError, "Format de type invalide");
            isValid = false;
        }

        // Validation Prix
        if (tfPrix.getText().isEmpty()) {
            afficherErreur(lblPrixError, "Le prix est requis");
            isValid = false;
        } else {
            try {
                float prix = Float.parseFloat(tfPrix.getText());
                if (prix <= 0) {
                    afficherErreur(lblPrixError, "Le prix doit être positif");
                    isValid = false;
                } else if (prix > 1000000) {
                    afficherErreur(lblPrixError, "Prix trop élevé");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                afficherErreur(lblPrixError, "Format de prix invalide");
                isValid = false;
            }
        }

        // Validation État
        if (cbEtat.getValue() == null || cbEtat.getValue().isEmpty()) {
            afficherErreur(lblEtatError, "Sélectionnez un état");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Affiche un message d'erreur sous un champ
     */
    private void afficherErreur(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        label.setVisible(true);
    }

    /**
     * Cache un message d'erreur
     */
    private void cacherErreur(Label label) {
        label.setVisible(false);
    }

    /**
     * Efface tous les messages d'erreur
     */
    private void effacerErreurs() {
        cacherErreur(lblNomError);
        cacherErreur(lblTypeError);
        cacherErreur(lblPrixError);
        cacherErreur(lblEtatError);
    }

    // =====================================================
    // CHARGEMENT DES DONNÉES
    // =====================================================

    /**
     * Charge les équipements depuis la base de données
     */
    private void chargerDonnees() {
        try {
            // Récupérer tous les équipements
            List<Equipement> equipements = service.getAll();

            // Mettre à jour la liste observable
            list.setAll(equipements);

            // Lier au tableau
            tableEquipement.setItems(list);

            System.out.println("📊 " + list.size() + " équipements chargés");

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger les équipements: " + e.getMessage());
        }
    }

    // =====================================================
    // ACTIONS SUR LES BOUTONS
    // =====================================================

    /**
     * Ajouter un équipement
     */
    @FXML
    void ajouterEquipement() {
        // 1. Valider les champs
        if (!validerChamps()) {
            AlertUtils.showWarning("Validation", "Veuillez corriger les erreurs");
            return;
        }

        // 2. Créer l'objet Equipement
        try {
            Equipement equipement = new Equipement();
            equipement.setNom(tfNom.getText().trim());
            equipement.setType(tfType.getText().trim());
            equipement.setPrix(Float.parseFloat(tfPrix.getText()));
            equipement.setDisponibilite(cbEtat.getValue());

            // ⚠️ VALEUR TEMPORAIRE - À remplacer plus tard par l'ID du vrai utilisateur connecté
            equipement.setUserId(1);  // Pour l'instant, on met 1 (utilisateur par défaut)

            // 3. Ajouter en base de données
            service.create(equipement);

            // 4. Recharger les données
            chargerDonnees();

            // 5. Vider le formulaire
            viderFormulaire();

            // 6. Message de succès
            AlertUtils.showInfo("Succès", "Équipement ajouté avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate")) {
                AlertUtils.showError("Erreur", "Cet équipement existe déjà");
            } else {
                AlertUtils.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Erreur", "Format de nombre invalide");
        }
    }

    /**
     * Modifier un équipement
     */
    @FXML
    void modifierEquipement() {
        // 1. Vérifier qu'un équipement est sélectionné
        Equipement selected = tableEquipement.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un équipement");
            return;
        }

        // 2. Valider les champs
        if (!validerChamps()) {
            AlertUtils.showWarning("Validation", "Veuillez corriger les erreurs");
            return;
        }

        // 3. Mettre à jour l'objet
        try {
            selected.setNom(tfNom.getText().trim());
            selected.setType(tfType.getText().trim());
            selected.setPrix(Float.parseFloat(tfPrix.getText()));
            selected.setDisponibilite(cbEtat.getValue());
            // Note: On ne modifie PAS le userId (le propriétaire ne change pas)

            // 4. Mettre à jour en base de données
            service.update(selected);

            // 5. Recharger les données
            chargerDonnees();

            // 6. Vider le formulaire
            viderFormulaire();

            // 7. Message de succès
            AlertUtils.showInfo("Succès", "Équipement modifié avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    /**
     * Supprimer un équipement
     */
    @FXML
    void supprimerEquipement() {
        // 1. Vérifier qu'un équipement est sélectionné
        Equipement selected = tableEquipement.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un équipement");
            return;
        }

        // 2. Vérifier si l'équipement a des reviews associées
        try {
            List<Review> reviews = reviewService.getByEquipementId(selected.getId());

            // 3. Demander confirmation
            boolean confirm;
            if (!reviews.isEmpty()) {
                // Cas avec reviews associées
                confirm = AlertUtils.showConfirmation(
                        "Confirmation",
                        "⚠️ Cet équipement a " + reviews.size() + " review(s) associée(s).\n" +
                                "La suppression supprimera également toutes ses reviews.\n\n" +
                                "Voulez-vous vraiment continuer ?"
                );
            } else {
                // Cas sans reviews
                confirm = AlertUtils.showConfirmation(
                        "Confirmation",
                        "Voulez-vous vraiment supprimer l'équipement \"" + selected.getNom() + "\" ?"
                );
            }

            if (confirm) {
                // 4. Supprimer
                service.delete(selected.getId());

                // 5. Recharger les données
                chargerDonnees();

                // 6. Vider le formulaire
                viderFormulaire();

                // 7. Message de succès
                AlertUtils.showInfo("Succès", "Équipement supprimé");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de vérifier les reviews associées");
        }
    }

    /**
     * Vider le formulaire
     */
    @FXML
    void viderFormulaire() {
        tfNom.clear();
        tfType.clear();
        tfPrix.clear();
        cbEtat.setValue("Disponible");
        effacerErreurs();

        // Désélectionner dans le tableau
        tableEquipement.getSelectionModel().clearSelection();
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

    /**
     * Double-clic sur le tableau pour modifier
     */
    @FXML
    void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            modifierEquipement();
        }
    }
}
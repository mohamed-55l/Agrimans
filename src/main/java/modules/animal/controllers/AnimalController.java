package modules.animal.controllers;

import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import modules.animal.models.Animal;
import modules.animal.services.AnimalService;

import java.sql.SQLException;
import java.util.List;

public class AnimalController {

    @FXML private TableView<Animal> tableAnimal;
    @FXML private TableColumn<Animal, Integer> colId;
    @FXML private TableColumn<Animal, String> colNom;
    @FXML private TableColumn<Animal, String> colEspece;
    @FXML private TableColumn<Animal, String> colRace;
    @FXML private TableColumn<Animal, Float> colPoids;
    @FXML private TableColumn<Animal, String> colEtat;
    @FXML private TableColumn<Animal, Integer> colUser;

    @FXML private TextField tfNom;
    @FXML private TextField tfEspece;
    @FXML private TextField tfRace;
    @FXML private TextField tfPoids;
    @FXML private ComboBox<String> cbEtat;
    
    // Labels pour erreurs (si ajoutés dans le FXML)
    @FXML private Label lblNomError;
    @FXML private Label lblEspeceError;
    @FXML private Label lblPoidsError;

    private final AnimalService service = new AnimalService();
    private ObservableList<Animal> animalList;

    @FXML
    public void initialize() {
        // Initialiser les colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colRace.setCellValueFactory(new PropertyValueFactory<>("race"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatSante"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));

        // Remplir le ComboBox
        cbEtat.setItems(FXCollections.observableArrayList(
                "Sain", "Malade", "En traitement", "Gestation", "Quarantaine"
        ));

        // Sélection dans la table pour remplir les champs
        tableAnimal.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFields(newSelection);
            }
        });

        loadData();
    }

    private void loadData() {
        try {
            List<Animal> list = service.getAll();
            animalList = FXCollections.observableArrayList(list);
            tableAnimal.setItems(animalList);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur SQL", "Impossible de charger la liste des animaux: " + e.getMessage());
        }
    }

    private void fillFields(Animal a) {
        tfNom.setText(a.getNom());
        tfEspece.setText(a.getEspece());
        tfRace.setText(a.getRace() != null ? a.getRace() : "");
        tfPoids.setText(String.valueOf(a.getPoids()));
        cbEtat.setValue(a.getEtatSante());
    }

    @FXML
    void ajouterAnimal() {
        if (!validateInput()) return;

        try {
            Animal a = new Animal(
                    tfNom.getText().trim(),
                    tfEspece.getText().trim(),
                    tfRace.getText().trim(),
                    Float.parseFloat(tfPoids.getText().trim()),
                    cbEtat.getValue(),
                    core.session.SessionManager.getCurrentUserId() // On attribue par défaut à l'admin s'il ajoute, ou 0
            );

            service.create(a);
            AlertUtils.showInfo("Succès", "Animal ajouté avec succès !");
            clearFields();
            loadData();

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    void modifierAnimal() {
        Animal selected = tableAnimal.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un animal à modifier.");
            return;
        }

        if (!validateInput()) return;

        try {
            selected.setNom(tfNom.getText().trim());
            selected.setEspece(tfEspece.getText().trim());
            selected.setRace(tfRace.getText().trim());
            selected.setPoids(Float.parseFloat(tfPoids.getText().trim()));
            selected.setEtatSante(cbEtat.getValue());

            service.update(selected);
            AlertUtils.showInfo("Succès", "Animal modifié avec succès !");
            clearFields();
            loadData();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    void supprimerAnimal() {
        Animal selected = tableAnimal.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un animal à supprimer.");
            return;
        }

        if (AlertUtils.showConfirmation("Confirmation", "Voulez-vous vraiment supprimer " + selected.getNom() + " ?")) {
            try {
                service.delete(selected.getId());
                AlertUtils.showInfo("Succès", "Animal supprimé !");
                clearFields();
                loadData();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        tfNom.clear();
        tfEspece.clear();
        tfRace.clear();
        tfPoids.clear();
        cbEtat.setValue(null);
        tableAnimal.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        boolean isValid = true;

        if (tfNom.getText().trim().isEmpty()) {
            if(lblNomError != null) lblNomError.setText("Nom requis");
            isValid = false;
        } else {
            if(lblNomError != null) lblNomError.setText("");
        }

        if (tfEspece.getText().trim().isEmpty()) {
            if(lblEspeceError != null) lblEspeceError.setText("Espèce requise");
            isValid = false;
        } else {
            if(lblEspeceError != null) lblEspeceError.setText("");
        }

        try {
            Float.parseFloat(tfPoids.getText().trim());
            if(lblPoidsError != null) lblPoidsError.setText("");
        } catch (NumberFormatException e) {
            if(lblPoidsError != null) lblPoidsError.setText("Poids invalide");
            isValid = false;
        }

        if (cbEtat.getValue() == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner l'état de l'animal.");
            isValid = false;
        }

        return isValid;
    }

    @FXML void handleButtonEnter() {}
    @FXML void handleButtonExit() {}
}

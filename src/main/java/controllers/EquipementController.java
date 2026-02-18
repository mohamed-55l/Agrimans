package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import models.Equipement;
import services.EquipementService;

public class EquipementController {

    @FXML
    private TextField tfNom;

    @FXML
    private TextField tfType;

    @FXML
    private TextField tfPrix;

    EquipementService es =
            new EquipementService();

    @FXML
    void ajouterEquipement() {

        try {

            Equipement e =
                    new Equipement(
                            0,
                            tfNom.getText(),
                            tfType.getText(),
                            Float.parseFloat(tfPrix.getText()),
                            "Disponible"
                    );

            es.create(e);

            System.out.println("Ajout depuis UI ✅");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}

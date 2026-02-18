package tests;

import models.Equipement;
import services.EquipementService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        EquipementService es =
                new EquipementService();

        //Ajouter :

        /*
        Equipement e =
                new Equipement(
                        0,
                        "Tracteur Massey",
                        "Machine",
                        2500,
                        "Disponible"
                );

        try {
            es.create(e);
        }
         */

        //Afficher :

        /*
        try {
            List<Equipement> list =
                    es.getAll();

            for (Equipement e : list) {

                System.out.println(
                        e.getId() + " | " +
                                e.getNom() + " | " +
                                e.getType() + " | " +
                                e.getPrix() + " | " +
                                e.getDisponibilite()
                );
            }

        }
         */

        //Modifier :

        /*
        Equipement e =
            new Equipement(
                    1, // ID à modifier
                    "Tracteur John Deere",
                    "Machine",
                    3000,
                    "Disponible"
            );

    try {
        es.update(e);
    }
         */

        //Supprimer :

        /*
        try {
            es.delete(2); // supprimer ID = 2
        }
        */

        //GetByID :

        try {

            Equipement e =
                    es.getById(1);

            if (e != null) {

                System.out.println(
                        e.getNom() + " | " +
                                e.getPrix()
                );

            } else {
                System.out.println("Equipement introuvable");
            }

        }

         catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
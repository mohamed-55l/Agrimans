package modules.parcelle.services;

public class RendementService {

    public static double calculerRendement(String culture, double superficie) {

        double rendementParHa = 3;

        switch (culture.toLowerCase()) {

            case "blé":
                rendementParHa = 4;
                break;

            case "maïs":
                rendementParHa = 6;
                break;

            case "riz":
                rendementParHa = 5;
                break;

            case "pommes de terre":
                rendementParHa = 20;
                break;
        }

        return superficie * rendementParHa;
    }
}
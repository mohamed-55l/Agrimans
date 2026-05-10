package modules.parcelle.services;

public class RecommendationService {

    public static String recommanderCulture(String typeSol) {

        if(typeSol == null) return "Inconnu";

        switch (typeSol.toLowerCase()) {

            case "sableux":
                return "Pommes de terre";

            case "argileux":
                return "Riz";

            case "limoneux":
                return "Blé";

            case "calcaire":
                return "Olivier";

            case "agri":
                return "Blé";

            default:
                return "Culture polyvalente (Maïs)";
        }
    }
}
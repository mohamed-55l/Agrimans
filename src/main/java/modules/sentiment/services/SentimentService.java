package modules.sentiment.services;

import modules.review.models.Review;
import modules.sentiment.models.AnalyseSentiment;

import java.util.*;
import java.util.regex.Pattern;

public class SentimentService {

    // Dictionnaire de mots positifs
    private static final Set<String> MOTS_POSITIFS = new HashSet<>(Arrays.asList(
            "bon", "bien", "excellent", "parfait", "super", "génial", "top",
            "satisfait", "content", "ravi", "parfait", "impeccable", "efficace",
            "qualité", "robuste", "fiable", "performant", "facile", "pratique",
            "merci", "recommande", "bravo", "félicitations", "👍", "❤️", "😊",
            "👍🏻", "👍🏼", "👍🏽", "génial", "magnifique", "incroyable"
    ));

    // Dictionnaire de mots négatifs (génèrent un warning)
    private static final Map<String, Integer> MOTS_NEGATIFS = new LinkedHashMap<>();
    static {
        // Mots critiques (warning immédiat)
        String[] motsCritiques = {
                "cassé", "cassée", "casser", "panne", "pannes", "en panne",
                "problème", "problèmes", "mauvais", "mauvaise", "mal",
                "déçu", "déçue", "deçu", "décevant", "décevante",
                "inutilisable", "inutile", "danger", "dangereux",
                "exploser", "incendie", "feu", "brûlé", "casse",
                "réparation", "réparer", "maintenance", "sav", "garantie",
                "cher", "chère", "trop cher", "prix élevé", "arnaque",
                "voleur", "escroc", "mensonge", "faux", "escroquerie",
                "honteux", "honte", "scandale", "inacceptable"
        };

        // Mots d'avertissement (warning si plusieurs)
        String[] motsWarning = {
                "bruit", "bruyant", "vibration", "secousse",
                "lent", "lente", "rapide", "vitesse",
                "consommation", "essence", "diesel", "carburant",
                "difficile", "compliqué", "complexe",
                "fragile", "sensible", "délicat",
                "rouille", "corrosion", "oxydation",
                "fuit", "fuite", "goutte",
                "fatigué", "usé", "vieux", "vieilli"
        };

        for (String mot : motsCritiques) {
            MOTS_NEGATIFS.put(mot, 1); // Un seul mot suffit pour warning
        }
        for (String mot : motsWarning) {
            MOTS_NEGATIFS.put(mot, 2); // Besoin de 2 occurrences
        }
    }

    // Mots neutres (stop words)
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "le", "la", "les", "un", "une", "des", "du", "de", "ce", "cet", "cette",
            "et", "ou", "mais", "donc", "car", "ni", "là", "en", "dans", "par",
            "pour", "avec", "sans", "chez", "sur", "sous", "entre", "vers",
            "je", "tu", "il", "elle", "nous", "vous", "ils", "elles",
            "mon", "ton", "son", "notre", "votre", "leur",
            "est", "sont", "ai", "as", "a", "avons", "avez", "ont",
            "être", "avoir", "faire", "aller", "venir", "voir"
    ));

    /**
     * Analyse le sentiment d'un commentaire
     */
    public AnalyseSentiment analyser(String commentaire, int reviewId) {
        AnalyseSentiment result = new AnalyseSentiment();
        result.setReviewId(reviewId);
        result.setCommentaire(commentaire);

        String texte = commentaire.toLowerCase()
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[îï]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[^a-zA-Z0-9\\s]", " ");

        // Compter les mots positifs et négatifs
        int positifCount = 0;
        int negatifCount = 0;
        List<String> motsTrouves = new ArrayList<>();

        // Détection des mots négatifs
        for (Map.Entry<String, Integer> entry : MOTS_NEGATIFS.entrySet()) {
            String mot = entry.getKey();

            // Compter les occurrences
            int occurrences = compterOccurrences(texte, mot);
            if (occurrences > 0) {
                negatifCount += occurrences;
                for (int i = 0; i < occurrences; i++) {
                    motsTrouves.add(mot);
                }
            }
        }

        // Détection des mots positifs
        for (String mot : MOTS_POSITIFS) {
            int occurrences = compterOccurrences(texte, mot);
            if (occurrences > 0) {
                positifCount += occurrences;
            }
        }

        // Calcul des scores
        int totalMots = compterMotsUtiles(texte);
        if (totalMots == 0) totalMots = 1;

        result.setScorePositif((double) positifCount / totalMots);
        result.setScoreNegatif((double) negatifCount / totalMots);
        result.setScoreNeutre(1.0 - (result.getScorePositif() + result.getScoreNegatif()));

        // Déterminer le sentiment
        if (negatifCount > 0 && negatifCount >= positifCount) {
            result.setSentiment("NEGATIF");

            // Vérifier si warning nécessaire
            if (negatifCount >= 2 || motsTrouves.stream().anyMatch(m ->
                    m.equals("cassé") || m.equals("panne") || m.equals("danger") ||
                            m.equals("problème") || m.equals("inutilisable") || m.equals("arnaque"))) {
                result.setWarning(true);
            }

        } else if (positifCount > 0 && positifCount > negatifCount) {
            result.setSentiment("POSITIF");
        } else {
            result.setSentiment("NEUTRE");
        }

        result.setMotsClesNegatifs(motsTrouves);

        return result;
    }

    /**
     * Analyse une review existante
     */
    public AnalyseSentiment analyser(Review review) {
        return analyser(review.getCommentaire(), review.getId());
    }

    /**
     * ✅ NOUVELLE MÉTHODE - Analyse en masse toutes les reviews
     */
    public List<AnalyseSentiment> analyserToutes(List<Review> reviews) {
        List<AnalyseSentiment> resultats = new ArrayList<>();
        for (Review review : reviews) {
            try {
                resultats.add(analyser(review));
            } catch (Exception e) {
                System.err.println("Erreur analyse review " + review.getId() + ": " + e.getMessage());
                // Ajouter une analyse vide en cas d'erreur
                AnalyseSentiment fallback = new AnalyseSentiment();
                fallback.setReviewId(review.getId());
                fallback.setCommentaire(review.getCommentaire());
                fallback.setSentiment("NEUTRE");
                resultats.add(fallback);
            }
        }
        return resultats;
    }

    /**
     * Compte les occurrences d'un mot dans un texte
     */
    private int compterOccurrences(String texte, String mot) {
        int count = 0;
        int index = 0;
        while ((index = texte.indexOf(mot, index)) != -1) {
            // Vérifier que c'est un mot entier (pas une partie d'un autre mot)
            boolean debutMot = index == 0 || !Character.isLetter(texte.charAt(index - 1));
            boolean finMot = index + mot.length() >= texte.length() ||
                    !Character.isLetter(texte.charAt(index + mot.length()));

            if (debutMot && finMot) {
                count++;
            }
            index += mot.length();
        }
        return count;
    }

    /**
     * Compte le nombre de mots utiles (sans les stop words)
     */
    private int compterMotsUtiles(String texte) {
        String[] mots = texte.split("\\s+");
        int count = 0;
        for (String mot : mots) {
            if (mot.length() > 2 && !STOP_WORDS.contains(mot)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Récupère uniquement les reviews avec warning
     */
    public List<AnalyseSentiment> getWarnings(List<Review> reviews) {
        return analyserToutes(reviews).stream()
                .filter(AnalyseSentiment::isWarning)
                .toList();
    }

    /**
     * Obtient des statistiques globales sur les sentiments
     */
    public Map<String, Object> getStatistiques(List<Review> reviews) {
        List<AnalyseSentiment> analyses = analyserToutes(reviews);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", analyses.size());
        stats.put("positifs", analyses.stream().filter(a -> "POSITIF".equals(a.getSentiment())).count());
        stats.put("negatifs", analyses.stream().filter(a -> "NEGATIF".equals(a.getSentiment())).count());
        stats.put("neutres", analyses.stream().filter(a -> "NEUTRE".equals(a.getSentiment())).count());
        stats.put("warnings", analyses.stream().filter(AnalyseSentiment::isWarning).count());

        return stats;
    }
}
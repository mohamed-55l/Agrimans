package modules.chatbot.services;

import java.util.*;
import java.util.regex.Pattern;

public class ChatbotService {

    // Base de connaissances agricole
    private final Map<String, String> reponses;
    private final List<String> questionsFrequentes;

    public ChatbotService() {
        reponses = new HashMap<>();
        questionsFrequentes = new ArrayList<>();
        initialiserConnaissances();
    }

    private void initialiserConnaissances() {
        // Équipements
        reponses.put("tracteur|tracteurs",
                "🚜 Les tracteurs sont des engins agricoles motorisés. Ils servent à:\n" +
                        "- Labourer les champs\n" +
                        "- Tirer des remorques\n" +
                        "- Actionner des outils (charrues, semoirs)\n\n" +
                        "💡 Conseil: Faites l'entretien tous les 6 mois !");

        reponses.put("semoir|semoirs",
                "🌱 Le semoir sert à planter les graines de façon régulière.\n\n" +
                        "✅ Avantages:\n" +
                        "- Économie de semences\n" +
                        "- Répartition homogène\n" +
                        "- Gain de temps\n\n" +
                        "📏 Profondeur idéale: 2-3 cm selon la culture");

        reponses.put("panne|pannes|réparation",
                "🔧 En cas de panne:\n\n" +
                        "1. Signalez-la dans l'application\n" +
                        "2. Le chef sera notifié\n" +
                        "3. Un technicien interviendra\n\n" +
                        "⏱️ Délai moyen: 24-48h\n\n" +
                        "🚑 Urgence ? Contactez le 12345678");

        // Cultures
        reponses.put("blé|ble",
                "🌾 CULTURE DU BLÉ:\n\n" +
                        "📅 Semis: Octobre-Novembre\n" +
                        "🌡️ Température idéale: 15-20°C\n" +
                        "💧 Besoin en eau: 400-500mm\n" +
                        "📊 Rendement moyen: 60-80 quintaux/ha\n" +
                        "🔔 Récolte: Juin-Juillet");

        reponses.put("maïs|mais",
                "🌽 CULTURE DU MAÏS:\n\n" +
                        "📅 Semis: Avril-Mai\n" +
                        "🌡️ Température idéale: 20-25°C\n" +
                        "💧 Besoin en eau: 500-600mm\n" +
                        "📊 Rendement moyen: 80-100 quintaux/ha\n" +
                        "🔔 Récolte: Septembre-Octobre");

        reponses.put("maladie|maladies|traitement",
                "🦠 MALADIES COURANTES:\n\n" +
                        "1. Mildiou → Bouillie bordelaise\n" +
                        "2. Oïdium → Soufre\n" +
                        "3. Rouille → Fongicides spécifiques\n\n" +
                        "✅ Prévention:\n" +
                        "- Rotation des cultures\n" +
                        "- Variétés résistantes\n" +
                        "- Traitements préventifs");

        reponses.put("engrais|fertilisant",
                "🧪 FERTILISATION:\n\n" +
                        "🌱 Azote (N): Croissance\n" +
                        "🌸 Phosphore (P): Racines\n" +
                        "🍎 Potassium (K): Fruits\n\n" +
                        "📊 Dosages recommandés:\n" +
                        "- Blé: 150-200 kg/ha\n" +
                        "- Maïs: 200-250 kg/ha\n" +
                        "- Légumes: 100-150 kg/ha");

        reponses.put("arrosage|irrigation",
                "💧 IRRIGATION:\n\n" +
                        "⏰ Meilleurs moments:\n" +
                        "- Tôt le matin (5h-8h)\n" +
                        "- Fin d'après-midi (17h-20h)\n\n" +
                        "📏 Fréquence:\n" +
                        "- Été: tous les 3-4 jours\n" +
                        "- Printemps: tous les 7 jours\n" +
                        "- Hiver: selon pluie");

        // Général
        reponses.put("bonjour|salut|coucou",
                "👋 Bonjour ! Je suis votre assistant agricole.\n\n" +
                        "Posez-moi des questions sur:\n" +
                        "🔧 Les équipements\n" +
                        "🌾 Les cultures\n" +
                        "🦠 Les maladies\n" +
                        "💧 L'irrigation\n" +
                        "🧪 Les engrais");

        reponses.put("merci|thanks",
                "🌟 Avec plaisir ! N'hésitez pas si vous avez d'autres questions.");

        reponses.put("aide|help|commandes",
                "📚 COMMANDES DISPONIBLES:\n\n" +
                        "• tractor / tracteur\n" +
                        "• semoir\n" +
                        "• blé / maïs\n" +
                        "• maladie / traitement\n" +
                        "• engrais\n" +
                        "• arrosage\n" +
                        "• météo\n" +
                        "• panne\n\n" +
                        "💡 Tapez un mot-clé pour plus d'infos !");

        // Questions fréquentes
        questionsFrequentes.addAll(Arrays.asList(
                "Comment entretenir un tracteur ?",
                "Quand semer le blé ?",
                "Que faire en cas de panne ?",
                "Comment traiter le mildiou ?",
                "Quel engrais pour le maïs ?",
                "Fréquence d'arrosage ?"
        ));
    }

    public String getReponse(String message) {
        message = message.toLowerCase().trim();

        // Vérifier les mots-clés
        for (Map.Entry<String, String> entry : reponses.entrySet()) {
            String[] motsCles = entry.getKey().split("\\|");
            for (String motCle : motsCles) {
                if (message.contains(motCle)) {
                    return entry.getValue();
                }
            }
        }

        // Si message trop court
        if (message.length() < 3) {
            return "❓ Pouvez-vous être plus précis ? Tapez 'aide' pour voir les commandes.";
        }

        // Réponse par défaut
        return "🤔 Je n'ai pas encore appris à répondre à cette question.\n\n" +
                "💡 Tapez 'aide' pour voir ce que je sais faire !\n\n" +
                "📧 Ou contactez le support: support@agrimans.com";
    }

    public List<String> getQuestionsFrequentes() {
        return questionsFrequentes;
    }
}
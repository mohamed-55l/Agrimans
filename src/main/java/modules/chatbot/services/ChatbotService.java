package modules.chatbot.services;

import java.util.*;
import java.util.stream.*;

public class ChatbotService {

    // ═══════════════════════════════════════════════════════════
    //  STRUCTURES DE DONNÉES INTELLIGENTES
    // ═══════════════════════════════════════════════════════════

    /** Une entrée de connaissance agricole avec mots-clés pondérés */
    private static class Knowledge {
        final String id;
        final String[] keywords;      // mots-clés principaux
        final String[] synonyms;      // variantes et synonymes
        final String response;        // réponse formatée
        final String category;        // catégorie (culture, maladie, équipement...)
        final double weight;          // importance/priorité

        Knowledge(String id, String[] keywords, String[] synonyms,
                  String response, String category, double weight) {
            this.id = id;
            this.keywords = keywords;
            this.synonyms = synonyms;
            this.response = response;
            this.category = category;
            this.weight = weight;
        }
    }

    /** Un tour de conversation */
    private static class ConversationTurn {
        final String userMessage;
        final String botResponse;
        final String detectedCategory;
        final long timestamp;

        ConversationTurn(String userMessage, String botResponse, String detectedCategory) {
            this.userMessage = userMessage;
            this.botResponse = botResponse;
            this.detectedCategory = detectedCategory;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ÉTAT DU SERVICE
    // ═══════════════════════════════════════════════════════════

    private final List<Knowledge> knowledgeBase = new ArrayList<>();
    private final Deque<ConversationTurn> conversationHistory = new ArrayDeque<>();
    private final Map<String, Integer> unknownQuestionsLog = new HashMap<>();
    private final Map<String, Integer> statistiquesQuestions = new HashMap<>();
    private final List<String> questionsFrequentes = new ArrayList<>();

    private static final int MAX_HISTORY = 5;
    private static final double SIMILARITY_THRESHOLD = 0.15;

    // Mots vides français (stop words)
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "le", "la", "les", "un", "une", "des", "de", "du", "en", "et", "est",
            "je", "tu", "il", "elle", "nous", "vous", "ils", "elles", "mon", "ma",
            "mes", "ton", "ta", "tes", "son", "sa", "ses", "que", "qui", "quoi",
            "comment", "pourquoi", "quand", "où", "quel", "quelle", "quels", "quelles",
            "pour", "par", "sur", "sous", "dans", "avec", "sans", "au", "aux",
            "se", "me", "te", "lui", "y", "en", "ne", "pas", "plus", "très",
            "bien", "aussi", "mais", "ou", "donc", "car", "ni", "si", "or",
            "ce", "cet", "cette", "ces", "faire", "avoir", "être", "fait", "peut",
            "faut", "doit", "dois", "dois", "veut", "veux", "suis", "sont"
    ));

    public ChatbotService() {
        initialiserConnaissances();
        initialiserQuestionsFrequentes();
    }

    // ═══════════════════════════════════════════════════════════
    //  MOTEUR NLP — TF-IDF + COSINE SIMILARITY
    // ═══════════════════════════════════════════════════════════

    /** Normalise et tokenise un texte français */
    private List<String> tokenize(String text) {
        return Arrays.stream(
                        text.toLowerCase()
                                .replaceAll("[àáâã]", "a")
                                .replaceAll("[éèêë]", "e")
                                .replaceAll("[îï]", "i")
                                .replaceAll("[ôö]", "o")
                                .replaceAll("[ùûü]", "u")
                                .replaceAll("[ç]", "c")
                                .replaceAll("[^a-z0-9\\s]", " ")
                                .split("\\s+")
                )
                .filter(w -> w.length() > 2 && !STOP_WORDS.contains(w))
                .map(this::stem)
                .collect(Collectors.toList());
    }

    /** Stemming léger pour le français (suppression de suffixes courants) */
    private String stem(String word) {
        if (word.length() <= 4) return word;
        // Suffixes courants en français
        String[][] rules = {
                {"ement", ""}, {"ements", ""}, {"ation", ""}, {"ations", ""},
                {"eur", ""}, {"eurs", ""}, {"euse", ""}, {"euses", ""},
                {"ique", ""}, {"iques", ""}, {"iser", ""}, {"isation", ""},
                {"ment", ""}, {"ments", ""}, {"ier", ""}, {"iers", ""},
                {"age", ""}, {"ages", ""}, {"ais", ""}, {"ait", ""},
                {"ance", ""}, {"ances", ""}, {"ible", ""}, {"ibles", ""},
                {"tion", ""}, {"tions", ""}, {"eux", ""}, {"ant", ""},
                {"ants", ""}, {"ent", ""}, {"ents", ""}, {"aux", "al"},
                {"ielle", ""}, {"iel", ""}, {"ielle", ""}, {"eux", ""},
                {"ures", "ur"}, {"ure", "ur"}, {"ites", "it"}, {"ite", "it"},
                {"ions", "ion"}, {"ons", "on"}, {"ees", "e"}, {"es", ""},
                {"er", ""}, {"ir", ""}, {"re", ""}, {"s", ""}
        };
        for (String[] rule : rules) {
            if (word.endsWith(rule[0]) && word.length() - rule[0].length() >= 3) {
                return word.substring(0, word.length() - rule[0].length()) + rule[1];
            }
        }
        return word;
    }

    /** Calcule le vecteur TF (fréquence des termes) d'un texte */
    private Map<String, Double> computeTF(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1.0, Double::sum);
        double total = tokens.size();
        tf.replaceAll((k, v) -> v / total);
        return tf;
    }

    /** Calcule la similarité cosinus entre deux vecteurs */
    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        double dotProduct = 0, norm1 = 0, norm2 = 0;
        Set<String> allKeys = new HashSet<>(v1.keySet());
        allKeys.addAll(v2.keySet());
        for (String key : allKeys) {
            double a = v1.getOrDefault(key, 0.0);
            double b = v2.getOrDefault(key, 0.0);
            dotProduct += a * b;
        }
        for (double v : v1.values()) norm1 += v * v;
        for (double v : v2.values()) norm2 += v * v;
        if (norm1 == 0 || norm2 == 0) return 0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /** Score de pertinence composite pour une Knowledge */
    private double scoreKnowledge(Knowledge k, List<String> queryTokens, Map<String, Double> queryTF) {
        // 1. Score TF-IDF cosinus sur les mots-clés
        List<String> kwTokens = new ArrayList<>();
        for (String kw : k.keywords) kwTokens.addAll(tokenize(kw));
        for (String syn : k.synonyms) kwTokens.addAll(tokenize(syn));
        Map<String, Double> kwTF = computeTF(kwTokens);
        double cosScore = cosineSimilarity(queryTF, kwTF);

        // 2. Bonus exact match sur les mots-clés
        double exactBonus = 0;
        for (String token : queryTokens) {
            for (String kw : k.keywords) {
                if (tokenize(kw).contains(token)) exactBonus += 0.3;
            }
            for (String syn : k.synonyms) {
                if (tokenize(syn).contains(token)) exactBonus += 0.15;
            }
        }

        // 3. Bonus contextuel (si même catégorie que dernier échange)
        double contextBonus = 0;
        if (!conversationHistory.isEmpty()) {
            ConversationTurn last = conversationHistory.peekLast();
            if (last != null && k.category.equals(last.detectedCategory)) {
                contextBonus = 0.1;
            }
        }

        return (cosScore + exactBonus + contextBonus) * k.weight;
    }

    // ═══════════════════════════════════════════════════════════
    //  DÉTECTION D'INTENTION
    // ═══════════════════════════════════════════════════════════

    private enum Intent {
        GREETING, FAREWELL, THANKS, HELP, URGENCY,
        DIAGNOSIS_SYMPTOM, QUESTION, STATEMENT, UNKNOWN
    }

    private Intent detectIntent(String message) {
        String m = message.toLowerCase();
        if (m.matches(".*(bonjour|salut|coucou|hello|bonsoir|hola|salam).*")) return Intent.GREETING;
        if (m.matches(".*(au revoir|bye|goodbye|a bientot|bonne journee).*")) return Intent.FAREWELL;
        if (m.matches(".*(merci|thanks|thank you|super|parfait|excellent).*")) return Intent.THANKS;
        if (m.matches(".*(aide|help|commande|menu|liste|que sais|peux tu).*")) return Intent.HELP;
        if (m.matches(".*(urgent|urgence|vite|rapidement|grave|catastrophe|detruire|mourir).*")) return Intent.URGENCY;
        if (m.matches(".*(tache|symptome|jaun|brun|noir|blanc|pourri|moisissure|fletr).*")) return Intent.DIAGNOSIS_SYMPTOM;
        if (m.contains("?")) return Intent.QUESTION;
        return Intent.STATEMENT;
    }

    // ═══════════════════════════════════════════════════════════
    //  POINT D'ENTRÉE PRINCIPAL
    // ═══════════════════════════════════════════════════════════

    public String getReponse(String message) {
        if (message == null || message.isBlank()) {
            return "❓ Veuillez saisir votre question. Tapez **aide** pour voir ce que je sais faire.";
        }

        // Stats
        String msgKey = message.toLowerCase().trim();
        statistiquesQuestions.merge(msgKey, 1, Integer::sum);

        // Détection d'intention
        Intent intent = detectIntent(message);

        // Réponses directes selon l'intention
        switch (intent) {
            case GREETING: return handleGreeting();
            case FAREWELL: return "👋 **Au revoir !** Bonne culture et à bientôt ! 🌾";
            case THANKS:   return "🌟 **Avec plaisir !** N'hésitez pas pour d'autres questions agricoles. 🌱";
            case HELP:     return handleHelp();
            case URGENCY:  return handleUrgency(message);
            default:       break;
        }

        // Diagnostic par symptôme si pertinent
        if (intent == Intent.DIAGNOSIS_SYMPTOM) {
            String diag = tryDiagnosis(message);
            if (diag != null) return addToHistory(message, diag, "maladie");
        }

        // Tokenisation + recherche NLP
        List<String> tokens = tokenize(message);
        if (tokens.isEmpty()) {
            return "❓ Je n'ai pas compris. Tapez **aide** pour voir les commandes disponibles.";
        }
        Map<String, Double> queryTF = computeTF(tokens);

        // Scoring de toutes les entrées
        Knowledge best = null;
        double bestScore = 0;
        for (Knowledge k : knowledgeBase) {
            double score = scoreKnowledge(k, tokens, queryTF);
            if (score > bestScore) {
                bestScore = score;
                best = k;
            }
        }

        if (best != null && bestScore >= SIMILARITY_THRESHOLD) {
            logUnknownIfNeeded(message, true);
            return addToHistory(message, best.response, best.category);
        }

        // Aucune correspondance
        logUnknownIfNeeded(message, false);
        return addToHistory(message, buildDefaultResponse(message), "inconnu");
    }

    private String addToHistory(String msg, String response, String category) {
        conversationHistory.addLast(new ConversationTurn(msg, response, category));
        if (conversationHistory.size() > MAX_HISTORY) conversationHistory.pollFirst();
        return response;
    }

    private void logUnknownIfNeeded(String message, boolean found) {
        if (!found) {
            unknownQuestionsLog.merge(message.toLowerCase().trim(), 1, Integer::sum);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DIAGNOSTIC PAR SYMPTÔMES
    // ═══════════════════════════════════════════════════════════

    private String tryDiagnosis(String message) {
        String m = message.toLowerCase();
        List<String> clues = new ArrayList<>();

        // Collecte des indices symptomatiques
        if (m.matches(".*tach.*(jaun|huileux|huile).*") || m.matches(".*jaun.*feuill.*"))
            clues.add("mildiou");
        if (m.matches(".*poudre.*blanc.*") || m.matches(".*blanc.*farineux.*") || m.contains("oidium"))
            clues.add("oidium");
        if (m.matches(".*pustul.*(orange|rouille).*") || m.matches(".*tach.*(rouill|brun).*"))
            clues.add("rouille");
        if (m.matches(".*moisissure.*gris.*") || m.matches(".*pourrit.*gris.*") || m.contains("botrytis"))
            clues.add("botrytis");
        if (m.matches(".*tach.*noir.*") || m.matches(".*noir.*brun.*fruit.*"))
            clues.add("alternariose");
        if (m.matches(".*tige.*blanc.*moisi.*") || m.contains("sclerotinia") || m.contains("sclerotiniose"))
            clues.add("sclerotinia");
        if (m.matches(".*petits insect.*vert.*") || m.matches(".*feuill.*recroquevill.*") || m.contains("puceron"))
            clues.add("puceron");
        if (m.matches(".*trou.*feuill.*") || m.contains("doryphore") || m.matches(".*larve.*mange.*"))
            clues.add("doryphore");

        if (clues.isEmpty()) return null;

        // Recherche de la connaissance correspondante
        String target = clues.get(0);
        for (Knowledge k : knowledgeBase) {
            if (k.id.equals(target) || Arrays.asList(k.keywords).contains(target)) {
                return "🔬 **Diagnostic basé sur vos symptômes :**\n\n" + k.response;
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    //  RÉPONSES SPÉCIALES
    // ═══════════════════════════════════════════════════════════

    private String handleGreeting() {
        String ctx = "";
        if (!conversationHistory.isEmpty()) {
            ctx = "\n\n💡 Nous parlions de **" + conversationHistory.peekLast().detectedCategory +
                    "** précédemment. Voulez-vous continuer ?";
        }
        return "👋 **Bonjour !** Je suis **AgriBot** 🌾, votre assistant agricole intelligent.\n\n" +
                "Je maîtrise :\n" +
                "   🌾 Cultures : blé, maïs, colza, tournesol, orge, sorgho...\n" +
                "   🦠 Maladies & ravageurs : diagnostic, traitement, prévention\n" +
                "   🧪 Fertilisation : NPK, oligo-éléments, doses précises\n" +
                "   💧 Irrigation : méthodes, calendriers, besoins par culture\n" +
                "   🚜 Équipements : tracteurs, semoirs, moissonneuses, pulvérisateurs\n" +
                "   🌍 Sol & travail du sol\n" +
                "   ☀️ Météo & protection contre le gel\n\n" +
                "💡 **Conseil :** Décrivez vos symptômes pour un diagnostic ou tapez **aide**." + ctx;
    }

    private String handleHelp() {
        return "📚 **GUIDE COMPLET — AGRIBOT v2.0**\n\n" +
                "🌾 **CULTURES**\n" +
                "   Tapez : blé · maïs · colza · tournesol · orge · sorgho · pomme de terre · tomate\n\n" +
                "🦠 **MALADIES** (ou décrivez vos symptômes !)\n" +
                "   Tapez : mildiou · oïdium · rouille · botrytis · alternariose · sclérotinia\n" +
                "   Exemple : *'mes feuilles ont des taches jaunes'* → diagnostic automatique\n\n" +
                "🐛 **RAVAGEURS**\n" +
                "   Tapez : pucerons · doryphore · altise · taupin · thrips · limace\n\n" +
                "🧪 **FERTILISATION**\n" +
                "   Tapez : engrais · azote · phosphore · potassium · NPK · fumure\n\n" +
                "💧 **IRRIGATION**\n" +
                "   Tapez : arrosage · irrigation · goutte à goutte · aspersion\n\n" +
                "🚜 **ÉQUIPEMENTS**\n" +
                "   Tapez : tracteur · semoir · moissonneuse · pulvérisateur · panne\n\n" +
                "🌍 **SOL**\n" +
                "   Tapez : sol · labour · pH · argile · limon · rotation\n\n" +
                "☀️ **MÉTÉO**\n" +
                "   Tapez : météo · gel · sécheresse · canicule · grêle\n\n" +
                "🌱 **AGRICULTURE DURABLE**\n" +
                "   Tapez : bio · agroécologie · compost · couvert végétal\n\n" +
                "📊 **GESTION**\n" +
                "   Tapez : rendement · rotation des cultures · calendrier cultural";
    }

    private String handleUrgency(String message) {
        return "🚨 **SITUATION D'URGENCE DÉTECTÉE**\n\n" +
                "Voici les étapes immédiates :\n\n" +
                "1️⃣ **Isoler** la zone atteinte si possible\n" +
                "2️⃣ **Photographier** les symptômes (feuilles, tiges, fruits)\n" +
                "3️⃣ **Identifier** : tapez le nom de la maladie ou décrivez les symptômes\n" +
                "4️⃣ **Signaler** dans l'application\n\n" +
                "📞 **Contacts d'urgence :**\n" +
                "   • Technicien agricole : 12345678\n" +
                "   • Support application : support@agrimans.com\n" +
                "   • Chambre d'agriculture : votre région\n\n" +
                "💡 Décrivez-moi précisément ce que vous observez pour un diagnostic immédiat.";
    }

    private String buildDefaultResponse(String message) {
        // Suggestions intelligentes basées sur les mots présents
        List<String> suggestions = new ArrayList<>();
        String m = message.toLowerCase();
        if (m.matches(".*(plant|pousse|croiss|lev).*")) suggestions.add("blé, maïs, colza");
        if (m.matches(".*(sol|terre|champ|parcel).*")) suggestions.add("sol, labour, rotation");
        if (m.matches(".*(eau|pluie|sec|humid).*")) suggestions.add("irrigation, arrosage");
        if (m.matches(".*(traitment|product|appliqu).*")) suggestions.add("maladie, engrais, pulvérisateur");

        String sug = suggestions.isEmpty() ? "blé, mildiou, engrais, tracteur" :
                String.join(", ", suggestions);

        String context = "";
        if (!conversationHistory.isEmpty()) {
            context = "\n\n🔗 Nous parlions de **" + conversationHistory.peekLast().detectedCategory +
                    "** — voulez-vous continuer sur ce sujet ?";
        }

        unknownQuestionsLog.merge(message.toLowerCase().trim(), 1, Integer::sum);

        return "🤔 Je n'ai pas encore de réponse précise à cette question.\n\n" +
                "💡 **Essayez des mots-clés comme :** " + sug + "\n\n" +
                "🔬 **Astuce :** Si vous observez des symptômes sur vos plantes, décrivez-les\n" +
                "   (ex: *'feuilles avec taches jaunes'*, *'poudre blanche sur les feuilles'*)\n\n" +
                "📧 Question non couverte ? Contactez : support@agrimans.com" + context;
    }

    // ═══════════════════════════════════════════════════════════
    //  BASE DE CONNAISSANCES ENRICHIE
    // ═══════════════════════════════════════════════════════════

    private void initialiserConnaissances() {

        // ── SALUTATIONS ─────────────────────────────────────────────
        add("bonjour", arr("bonjour", "salut", "coucou", "hello"),
                arr("bonsoir", "hola", "salam", "hey"),
                handleGreeting(), "salutation", 1.0);

        // ── BLÉ ─────────────────────────────────────────────────────
        add("ble", arr("blé", "ble", "froment", "céréale"),
                arr("triticum", "grain", "épi"),
                "🌾 **CULTURE DU BLÉ**\n\n" +
                        "📅 **Calendrier complet :**\n" +
                        "   • Semis : Oct-Nov (blé d'hiver) | Fév-Mar (blé de printemps)\n" +
                        "   • Tallage : Nov-Fév\n" +
                        "   • Montaison : Mar-Avr\n" +
                        "   • Épiaison : Mai\n" +
                        "   • Floraison : Mai-Juin\n" +
                        "   • Maturité & Récolte : Juin-Juil\n\n" +
                        "🌡️ **Conditions idéales :**\n" +
                        "   • Température optimale : 15-20°C\n" +
                        "   • Pluviométrie : 400-600 mm/cycle\n" +
                        "   • pH sol : 6.0-7.5\n" +
                        "   • Sol : limoneux ou argilo-limoneux\n\n" +
                        "📊 **Rendements :**\n" +
                        "   • Moyen : 60-80 q/ha\n" +
                        "   • Bon : 80-100 q/ha\n" +
                        "   • Exceptionnel : >100 q/ha\n\n" +
                        "🧪 **Fertilisation recommandée :**\n" +
                        "   • Azote N : 150-200 kg/ha (fractionné en 2-3 apports)\n" +
                        "   • Phosphore P : 60-80 kg/ha (avant semis)\n" +
                        "   • Potassium K : 80-100 kg/ha\n" +
                        "   • Soufre S : 20-30 kg/ha (important pour qualité)\n\n" +
                        "🦠 **Maladies principales :**\n" +
                        "   • Rouille brune/jaune → fongicides triazoles\n" +
                        "   • Septoriose → surveillance dès montaison\n" +
                        "   • Fusariose épis → risque mycotoxines\n" +
                        "   • Oïdium → soufre ou strobilurines\n\n" +
                        "💡 **Conseil :** Fractionnez l'azote : 1/3 au semis, 1/3 tallage, 1/3 montaison.",
                "culture", 1.0);

        // ── MAÏS ─────────────────────────────────────────────────────
        add("mais", arr("maïs", "mais", "corn"),
                arr("zea mays", "grain", "ensilage", "fourrage"),
                "🌽 **CULTURE DU MAÏS**\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Semis : Avr-Mai (sol >10°C)\n" +
                        "   • Levée : 10-15 jours après semis\n" +
                        "   • Floraison : Juil-Août\n" +
                        "   • Récolte grain : Sep-Oct (humidité <15%)\n" +
                        "   • Récolte ensilage : Août-Sep\n\n" +
                        "🌡️ **Exigences :**\n" +
                        "   • Température sol semis : >10°C\n" +
                        "   • Température idéale croissance : 20-25°C\n" +
                        "   • Besoin en eau : 500-600 mm/cycle\n" +
                        "   • Très sensible au gel (0°C fatal)\n\n" +
                        "📊 **Rendements :**\n" +
                        "   • Grain : 80-120 q/ha\n" +
                        "   • Ensilage : 40-60 t MS/ha\n\n" +
                        "🧪 **Fertilisation :**\n" +
                        "   • N : 200-250 kg/ha (méthode CAU)\n" +
                        "   • P : 80-100 kg/ha\n" +
                        "   • K : 100-150 kg/ha\n" +
                        "   • Mg : 30-50 kg/ha\n\n" +
                        "🐛 **Ravageurs principaux :**\n" +
                        "   • Taupin → traitement semences\n" +
                        "   • Pyrale → trichogrammes (bio) ou insecticides\n" +
                        "   • Sésamie → surveillance pièges\n\n" +
                        "💡 **Conseil :** Ne semez jamais avant que le sol atteigne 10°C sur 10 cm.",
                "culture", 1.0);

        // ── COLZA ─────────────────────────────────────────────────────
        add("colza", arr("colza", "canola", "oléagineux"),
                arr("brassica napus", "huile", "biodiesel"),
                "🌼 **CULTURE DU COLZA**\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Semis : Mi-août à mi-septembre\n" +
                        "   • Stade rosette avant hiver\n" +
                        "   • Reprise végétation : Fév-Mar\n" +
                        "   • Floraison : Mar-Mai\n" +
                        "   • Récolte : Fin juin à mi-juillet\n\n" +
                        "🌡️ **Conditions :**\n" +
                        "   • Résistant au froid (jusqu'à -15°C)\n" +
                        "   • Peu exigeant en eau (300-450 mm)\n" +
                        "   • pH optimal : 6.5-7.5\n\n" +
                        "📊 **Rendements :**\n" +
                        "   • Moyen : 30-40 q/ha\n" +
                        "   • Excellent : >45 q/ha\n" +
                        "   • Teneur en huile : 42-46%\n\n" +
                        "🧪 **Fertilisation :**\n" +
                        "   • N : 180-220 kg/ha (7 kg N par q produit)\n" +
                        "   • Soufre S : INDISPENSABLE 40-60 kg/ha\n" +
                        "   • P : 60-80 kg/ha\n" +
                        "   • Bore B : oligo-élément crucial\n\n" +
                        "🦠 **Maladies :**\n" +
                        "   • Sclérotinia (floraison) → fongicide préventif\n" +
                        "   • Phoma → variétés résistantes\n" +
                        "   • Alternariose → conditions humides\n\n" +
                        "🐛 **Ravageurs :**\n" +
                        "   • Altises à l'automne → semis précoce\n" +
                        "   • Méligèthes au printemps → seuil 1/plante\n" +
                        "   • Charançons des siliques\n\n" +
                        "💡 **Avantage agronomique :** +10% rendement blé après colza !",
                "culture", 1.0);

        // ── TOURNESOL ─────────────────────────────────────────────────
        add("tournesol", arr("tournesol", "soleil", "hélianthus"),
                arr("oléagineux", "huile de tournesol"),
                "🌻 **CULTURE DU TOURNESOL**\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Semis : Avr-Mai (sol >8°C)\n" +
                        "   • Floraison : Juil-Août\n" +
                        "   • Récolte : Sep-Oct\n\n" +
                        "🌡️ **Points forts :**\n" +
                        "   • Très résistant à la sécheresse\n" +
                        "   • Besoin en eau : 300-400 mm seulement\n" +
                        "   • Idéal en zones semi-arides\n\n" +
                        "📊 **Rendements :**\n" +
                        "   • Grain : 25-40 q/ha\n" +
                        "   • Teneur en huile : 45-52%\n\n" +
                        "🧪 **Fertilisation :**\n" +
                        "   • N : 60-100 kg/ha (ne pas excéder)\n" +
                        "   • P : 40-60 kg/ha\n" +
                        "   • K : 80-120 kg/ha\n" +
                        "   • Bore B : oligo-élément clé\n\n" +
                        "🦠 **Maladies :**\n" +
                        "   • Mildiou → variétés résistantes\n" +
                        "   • Sclérotinia → rotation 5 ans minimum\n" +
                        "   • Phomopsis → conditions humides\n\n" +
                        "💡 **Conseil :** La rotation est essentielle — ne pas revenir avant 5 ans.",
                "culture", 1.0);

        // ── ORGE ────────────────────────────────────────────────────
        add("orge", arr("orge", "hordeum"),
                arr("bière", "malt", "céréale hiver", "céréale printemps"),
                "🌾 **CULTURE DE L'ORGE**\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Orge d'hiver : Semis Oct-Nov, Récolte Juin-Juil\n" +
                        "   • Orge de printemps : Semis Fév-Mar, Récolte Juil-Août\n\n" +
                        "🌡️ **Conditions :**\n" +
                        "   • Plus précoce que le blé\n" +
                        "   • Tolérant à la sécheresse\n" +
                        "   • pH : 6.0-8.0\n\n" +
                        "📊 **Rendements :**\n" +
                        "   • 60-90 q/ha selon variété\n\n" +
                        "🧪 **Fertilisation :**\n" +
                        "   • N : 100-150 kg/ha\n" +
                        "   • P : 50-70 kg/ha\n" +
                        "   • K : 60-90 kg/ha\n\n" +
                        "💡 **Usage :** Brasserie (orge 2 rangs), alimentation animale (6 rangs).",
                "culture", 0.9);

        // ── SORGHO ──────────────────────────────────────────────────
        add("sorgho", arr("sorgho", "sorghum"),
                arr("fourrage", "grain sorgho", "mil"),
                "🌾 **CULTURE DU SORGHO**\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Semis : Mai-Juin (sol >15°C)\n" +
                        "   • Cycle : 145-165 jours\n" +
                        "   • Récolte fourrage : Août-Sep\n" +
                        "   • Récolte grain : Sep-Oct\n\n" +
                        "🌡️ **Avantages :**\n" +
                        "   • Très résistant à la sécheresse\n" +
                        "   • Excellent en zones chaudes et sèches\n" +
                        "   • Rendement fourrage : 10-20 t MS/ha\n\n" +
                        "🧪 **Fertilisation :**\n" +
                        "   • N : 150-200 kg/ha\n" +
                        "   • Fumure organique : 30-50 t/ha recommandé\n\n" +
                        "💡 **Association possible :** Relay-cropping avec orge d'hiver.",
                "culture", 0.9);

        // ── POMME DE TERRE ──────────────────────────────────────────
        add("pomme-de-terre", arr("pomme de terre", "patate", "potato"),
                arr("tubercule", "solanum tuberosum"),
                "🥔 **CULTURE DE LA POMME DE TERRE**\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Plantation : Mar-Avr\n" +
                        "   • Buttage : 3-4 sem après levée\n" +
                        "   • Récolte : Juil-Oct selon variété\n\n" +
                        "🌡️ **Conditions :**\n" +
                        "   • Température idéale : 15-18°C\n" +
                        "   • Besoin en eau : 500-700 mm\n" +
                        "   • Sol léger et meuble\n\n" +
                        "📊 **Rendements :**\n" +
                        "   • 30-60 t/ha selon variété et irrigation\n\n" +
                        "🧪 **Fertilisation :**\n" +
                        "   • N : 150-180 kg/ha\n" +
                        "   • P : 80-120 kg/ha\n" +
                        "   • K : 200-250 kg/ha (culture très exigeante en K)\n\n" +
                        "🦠 **Maladies majeures :**\n" +
                        "   • Mildiou (Phytophthora infestans) → surveillance bi-hebdomadaire\n" +
                        "   • Gale commune → pH sol <6.5\n" +
                        "   • Rhizoctone brun → rotation\n\n" +
                        "🐛 **Ravageurs :**\n" +
                        "   • Doryphore → très important, surveiller dès mai\n" +
                        "   • Pucerons → vecteurs de virus\n\n" +
                        "💡 **Conseil :** Ne replantez pas 3-4 ans au même endroit.",
                "culture", 1.0);

        // ── TOMATE ──────────────────────────────────────────────────
        add("tomate", arr("tomate", "tomato"),
                arr("solanum lycopersicum", "plants tomates"),
                "🍅 **CULTURE DE LA TOMATE**\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Semis sous abri : Fév-Mar\n" +
                        "   • Transplantation : Avr-Mai (après gelées)\n" +
                        "   • Récolte : Juil-Oct\n\n" +
                        "🌡️ **Conditions :**\n" +
                        "   • Température : 20-25°C jour, 15-18°C nuit\n" +
                        "   • Besoin en eau : 400-600 mm (régulier)\n" +
                        "   • pH : 6.0-6.8\n\n" +
                        "🧪 **Fertilisation :**\n" +
                        "   • N : 150-200 kg/ha\n" +
                        "   • P : 80-100 kg/ha\n" +
                        "   • K : 200-300 kg/ha (fruits)\n" +
                        "   • Ca et Mg importants contre nécrose apicale\n\n" +
                        "🦠 **Maladies fréquentes :**\n" +
                        "   • Mildiou → arroser au pied, aérer\n" +
                        "   • Botrytis → ventilation, coupes propres\n" +
                        "   • Alternariose → taches œil de bœuf\n" +
                        "   • Nécrose apicale → carence calcium\n\n" +
                        "💡 **Tuteurage** indispensable, espacer 80 cm à 1 m.",
                "culture", 1.0);

        // ── MILDIOU ─────────────────────────────────────────────────
        add("mildiou", arr("mildiou", "phytophthora"),
                arr("moisissure", "taches jaunes", "duvet blanc", "cryptogamique"),
                "🦠 **MILDIOU**\n\n" +
                        "🔍 **Symptômes caractéristiques :**\n" +
                        "   • Taches jaunâtres/huileuses sur le dessus des feuilles\n" +
                        "   • Feutrage blanc-grisâtre au revers par temps humide\n" +
                        "   • Brunissement et desséchement rapide\n" +
                        "   • Sur fruits : lésions brunes, pourriture\n\n" +
                        "🌡️ **Conditions favorables :**\n" +
                        "   • Humidité >90% pendant 24-48h\n" +
                        "   • Température 12-20°C\n" +
                        "   • Après pluies et rosées\n\n" +
                        "🌱 **Cultures sensibles :**\n" +
                        "   • Vigne, tomate, pomme de terre\n" +
                        "   • Courges, concombre, laitue\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Préventif : Bouillie bordelaise (cuivre)\n" +
                        "   • Curatif : Fongicides systémiques (métalaxyl)\n" +
                        "   • Bio : Décoction de prêle, purin d'ortie\n" +
                        "   • Fréquence : tous les 10-15 jours en période à risque\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Arroser AU PIED (jamais sur le feuillage)\n" +
                        "   • Espacer les plants (80 cm à 1 m)\n" +
                        "   • Surveiller 2x/semaine pomme de terre et tomate\n" +
                        "   • Rotation 3-4 ans\n" +
                        "   • Variétés résistantes\n\n" +
                        "⚠️ **Urgence :** Le mildiou peut détruire une culture en quelques jours !",
                "maladie", 1.0);

        // ── OÏDIUM ──────────────────────────────────────────────────
        add("oidium", arr("oïdium", "oidium", "blanc"),
                arr("poudre blanche", "erysiphe", "maladie blanche"),
                "⚪ **OÏDIUM (Maladie du Blanc)**\n\n" +
                        "🔍 **Symptômes :**\n" +
                        "   • Feutrage blanc farineux sur feuilles et tiges\n" +
                        "   • Déformation des jeunes pousses\n" +
                        "   • Jaunissement puis dessèchement\n" +
                        "   • Sur fruits : craquelures, taches grises\n\n" +
                        "🌡️ **Conditions favorables :**\n" +
                        "   • Temps chaud et sec (≠ mildiou)\n" +
                        "   • 20-25°C, humidité relative 50-70%\n" +
                        "   • Excès d'azote favorise l'oïdium\n\n" +
                        "🌱 **Cultures sensibles :**\n" +
                        "   • Vigne, cucurbitacées, céréales\n" +
                        "   • Rosiers, fraisiers, courgettes\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Soufre (efficace, bon marché)\n" +
                        "   • Bicarbonate de potassium\n" +
                        "   • Huile de neem\n" +
                        "   • Strobilurines, DMI (céréales)\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Bonne aération entre les plants\n" +
                        "   • Éviter l'excès d'azote\n" +
                        "   • Variétés résistantes\n" +
                        "   • Traitements préventifs dès débourrement",
                "maladie", 1.0);

        // ── ROUILLE ─────────────────────────────────────────────────
        add("rouille", arr("rouille", "rouille brune", "rouille jaune"),
                arr("pustules", "puccinia", "orange", "rouille des céréales"),
                "🟤 **ROUILLES DES CÉRÉALES**\n\n" +
                        "🔍 **Symptômes :**\n" +
                        "   • Pustules orangées (rouille brune) sur feuilles\n" +
                        "   • Pustules jaunes en lignes (rouille jaune)\n" +
                        "   • Desséchement des feuilles, chute de rendement\n\n" +
                        "🌡️ **Conditions :**\n" +
                        "   • Rouille brune : 15-25°C, rosées nocturnes\n" +
                        "   • Rouille jaune : 10-15°C (plus froide)\n\n" +
                        "🌱 **Cultures touchées :**\n" +
                        "   • Blé (principale), orge, seigle\n" +
                        "   • Alliacées (ail, poireau) pour rouille allium\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Fongicides triazoles (propiconazole, tébuconazole)\n" +
                        "   • Strobilurines (azoxystrobine)\n" +
                        "   • Intervenir dès les premiers symptômes\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Variétés résistantes (choix primordial)\n" +
                        "   • Date de semis adaptée\n" +
                        "   • Éliminer résidus de culture\n" +
                        "   • Rotation longue\n\n" +
                        "⚠️ **Impact :** Jusqu'à -50% de rendement sans traitement.",
                "maladie", 1.0);

        // ── BOTRYTIS ────────────────────────────────────────────────
        add("botrytis", arr("botrytis", "pourriture grise"),
                arr("moisissure grise", "botrytis cinerea", "grey mould"),
                "🩶 **BOTRYTIS (Pourriture Grise)**\n\n" +
                        "🔍 **Symptômes :**\n" +
                        "   • Moisissure grise caractéristique\n" +
                        "   • Tissus mous et pourrissants\n" +
                        "   • Flétrissement rapide\n" +
                        "   • Spores grises en nuage au toucher\n\n" +
                        "🌡️ **Conditions :**\n" +
                        "   • Temps humide, froid-frais (15-20°C)\n" +
                        "   • Blessures (taille, grêle) favorisent l'entrée\n" +
                        "   • Plantes affaiblies\n\n" +
                        "🌱 **Cultures touchées :**\n" +
                        "   • Tomate, fraisier, vigne, pois, haricot\n" +
                        "   • Très polyphage (>220 espèces hôtes)\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Pas de guérison complète possible\n" +
                        "   • Fongicides : iprodione, boscalide\n" +
                        "   • Bio : Trichoderma, Bacillus subtilis\n" +
                        "   • Supprimer et détruire les parties atteintes\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Ventilation maximale (serre, tunnel)\n" +
                        "   • Éviter blessures sur les plantes\n" +
                        "   • Tailler le matin (plaies sèchent vite)\n" +
                        "   • Espacer les plants\n" +
                        "   • Ne pas laisser résidus végétaux",
                "maladie", 1.0);

        // ── ALTERNARIOSE ────────────────────────────────────────────
        add("alternariose", arr("alternariose", "alternaria"),
                arr("taches noires", "oeil de boeuf", "cercles concentriques"),
                "⚫ **ALTERNARIOSE**\n\n" +
                        "🔍 **Symptômes :**\n" +
                        "   • Taches circulaires à cercles concentriques\n" +
                        "   • Brun-noir sur feuilles et fruits\n" +
                        "   • Pourriture sèche des fruits\n\n" +
                        "🌱 **Cultures touchées :**\n" +
                        "   • Tomate (taches œil de bœuf), pomme de terre\n" +
                        "   • Carotte, chou, colza, oignon\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Fongicides : mancozèbe, iprodione\n" +
                        "   • Bio : bouillie bordelaise\n" +
                        "   • Supprimer parties atteintes\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Rotation des cultures\n" +
                        "   • Éviter stress hydrique\n" +
                        "   • Arrosage au pied",
                "maladie", 0.9);

        // ── SCLÉROTINIA ─────────────────────────────────────────────
        add("sclerotinia", arr("sclérotinia", "sclerotinia", "sclerotiniose"),
                arr("pourriture blanche", "moisissure blanche", "tige"),
                "🤍 **SCLÉROTINIA**\n\n" +
                        "🔍 **Symptômes :**\n" +
                        "   • Moisissure blanche cotonneuse sur tige\n" +
                        "   • Petits sclérotes noirs dans la tige\n" +
                        "   • Flétrissement brutal\n\n" +
                        "🌱 **Cultures touchées :**\n" +
                        "   • Colza (principale menace), tournesol\n" +
                        "   • Salade, haricot, pois, carotte\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Colza : fongicide au début floraison (boscalide)\n" +
                        "   • Rotation longue (5+ ans)\n" +
                        "   • Destruire les résidus infectés\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Variétés résistantes\n" +
                        "   • Densité de semis adaptée\n" +
                        "   • Aération des cultures\n\n" +
                        "⚠️ **Impact colza :** Pertes pouvant dépasser 30% du rendement.",
                "maladie", 0.9);

        // ── FUSARIOSE ───────────────────────────────────────────────
        add("fusariose", arr("fusariose", "fusarium"),
                arr("mycotoxines", "don", "echaudage", "fusariose epis"),
                "🟡 **FUSARIOSE**\n\n" +
                        "🔍 **Symptômes :**\n" +
                        "   • Épis partiellement blancs (blé)\n" +
                        "   • Base tige rose-orangée\n" +
                        "   • Grains ridés, rose-orangé\n\n" +
                        "⚠️ **Danger majeur :** Production de mycotoxines (DON)\n" +
                        "   → Risque santé humaine et animale !\n\n" +
                        "🌡️ **Conditions favorables :**\n" +
                        "   • Floraison humide et douce\n" +
                        "   • Pluies autour de l'épiaison\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Fongicide à l'épiaison (si risque élevé)\n" +
                        "   • Tebuconazole, metconazole\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Variétés peu sensibles\n" +
                        "   • Labour pour enfouir résidus\n" +
                        "   • Ne pas semer blé après maïs\n" +
                        "   • Rotation diversifiée",
                "maladie", 0.9);

        // ── PUCERONS ────────────────────────────────────────────────
        add("puceron", arr("puceron", "pucerons", "aphid"),
                arr("colonie verte", "mielat", "insecte", "feuilles recroquevillées"),
                "🐛 **PUCERONS**\n\n" +
                        "🔍 **Symptômes :**\n" +
                        "   • Colonies d'insectes verts/noirs sur pousses\n" +
                        "   • Feuilles crispées, enroulées\n" +
                        "   • Miellat collant → fumagine noire\n" +
                        "   • Transmission de virus\n\n" +
                        "🌱 **Cultures touchées :**\n" +
                        "   • Presque toutes les cultures\n" +
                        "   • Puceron vert des céréales (épis au printemps)\n" +
                        "   • Pucerons noirs (betterave, fève)\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Favoriser les auxiliaires (coccinelles, chrysopes)\n" +
                        "   • Savon noir dilué (potager)\n" +
                        "   • Pyréthrines naturelles\n" +
                        "   • Insecticides chimiques si seuil dépassé\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Surveiller régulièrement\n" +
                        "   • Éviter excès d'azote\n" +
                        "   • Haies abritant les prédateurs naturels\n" +
                        "   • ⚠️ Les insecticides contre pucerons n'empêchent pas la transmission virale",
                "ravageur", 0.9);

        // ── DORYPHORE ───────────────────────────────────────────────
        add("doryphore", arr("doryphore", "colorado"),
                arr("rayures jaune noir", "larve rouge", "pomme de terre insecte"),
                "🐞 **DORYPHORE**\n\n" +
                        "🔍 **Identification :**\n" +
                        "   • Coléoptère rayé jaune et noir\n" +
                        "   • Larves rouges-orangées très voraces\n" +
                        "   • Défoliation totale possible\n\n" +
                        "🌱 **Culture touchée :**\n" +
                        "   • Pomme de terre principalement\n" +
                        "   • Aubergine, tomate\n\n" +
                        "💊 **Traitement :**\n" +
                        "   • Surveiller dès mai, 2x/semaine\n" +
                        "   • Ramassage manuel (petites surfaces)\n" +
                        "   • Bacillus thuringiensis (bio, sur jeunes larves)\n" +
                        "   • Spinosad, azadirachtine\n" +
                        "   • Insecticides chimiques si nécessaire\n\n" +
                        "✅ **Prévention :**\n" +
                        "   • Rotation des cultures\n" +
                        "   • Paillage répulsif\n" +
                        "   • Haies refuges pour auxiliaires",
                "ravageur", 0.9);

        // ── ENGRAIS NPK ──────────────────────────────────────────────
        add("engrais", arr("engrais", "fertilisant", "fertilisation", "npk", "fumure"),
                arr("nutriment", "apport", "dose"),
                "🧪 **FERTILISATION AGRICOLE**\n\n" +
                        "🌱 **Macroéléments principaux (NPK) :**\n" +
                        "   • **N — Azote :** Croissance végétative, couleur verte\n" +
                        "     → Carence : jaunissement général, croissance ralentie\n" +
                        "   • **P — Phosphore :** Développement racinaire, floraison\n" +
                        "     → Carence : teintes pourpres, racines faibles\n" +
                        "   • **K — Potassium :** Qualité des fruits, résistance\n" +
                        "     → Carence : bord des feuilles brûlé, fruits petits\n\n" +
                        "🌿 **Macroéléments secondaires :**\n" +
                        "   • **Ca (Calcium):** Structure cellulaire, nécrose apicale\n" +
                        "   • **Mg (Magnésium):** Chlorophylle\n" +
                        "   • **S (Soufre):** Colza, oignons, crucifères\n\n" +
                        "🔬 **Oligo-éléments essentiels :**\n" +
                        "   • Fer (Fe), Zinc (Zn), Manganèse (Mn), Bore (B), Cuivre (Cu)\n\n" +
                        "📊 **Doses indicatives par culture :**\n" +
                        "   Culture      | N (kg/ha) | P (kg/ha) | K (kg/ha)\n" +
                        "   -------------|-----------|-----------|----------\n" +
                        "   Blé          | 150-200   | 60-80     | 80-100\n" +
                        "   Maïs         | 200-250   | 80-100    | 100-150\n" +
                        "   Colza        | 180-220   | 60-80     | 80-120\n" +
                        "   Pomme terre  | 150-180   | 80-120    | 200-250\n" +
                        "   Tournesol    | 60-100    | 40-60     | 80-120\n\n" +
                        "💡 **Règle d'or :** Analyser le sol tous les 3-5 ans !",
                "fertilisation", 1.0);

        // ── AZOTE ────────────────────────────────────────────────────
        add("azote", arr("azote", "nitrate", "ammonium", "urée"),
                arr("n", "engrais azoté", "azote minéral"),
                "🌱 **L'AZOTE (N)**\n\n" +
                        "📈 **Rôle :**\n" +
                        "   • Croissance végétative intense\n" +
                        "   • Synthèse des protéines et chlorophylle\n" +
                        "   • Couleur verte des feuilles\n\n" +
                        "⚠️ **Carence :**\n" +
                        "   • Jaunissement des feuilles âgées d'abord\n" +
                        "   • Croissance très ralentie\n" +
                        "   • Faible rendement\n\n" +
                        "🚫 **Excès :**\n" +
                        "   • Verse des céréales\n" +
                        "   • Sensibilité aux maladies (oïdium, botrytis)\n" +
                        "   • Pollution des nappes (nitrates)\n\n" +
                        "📊 **Apports recommandés :**\n" +
                        "   • Blé : 150-200 kg/ha (3 fractionnements)\n" +
                        "   • Maïs : 200-250 kg/ha (méthode CAU)\n" +
                        "   • Colza : 180-220 kg/ha\n" +
                        "   • Toujours fractionner !\n\n" +
                        "💡 **Types d'engrais azotés :**\n" +
                        "   • Urée (46% N) — volatilisation risque\n" +
                        "   • Ammonitrate (33% N) — le plus répandu\n" +
                        "   • Sulfate d'ammonium (21% N + S)",
                "fertilisation", 0.9);

        // ── PHOSPHORE ────────────────────────────────────────────────
        add("phosphore", arr("phosphore", "phosphate", "superphosphate"),
                arr("p2o5", "engrais phosphaté"),
                "🌸 **LE PHOSPHORE (P)**\n\n" +
                        "📈 **Rôle :**\n" +
                        "   • Développement racinaire\n" +
                        "   • Floraison et fructification\n" +
                        "   • Maturation et qualité des grains\n" +
                        "   • Résistance aux maladies\n\n" +
                        "⚠️ **Carence :**\n" +
                        "   • Teintes pourpres/violettes des feuilles\n" +
                        "   • Racines faibles et peu développées\n" +
                        "   • Floraison retardée\n\n" +
                        "📊 **Apports :**\n" +
                        "   • Général : 60-100 kg P2O5/ha\n" +
                        "   • Apporter de préférence au semis\n" +
                        "   • Peu mobile dans le sol → apport localisé efficace\n\n" +
                        "💡 **Forme :** Superphosphate simple (18%), triple (46%), DAP (46%P).",
                "fertilisation", 0.9);

        // ── POTASSIUM ────────────────────────────────────────────────
        add("potassium", arr("potassium", "potasse", "potassique"),
                arr("k2o", "chlorure potassium", "sulfate potassium"),
                "🍎 **LE POTASSIUM (K)**\n\n" +
                        "📈 **Rôle :**\n" +
                        "   • Qualité et goût des fruits/grains\n" +
                        "   • Résistance au froid et à la sécheresse\n" +
                        "   • Transport des sucres\n" +
                        "   • Régulation hydrique de la plante\n\n" +
                        "⚠️ **Carence :**\n" +
                        "   • Bords des feuilles brûlés (nécrose marginale)\n" +
                        "   • Fruits et grains petits\n" +
                        "   • Sensibilité accrue au froid\n\n" +
                        "📊 **Apports :**\n" +
                        "   • Pomme de terre : 200-250 kg K2O/ha\n" +
                        "   • Maïs : 100-150 kg K2O/ha\n" +
                        "   • Blé : 80-100 kg K2O/ha\n\n" +
                        "💡 **Sols légers :** Augmenter les doses (lessivage plus rapide).",
                "fertilisation", 0.9);

        // ── IRRIGATION ───────────────────────────────────────────────
        add("irrigation", arr("irrigation", "arrosage", "eau"),
                arr("aspersion", "goutte à goutte", "pivot", "micro-aspersion"),
                "💧 **IRRIGATION AGRICOLE**\n\n" +
                        "📏 **Méthodes principales :**\n" +
                        "   • **Goutte-à-goutte :** Économie 30-50% eau, pas de maladies foliaires\n" +
                        "   • **Aspersion :** Uniforme, bonne pour levée, risque maladies\n" +
                        "   • **Gravitaire :** Simple et peu coûteux\n" +
                        "   • **Pivot :** Grande surface, automatisé\n\n" +
                        "⏰ **Moments optimaux :**\n" +
                        "   • Tôt le matin (5h-8h) — idéal\n" +
                        "   • Fin de journée (18h-21h)\n" +
                        "   • Éviter la mi-journée (évaporation 30-50%)\n\n" +
                        "📊 **Besoins par culture (mm/cycle) :**\n" +
                        "   • Blé d'hiver : 400-600 mm\n" +
                        "   • Maïs : 500-700 mm\n" +
                        "   • Tournesol : 300-450 mm\n" +
                        "   • Pomme de terre : 500-700 mm\n" +
                        "   • Légumes maraîchers : 300-500 mm\n\n" +
                        "✅ **Économies d'eau :**\n" +
                        "   • Paillage → -30% évaporation\n" +
                        "   • Irrigation localisée\n" +
                        "   • Récupération eau de pluie\n" +
                        "   • Capteurs humidité sol\n\n" +
                        "💡 **Règle :** Irriguer en fonction des besoins réels, pas d'un calendrier fixe.",
                "irrigation", 1.0);

        // ── GOUTTE À GOUTTE ──────────────────────────────────────────
        add("goutte-a-goutte", arr("goutte à goutte", "drip", "goutteur"),
                arr("microirrigation", "tuyau percé", "goteo"),
                "💧 **GOUTTE-À-GOUTTE**\n\n" +
                        "✅ **Avantages :**\n" +
                        "   • Économie d'eau : 30-50% vs aspersion\n" +
                        "   • Pas de maladies foliaires (feuilles sèches)\n" +
                        "   • Fertigation possible (engrais dans l'eau)\n" +
                        "   • Moins de mauvaises herbes inter-rangs\n" +
                        "   • Rendement souvent supérieur\n\n" +
                        "⚙️ **Installation :**\n" +
                        "   • Goutteurs tous les 30-50 cm\n" +
                        "   • Débit : 1-4 L/heure selon culture\n" +
                        "   • Pression : 1-2 bars\n" +
                        "   • Filtration INDISPENSABLE\n\n" +
                        "🌱 **Idéal pour :**\n" +
                        "   • Maraîchage, arboriculture, vigne\n" +
                        "   • Serres et tunnels\n" +
                        "   • Régions à eau rare\n\n" +
                        "💡 **Entretien :** Rinçage des goutteurs, contrôle pression régulier.",
                "irrigation", 0.9);

        // ── SOL ──────────────────────────────────────────────────────
        add("sol", arr("sol", "terre", "terrain", "ph", "sol agricole"),
                arr("argile", "limon", "sable", "humus", "matieres organiques"),
                "🌍 **ANALYSE ET TYPES DE SOL**\n\n" +
                        "🏜️ **Sol sableux :**\n" +
                        "   • Léger, se réchauffe vite\n" +
                        "   • Pauvre en nutriments, drainage rapide\n" +
                        "   • Irriguer fréquemment, fertiliser en fractionnant\n\n" +
                        "🌿 **Sol limoneux (idéal) :**\n" +
                        "   • Excellent équilibre eau/air\n" +
                        "   • Bonne rétention nutriments\n" +
                        "   • La plupart des cultures s'y épanouissent\n\n" +
                        "🧱 **Sol argileux :**\n" +
                        "   • Lourd, se réchauffe lentement\n" +
                        "   • Riche en nutriments mais drainage lent\n" +
                        "   • Risque de compaction, asphyxie racinaire\n\n" +
                        "⚖️ **pH et cultures :**\n" +
                        "   • pH 5.5-6.5 : pomme de terre, petits fruits\n" +
                        "   • pH 6.0-7.0 : légumes, maïs, tournesol\n" +
                        "   • pH 6.5-7.5 : blé, orge, colza, betterave\n" +
                        "   • Corriger avec : chaux (si acide), soufre (si basique)\n\n" +
                        "🧪 **Analyse de sol :**\n" +
                        "   • À faire tous les 3-5 ans\n" +
                        "   • Prélever à 20-30 cm, 20+ points/parcelle\n" +
                        "   • Analyses : pH, MO, P, K, Mg, CEC\n\n" +
                        "💡 **Matière organique :** Viser 2-3% (compost, couverts végétaux, fumier).",
                "sol", 1.0);

        // ── LABOUR ───────────────────────────────────────────────────
        add("labour", arr("labour", "labourer", "travail du sol"),
                arr("charrue", "semis direct", "strip till", "déchaumage"),
                "🚜 **TRAVAIL DU SOL**\n\n" +
                        "🔄 **Labour profond (20-30 cm) :**\n" +
                        "   • Enfouit résidus et adventices\n" +
                        "   • Ameublit et aère le sol\n" +
                        "   • Fait à l'automne de préférence\n" +
                        "   • Consommateur de carburant\n\n" +
                        "🌱 **Techniques de conservation :**\n" +
                        "   • **Semis direct :** Minimum de perturbation\n" +
                        "   • **Strip-till :** Travail sur la ligne de semis seulement\n" +
                        "   • **Travail superficiel :** 5-10 cm\n\n" +
                        "✅ **Avantages du non-labour :**\n" +
                        "   • Protège la vie du sol (vers de terre ×10)\n" +
                        "   • Économie carburant : 40-60 L/ha\n" +
                        "   • Moins d'érosion\n" +
                        "   • Séquestration du carbone\n\n" +
                        "⚠️ **Inconvénients :**\n" +
                        "   • Gestion adventices plus délicate\n" +
                        "   • Risque maladies telluriques\n" +
                        "   • Adaptation de l'outil semoir nécessaire\n\n" +
                        "💡 **Tendance :** Passage progressif vers techniques simplifiées.",
                "sol", 0.9);

        // ── ROTATION ─────────────────────────────────────────────────
        add("rotation", arr("rotation", "rotation des cultures", "assolement"),
                arr("succession cultures", "précédent cultural"),
                "🔄 **ROTATION DES CULTURES**\n\n" +
                        "✅ **Pourquoi tourner ?**\n" +
                        "   • Rupture des cycles maladies/ravageurs\n" +
                        "   • Gestion des adventices\n" +
                        "   • Maintien fertilité du sol\n" +
                        "   • Réduction des intrants\n\n" +
                        "📊 **Rotations classiques :**\n" +
                        "   • **Classique 3 ans :** Colza → Blé → Orge\n" +
                        "   • **Intensive 4 ans :** Colza → Blé → Pois → Blé\n" +
                        "   • **Polyculture :** Céréale → Légumineuse → Oléagineux\n\n" +
                        "🚫 **À éviter absolument :**\n" +
                        "   • Blé après blé (fusariose, piétin)\n" +
                        "   • Colza après colza (sclérotinia, altises)\n" +
                        "   • Tournesol <5 ans sur la même parcelle\n\n" +
                        "💡 **Bonus colza :** +10% rendement blé après colza vs blé/blé.",
                "sol", 0.9);

        // ── TRACTEUR ─────────────────────────────────────────────────
        add("tracteur", arr("tracteur", "tracteurs", "engin agricole"),
                arr("cv", "puissance", "attelage", "pto", "prise de force"),
                "🚜 **TRACTEUR AGRICOLE**\n\n" +
                        "🔧 **Types par puissance :**\n" +
                        "   • Compact : 20-50 CV (maraîchage, vigne)\n" +
                        "   • Standard : 50-150 CV (polyvalent)\n" +
                        "   • Puissant : 150-300 CV (grandes cultures)\n" +
                        "   • Articulé : >300 CV (exploitation intensive)\n\n" +
                        "📋 **Utilisations :**\n" +
                        "   • Labour, semis, traitement, récolte\n" +
                        "   • Transport, manutention, traction\n\n" +
                        "⚙️ **Entretien indispensable :**\n" +
                        "   • Vidange huile moteur : toutes les 250h\n" +
                        "   • Filtre huile/air/gasoil : tous les 500h\n" +
                        "   • Révision complète : annuelle\n" +
                        "   • Vérifier pression pneus : avant chaque utilisation\n\n" +
                        "💡 **Économie :** Un tracteur bien réglé consomme 20-30% de carburant en moins.",
                "equipement", 1.0);

        // ── SEMOIR ───────────────────────────────────────────────────
        add("semoir", arr("semoir", "semeurs", "semer"),
                arr("densité semis", "profondeur semis", "inter-rang"),
                "🌱 **SEMOIR AGRICOLE**\n\n" +
                        "📏 **Types :**\n" +
                        "   • Semoir à céréales (dents ou disques)\n" +
                        "   • Semoir monograine (précision)\n" +
                        "   • Semoir pneumatique (haute densité)\n" +
                        "   • Semoir semis direct (sans travail sol)\n\n" +
                        "⚙️ **Réglages clés :**\n" +
                        "   • Profondeur : 2-5 cm selon culture\n" +
                        "   • Inter-rang : 12-50 cm selon culture\n" +
                        "   • Densité de semis :\n" +
                        "     - Blé : 180-220 kg/ha (200-350 graines/m²)\n" +
                        "     - Maïs : 20-25 kg/ha (90 000 graines/ha)\n" +
                        "     - Colza : 40-50 graines/m² (25-35 plantes/m²)\n" +
                        "     - Tournesol : 5-8 kg/ha (60 000 graines/ha)\n\n" +
                        "💡 **Vérification :** Testez toujours le débit sur 10-20 m avant de semer.",
                "equipement", 0.9);

        // ── MOISSONNEUSE ─────────────────────────────────────────────
        add("moissonneuse", arr("moissonneuse", "moissonneuse batteuse", "moisson"),
                arr("récolte céréales", "batteur", "sécoueurs", "trémie"),
                "🌾 **MOISSONNEUSE-BATTEUSE**\n\n" +
                        "🔧 **Composants principaux :**\n" +
                        "   • Table de coupe (rabatteur, vis)\n" +
                        "   • Batteur (cylindre, contre-batteur)\n" +
                        "   • Séparateurs et secoueurs\n" +
                        "   • Nettoyeur (ventilateur + grilles)\n" +
                        "   • Trémie de stockage\n\n" +
                        "📈 **Performances :**\n" +
                        "   • Débit : 10-30 t/heure\n" +
                        "   • Perte admissible : <3%\n\n" +
                        "⚙️ **Réglages selon culture :**\n" +
                        "   • Blé : batteur 900-1200 tr/min\n" +
                        "   • Maïs : batteur 400-600 tr/min (doux)\n" +
                        "   • Colza : batteur 600-800 tr/min, table basse\n\n" +
                        "💡 **Conseil :** Récolter blé à 13-15% humidité pour éviter séchage.",
                "equipement", 0.9);

        // ── PULVÉRISATEUR ────────────────────────────────────────────
        add("pulverisateur", arr("pulvérisateur", "pulverisateur", "pulvé", "traitement phyto"),
                arr("bouillie", "rampe", "buse", "produit phytosanitaire"),
                "💦 **PULVÉRISATEUR**\n\n" +
                        "📏 **Types :**\n" +
                        "   • Porté : 600-1500 L\n" +
                        "   • Traîné : 2000-6000 L\n" +
                        "   • Automoteur : 3000-8000 L\n\n" +
                        "⚙️ **Réglages :**\n" +
                        "   • Pression : 2-5 bars selon buse\n" +
                        "   • Volume : 100-300 L/ha\n" +
                        "   • Vitesse : 6-12 km/h\n" +
                        "   • Hauteur rampe : 50 cm au-dessus végétation\n\n" +
                        "⚠️ **Sécurité OBLIGATOIRE :**\n" +
                        "   • Combinaison, gants, masque, lunettes\n" +
                        "   • Ne traiter que sous 15-20 km/h de vent\n" +
                        "   • Respecter zones non-traitées (ZNT)\n" +
                        "   • Rincer le matériel après usage\n\n" +
                        "💡 **Calibrage :** Vérifier le débit de chaque buse (écart <5%).",
                "equipement", 0.9);

        // ── PANNE ────────────────────────────────────────────────────
        add("panne", arr("panne", "pannes", "réparation", "cassé"),
                arr("dépannage", "technicien", "arrêt machine"),
                "🔧 **PROCÉDURE EN CAS DE PANNE**\n\n" +
                        "1️⃣ **Signaler** la panne dans l'application\n" +
                        "2️⃣ **Sécuriser** la zone et le matériel\n" +
                        "3️⃣ **Photographier** l'anomalie observée\n" +
                        "4️⃣ **Relever** le code d'erreur si disponible\n\n" +
                        "🔍 **Diagnostics courants :**\n" +
                        "   • Tracteur ne démarre pas → batterie, carburant, filtre\n" +
                        "   • Surchauffe → niveau eau, ventilateur, thermostat\n" +
                        "   • Vibrations anormales → courroies, roulements\n" +
                        "   • Pression hydraulique faible → huile, pompe, filtre\n\n" +
                        "📞 **Contacts d'urgence :**\n" +
                        "   • Technicien terrain : 12345678\n" +
                        "   • Support AgriMans : support@agrimans.com\n\n" +
                        "⏱️ **Délai d'intervention :** 24-48h (urgence : 4h)",
                "equipement", 1.0);

        // ── MÉTÉO ────────────────────────────────────────────────────
        add("meteo", arr("météo", "meteo", "temps", "climat"),
                arr("température", "pluie", "vent", "prévision"),
                "☀️ **MÉTÉO ET AGRICULTURE**\n\n" +
                        "🌡️ **Températures critiques :**\n" +
                        "   • Gel : <0°C (gommez vos dates de semis)\n" +
                        "   • Canicule : >35°C (stress hydrique majeur)\n" +
                        "   • Échaudage céréales : >30°C pendant floraison\n\n" +
                        "💧 **Pluviométrie et irrigation :**\n" +
                        "   • Surveiller l'ETP (évapotranspiration potentielle)\n" +
                        "   • Irriguer quand pluie <5 mm/semaine en été\n\n" +
                        "⚠️ **Risques climatiques :**\n" +
                        "   • Gel printanier : protéger floraisons fruitières\n" +
                        "   • Grêle : souscrivez une assurance récolte\n" +
                        "   • Sécheresse : couverts végétaux, paillage\n\n" +
                        "📱 **Applications recommandées :**\n" +
                        "   • Météo France Agri\n" +
                        "   • WeatherPro\n" +
                        "   • AccuWeather\n\n" +
                        "💡 **Conseil :** Consultez les bulletins de santé du végétal (BSV) régulièrement.",
                "meteo", 1.0);

        // ── GEL ──────────────────────────────────────────────────────
        add("gel", arr("gel", "gelée", "givre", "froid"),
                arr("antigivre", "protection gel", "voile hivernage"),
                "❄️ **PROTECTION CONTRE LE GEL**\n\n" +
                        "🌡️ **Types de gel :**\n" +
                        "   • Gelée blanche (rayonnement, nuits calmes)\n" +
                        "   • Gelée noire (advection, vent froid)\n\n" +
                        "🛡️ **Méthodes de protection :**\n" +
                        "   • **Voiles d'hivernage :** +2-4°C sous voile\n" +
                        "   • **Aspersion antigel :** Eau gèle et libère chaleur latente\n" +
                        "   • **Chaufferettes/bougies :** Arboriculture\n" +
                        "   • **Brassage d'air :** Hélicoptères, éoliennes\n" +
                        "   • **Paillage :** Protection des racines\n\n" +
                        "🌱 **Stades sensibles :**\n" +
                        "   • Floraison (fruits à noyau, vigne) → très sensible\n" +
                        "   • Jeunes pousses printanières → sensibles\n" +
                        "   • Céréales au tallage → relativement résistantes\n\n" +
                        "💡 **Prévention :** Évitez les semis précoces en zone à risque de gel tardif.",
                "meteo", 0.9);

        // ── SÉCHERESSE ───────────────────────────────────────────────
        add("secheresse", arr("sécheresse", "secheresse", "manque eau", "stress hydrique"),
                arr("déficit pluviométrique", "aridité"),
                "🌵 **GESTION DE LA SÉCHERESSE**\n\n" +
                        "🚨 **Signes de stress hydrique :**\n" +
                        "   • Enroulement des feuilles (blé, maïs)\n" +
                        "   • Jaunissement et dessèchement\n" +
                        "   • Flétrissement diurne\n\n" +
                        "✅ **Stratégies d'adaptation :**\n" +
                        "   • Couverts végétaux → retient l'eau dans le sol\n" +
                        "   • Paillage → réduit évaporation de 30-50%\n" +
                        "   • Labour réduit → préserve humidité\n" +
                        "   • Variétés résistantes à la sécheresse\n" +
                        "   • Irrigation déficitaire raisonnée\n\n" +
                        "💧 **Cultures résistantes :**\n" +
                        "   • Tournesol, sorgho, mil\n" +
                        "   • Légumineuses (après établissement)\n" +
                        "   • Colza (printemps bénéfique)\n\n" +
                        "💡 **Règle d'or :** Un sol riche en matière organique retient 3-4x plus d'eau.",
                "meteo", 0.9);

        // ── AGROÉCOLOGIE / BIO ───────────────────────────────────────
        add("bio", arr("bio", "biologique", "agroécologie", "agriculture biologique"),
                arr("organic", "ab", "label rouge", "sans pesticides"),
                "🌿 **AGRICULTURE BIOLOGIQUE & AGROÉCOLOGIE**\n\n" +
                        "📋 **Principes fondamentaux :**\n" +
                        "   • Pas d'intrants de synthèse (engrais, pesticides)\n" +
                        "   • Rotation des cultures longue\n" +
                        "   • Maintien et amélioration de la biodiversité\n" +
                        "   • Travail du sol réduit\n\n" +
                        "🌱 **Pratiques clés :**\n" +
                        "   • **Couverts végétaux :** Légumineuses (fixation azote)\n" +
                        "   • **Compost :** 10-20 t/ha, améliore structure sol\n" +
                        "   • **Rotations :** 5-7 cultures minimum\n" +
                        "   • **Auxiliaires :** Hôtes de prédateurs naturels\n" +
                        "   • **Désherbage mécanique :** Herse étrille, binage\n\n" +
                        "🦠 **Traitements autorisés en AB :**\n" +
                        "   • Cuivre (bouillie bordelaise) — limité à 4 kg Cu/ha/an\n" +
                        "   • Soufre contre oïdium\n" +
                        "   • Pyréthrines naturelles\n" +
                        "   • Bacillus thuringiensis\n" +
                        "   • Trichoderma, Beauveria bassiana\n\n" +
                        "💡 **Conversion :** 2-3 ans avant certification, aide PAC disponible.",
                "durable", 0.9);

        // ── COMPOST ──────────────────────────────────────────────────
        add("compost", arr("compost", "fumier", "matière organique", "amendement"),
                arr("humus", "déchets organiques", "lombricompost"),
                "♻️ **COMPOST ET AMENDEMENTS ORGANIQUES**\n\n" +
                        "🌱 **Bénéfices du compost :**\n" +
                        "   • Améliore structure du sol\n" +
                        "   • Augmente la rétention d'eau\n" +
                        "   • Libère nutriments lentement\n" +
                        "   • Active la vie microbienne du sol\n\n" +
                        "📊 **Types et doses :**\n" +
                        "   • Compost mature : 5-10 t/ha/an\n" +
                        "   • Fumier bovin : 20-30 t/ha\n" +
                        "   • Fumier volaille : 3-5 t/ha (riche en N)\n" +
                        "   • Digestat : selon analyses\n\n" +
                        "🔄 **Compostage à la ferme :**\n" +
                        "   • Mélanger carbones et azotes (C/N ≈ 25-30)\n" +
                        "   • Humidité 50-60%\n" +
                        "   • Retournements réguliers\n" +
                        "   • Maturité : 3-6 mois\n\n" +
                        "💡 **Astuce :** Apporter le compost à l'automne pour intégration au sol.",
                "sol", 0.9);

        // ── RENDEMENT / DIAGNOSTIC AGRONOMIQUE ───────────────────────
        add("rendement", arr("rendement", "productivité", "récolte faible"),
                arr("quintal", "tonne hectare", "performance"),
                "📊 **OPTIMISATION DU RENDEMENT**\n\n" +
                        "🔍 **Facteurs limitants principaux :**\n" +
                        "   1. Choix variétal (20-30% du rendement)\n" +
                        "   2. Date et densité de semis\n" +
                        "   3. Fertilisation (surtout N)\n" +
                        "   4. Protection phytosanitaire\n" +
                        "   5. Irrigation\n" +
                        "   6. Travail du sol\n\n" +
                        "📈 **Rendements de référence :**\n" +
                        "   • Blé France : 75 q/ha (moyen)\n" +
                        "   • Maïs grain : 100 q/ha (moyen)\n" +
                        "   • Colza : 35 q/ha (moyen)\n" +
                        "   • Tournesol : 28 q/ha (moyen)\n\n" +
                        "💡 **Diagnostic baisse de rendement :**\n" +
                        "   • Levée irrégulière → qualité semences, sol, date\n" +
                        "   • Jaunissement → carence N ou maladie\n" +
                        "   • Verse → excès N, densité, choisir variété résistante\n" +
                        "   • Grains petits → stress hydrique floraison, K insuffisant",
                "gestion", 0.9);

        // ── COUVERT VÉGÉTAL ──────────────────────────────────────────
        add("couvert", arr("couvert végétal", "couvert", "engrais vert"),
                arr("cipan", "interculture", "légumineuse", "mélange"),
                "🌿 **COUVERTS VÉGÉTAUX**\n\n" +
                        "✅ **Bénéfices :**\n" +
                        "   • Limite lessivage des nitrates\n" +
                        "   • Fixation d'azote (légumineuses)\n" +
                        "   • Améliore structure du sol\n" +
                        "   • Limite érosion\n" +
                        "   • Brise-vent et refuge pour auxiliaires\n\n" +
                        "🌱 **Espèces populaires :**\n" +
                        "   • **Légumineuses :** Trèfle, vesce, féverole (fix. N2)\n" +
                        "   • **Crucifères :** Moutarde, radis chinois\n" +
                        "   • **Graminées :** Seigle, avoine, phacélie\n" +
                        "   • **Mélanges :** Multi-espèces pour synergie\n\n" +
                        "📅 **Calendrier :**\n" +
                        "   • Semis : Août-Sep après céréales\n" +
                        "   • Destruction : Fév-Mar (30 j avant semis suivant)\n\n" +
                        "⚠️ **CIPAN obligatoire** en zones vulnérables nitrates.",
                "durable", 0.9);
    }

    /** Méthode utilitaire pour ajouter une entrée de connaissance */
    private void add(String id, String[] keywords, String[] synonyms,
                     String response, String category, double weight) {
        knowledgeBase.add(new Knowledge(id, keywords, synonyms, response, category, weight));
    }

    private static String[] arr(String... values) { return values; }

    private void initialiserQuestionsFrequentes() {
        questionsFrequentes.addAll(Arrays.asList(
                "Quand semer le blé ?",
                "Comment traiter le mildiou ?",
                "Quelle dose d'azote pour le maïs ?",
                "Comment irriguer efficacement ?",
                "Que faire en cas de panne ?",
                "Qu'est-ce que la rotation des cultures ?",
                "Mes feuilles ont des taches jaunes, pourquoi ?",
                "Comment améliorer mon sol ?",
                "Quels engrais pour le colza ?",
                "Comment reconnaître l'oïdium ?"
        ));
    }

    // ═══════════════════════════════════════════════════════════
    //  APIs PUBLIQUES SUPPLÉMENTAIRES
    // ═══════════════════════════════════════════════════════════

    public List<String> getQuestionsFrequentes() {
        return Collections.unmodifiableList(questionsFrequentes);
    }

    public List<String> getQuestionsPopulaires() {
        return statistiquesQuestions.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> e.getKey() + " (" + e.getValue() + " fois)")
                .collect(Collectors.toList());
    }

    /** Questions non reconnues — pour améliorer la base de connaissances */
    public List<String> getQuestionsNonReconnues() {
        return unknownQuestionsLog.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> e.getKey() + " (" + e.getValue() + " fois)")
                .collect(Collectors.toList());
    }

    /** Catégories disponibles dans la base */
    public List<String> getCategories() {
        return knowledgeBase.stream()
                .map(k -> k.category)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** Réinitialise l'historique de conversation */
    public void resetConversation() {
        conversationHistory.clear();
    }

    /** Résumé de l'état du service */
    public String getServiceStats() {
        return String.format(
                "AgriBot v2.0 | Base: %d entrées | Catégories: %d | Questions traitées: %d | Inconnues: %d",
                knowledgeBase.size(),
                getCategories().size(),
                statistiquesQuestions.values().stream().mapToInt(Integer::intValue).sum(),
                unknownQuestionsLog.size()
        );
    }
}
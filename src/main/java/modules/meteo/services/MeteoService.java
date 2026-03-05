package modules.meteo.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import modules.meteo.models.AlerteMeteo;
import modules.meteo.models.PrevisionMeteo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MeteoService {

    // 🔑 Obtenez une clé gratuite sur https://openweathermap.org/api
    private static final String API_KEY = "e7e9f89f88f22ed9585f10c2f3ca9db9"; // À remplacer
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";

    private List<AlerteMeteo> alertes = new ArrayList<>();
    private NotificationService notificationService = new NotificationService();

    /**
     * Récupère la météo actuelle pour une ville
     */
    public PrevisionMeteo getMeteoActuelle(String ville) {
        try {
            String urlStr = BASE_URL + "/weather?q=" + ville +
                    "&appid=" + API_KEY + "&units=metric&lang=fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            PrevisionMeteo meteo = new PrevisionMeteo();
            meteo.setVille(ville);
            meteo.setDate(LocalDateTime.now());

            // Température
            JsonObject main = json.get("main").getAsJsonObject();
            meteo.setTemperature(main.get("temp").getAsDouble());
            meteo.setTemperatureRessentie(main.get("feels_like").getAsDouble());
            meteo.setHumidite(main.get("humidity").getAsInt());
            meteo.setPression(main.get("pressure").getAsDouble());

            // Vent
            JsonObject wind = json.get("wind").getAsJsonObject();
            meteo.setVitesseVent(wind.get("speed").getAsDouble());
            if (wind.has("deg")) {
                meteo.setDirectionVent(wind.get("deg").getAsInt());
            }

            // Description
            JsonObject weather = json.get("weather").getAsJsonArray().get(0).getAsJsonObject();
            meteo.setDescription(weather.get("description").getAsString());
            meteo.setIcone(weather.get("icon").getAsString());

            // Pluie (si disponible)
            if (json.has("rain")) {
                JsonObject rain = json.get("rain").getAsJsonObject();
                if (rain.has("1h")) {
                    meteo.setQuantitePluie(rain.get("1h").getAsDouble());
                }
                meteo.setProbabilitePluie(100); // S'il pleut, probabilité 100%
            }

            reader.close();
            conn.disconnect();

            // Vérifier si des alertes sont nécessaires
            verifierEtCreerAlertes(meteo);

            return meteo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Récupère les prévisions sur 5 jours
     */
    public List<PrevisionMeteo> getPrevisions(String ville) {
        List<PrevisionMeteo> previsions = new ArrayList<>();

        try {
            String urlStr = BASE_URL + "/forecast?q=" + ville +
                    "&appid=" + API_KEY + "&units=metric&lang=fr&cnt=40";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray list = json.get("list").getAsJsonArray();

            for (int i = 0; i < list.size(); i++) {
                JsonObject item = list.get(i).getAsJsonObject();

                PrevisionMeteo prev = new PrevisionMeteo();
                prev.setVille(ville);

                // Date
                long timestamp = item.get("dt").getAsLong();
                prev.setDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp),
                        ZoneId.systemDefault()
                ));

                // Température
                JsonObject main = item.get("main").getAsJsonObject();
                prev.setTemperature(main.get("temp").getAsDouble());
                prev.setTemperatureRessentie(main.get("feels_like").getAsDouble());
                prev.setHumidite(main.get("humidity").getAsInt());
                prev.setPression(main.get("pressure").getAsDouble());

                // Vent
                JsonObject wind = item.get("wind").getAsJsonObject();
                prev.setVitesseVent(wind.get("speed").getAsDouble());
                if (wind.has("deg")) {
                    prev.setDirectionVent(wind.get("deg").getAsInt());
                }

                // Description
                JsonObject weather = item.get("weather").getAsJsonArray().get(0).getAsJsonObject();
                prev.setDescription(weather.get("description").getAsString());
                prev.setIcone(weather.get("icon").getAsString());

                // Probabilité de pluie
                if (item.has("pop")) {
                    prev.setProbabilitePluie(item.get("pop").getAsDouble() * 100);
                }

                // Quantité de pluie
                if (item.has("rain")) {
                    JsonObject rain = item.get("rain").getAsJsonObject();
                    if (rain.has("3h")) {
                        prev.setQuantitePluie(rain.get("3h").getAsDouble());
                    }
                }

                previsions.add(prev);

                // Vérifier les alertes pour cette prévision
                verifierEtCreerAlertes(prev);
            }

            reader.close();
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return previsions;
    }

    /**
     * Vérifie si la météo nécessite une alerte
     */
    private void verifierEtCreerAlertes(PrevisionMeteo meteo) {

        // Alerte pluie forte
        if (meteo.getQuantitePluie() > 10) {
            AlerteMeteo alerte = new AlerteMeteo(
                    "🌧️ Alerte fortes pluies",
                    String.format("Précipitations intenses: %.1f mm prévues à %s. Risque d'inondation des cultures.",
                            meteo.getQuantitePluie(), meteo.getHeureFormatee()),
                    "PLUIE",
                    meteo.getDate(),
                    "DANGER"
            );
            ajouterAlerte(alerte);
        }
        // Alerte pluie modérée
        else if (meteo.getProbabilitePluie() > 70 && meteo.getQuantitePluie() > 2) {
            AlerteMeteo alerte = new AlerteMeteo(
                    "🌧️ Pluie annoncée",
                    String.format("Probabilité de pluie: %.0f%%. Quantité: %.1f mm. Pensez à reporter les traitements.",
                            meteo.getProbabilitePluie(), meteo.getQuantitePluie()),
                    "PLUIE",
                    meteo.getDate(),
                    "WARNING"
            );
            ajouterAlerte(alerte);
        }

        // Alerte gel
        if (meteo.getTemperature() < 2) {
            AlerteMeteo alerte = new AlerteMeteo(
                    "❄️ Risque de gel",
                    String.format("Température: %.1f°C. Protégez vos cultures sensibles (voiles d'hivernage).",
                            meteo.getTemperature()),
                    "GEL",
                    meteo.getDate(),
                    meteo.getTemperature() < 0 ? "DANGER" : "WARNING"
            );
            ajouterAlerte(alerte);
        }

        // Alerte canicule
        if (meteo.getTemperature() > 35) {
            AlerteMeteo alerte = new AlerteMeteo(
                    "☀️ Alerte canicule",
                    String.format("Température extrême: %.1f°C. Arrosez abondamment le soir et protégez les animaux.",
                            meteo.getTemperature()),
                    "CANICULE",
                    meteo.getDate(),
                    "DANGER"
            );
            ajouterAlerte(alerte);
        }

        // Alerte vent fort
        if (meteo.getVitesseVent() > 50) {
            AlerteMeteo alerte = new AlerteMeteo(
                    "💨 Vent fort",
                    String.format("Vitesse du vent: %.0f km/h. Évitez les traitements phytosanitaires.",
                            meteo.getVitesseVent() * 3.6), // Conversion m/s en km/h
                    "VENT",
                    meteo.getDate(),
                    meteo.getVitesseVent() > 70 ? "DANGER" : "WARNING"
            );
            ajouterAlerte(alerte);
        }

        // Alerte maladies (humidité + température)
        if (meteo.getHumidite() > 80 && meteo.getTemperature() > 15 && meteo.getTemperature() < 25) {
            AlerteMeteo alerte = new AlerteMeteo(
                    "🦠 Risque de maladies",
                    "Conditions favorables au mildiou et à l'oïdium. Traitement préventif recommandé.",
                    "MALADIE",
                    meteo.getDate(),
                    "WARNING"
            );
            ajouterAlerte(alerte);
        }
    }

    /**
     * Ajoute une alerte et envoie une notification
     */
    private void ajouterAlerte(AlerteMeteo alerte) {
        // Éviter les doublons (même type dans les prochaines heures)
        boolean existe = alertes.stream()
                .anyMatch(a -> a.getType().equals(alerte.getType()) &&
                        a.getDatePrevue().toLocalDate().equals(alerte.getDatePrevue().toLocalDate()));

        if (!existe) {
            alertes.add(alerte);

            // Envoyer une notification
            notificationService.envoyerNotification(
                    alerte.getTitre(),
                    alerte.getMessage(),
                    alerte.getNiveau()
            );

            System.out.println("🚨 Alerte créée: " + alerte.getTitre());
        }
    }

    public List<AlerteMeteo> getAlertes() {
        return alertes;
    }

    public List<AlerteMeteo> getAlertesNonLues() {
        return alertes.stream()
                .filter(a -> !a.isLue())
                .toList();
    }

    public void marquerAlerteLue(int id) {
        alertes.stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .ifPresent(a -> a.setLue(true));
    }

    /**
     * Obtient la direction du vent en texte
     */
    public String getDirectionVentTexte(int degres) {
        if (degres >= 337.5 || degres < 22.5) return "Nord";
        if (degres >= 22.5 && degres < 67.5) return "Nord-Est";
        if (degres >= 67.5 && degres < 112.5) return "Est";
        if (degres >= 112.5 && degres < 157.5) return "Sud-Est";
        if (degres >= 157.5 && degres < 202.5) return "Sud";
        if (degres >= 202.5 && degres < 247.5) return "Sud-Ouest";
        if (degres >= 247.5 && degres < 292.5) return "Ouest";
        return "Nord-Ouest";
    }
}
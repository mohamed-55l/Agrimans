package modules.meteo.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import modules.meteo.models.AlerteMeteo;
import modules.meteo.models.PrevisionMeteo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MeteoService {

    private static final String API_KEY = "2ec8e6c9c7e92d7355aebe734cde2a62";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private static final long CACHE_DURATION = 10 * 60 * 1000;

    private Map<String, CacheEntry> cacheMeteo = new ConcurrentHashMap<>();
    private Map<String, CacheEntry> cachePrevisions = new ConcurrentHashMap<>();
    private List<AlerteMeteo> alertes = new ArrayList<>();
    private NotificationService notificationService;

    public MeteoService() {
        this.notificationService = new NotificationService();
    }

    public PrevisionMeteo getMeteoActuelle(String ville) {
        CacheEntry cached = cacheMeteo.get(ville);
        if (cached != null && !cached.isExpired()) {
            System.out.println("📦 Cache: " + ville);
            return (PrevisionMeteo) cached.data;
        }

        try {
            String urlStr = BASE_URL + "/weather?q=" + ville +
                    "&appid=" + API_KEY + "&units=metric&lang=fr";

            JsonObject json = appelAPI(urlStr);
            if (json == null) return getFallbackData(ville);

            PrevisionMeteo meteo = parseMeteoActuelle(json, ville);
            cacheMeteo.put(ville, new CacheEntry(meteo));
            verifierEtCreerAlertes(meteo);
            return meteo;

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return getFallbackData(ville);
        }
    }

    public List<PrevisionMeteo> getPrevisions(String ville) {
        CacheEntry cached = cachePrevisions.get(ville);
        if (cached != null && !cached.isExpired()) {
            System.out.println("📦 Cache prévisions: " + ville);
            return (List<PrevisionMeteo>) cached.data;
        }

        List<PrevisionMeteo> previsions = new ArrayList<>();

        try {
            String urlStr = BASE_URL + "/forecast?q=" + ville +
                    "&appid=" + API_KEY + "&units=metric&lang=fr&cnt=40";

            JsonObject json = appelAPI(urlStr);
            if (json == null) return getFallbackPrevisions(ville);

            JsonArray list = json.get("list").getAsJsonArray();

            for (int i = 0; i < list.size(); i++) {
                JsonObject item = list.get(i).getAsJsonObject();
                PrevisionMeteo prev = parsePrevision(item, ville);
                previsions.add(prev);
                verifierEtCreerAlertes(prev);
            }

            cachePrevisions.put(ville, new CacheEntry(previsions));

        } catch (Exception e) {
            System.err.println("❌ Erreur prévisions: " + e.getMessage());
            return getFallbackPrevisions(ville);
        }

        return previsions;
    }

    private JsonObject appelAPI(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ API erreur " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();
            return json;

        } catch (IOException | JsonSyntaxException e) {
            System.err.println("❌ Erreur API: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private PrevisionMeteo parseMeteoActuelle(JsonObject json, String ville) {
        PrevisionMeteo meteo = new PrevisionMeteo();
        meteo.setVille(ville);
        meteo.setDate(LocalDateTime.now());

        try {
            JsonObject main = json.get("main").getAsJsonObject();
            meteo.setTemperature(main.get("temp").getAsDouble());
            meteo.setTemperatureRessentie(main.get("feels_like").getAsDouble());
            meteo.setHumidite(main.get("humidity").getAsInt());
            meteo.setPression(main.get("pressure").getAsDouble());

            if (json.has("wind")) {
                JsonObject wind = json.get("wind").getAsJsonObject();
                meteo.setVitesseVent(wind.get("speed").getAsDouble());
                if (wind.has("deg")) meteo.setDirectionVent(wind.get("deg").getAsInt());
            }

            JsonObject weather = json.get("weather").getAsJsonArray().get(0).getAsJsonObject();
            meteo.setDescription(weather.get("description").getAsString());
            meteo.setIcone(weather.get("icon").getAsString());

            if (json.has("rain")) {
                JsonObject rain = json.get("rain").getAsJsonObject();
                if (rain.has("1h")) meteo.setQuantitePluie(rain.get("1h").getAsDouble());
                meteo.setProbabilitePluie(100);
            }

            if (json.has("clouds")) {
                JsonObject clouds = json.get("clouds").getAsJsonObject();
                meteo.setNuages(clouds.get("all").getAsInt());
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur parsing: " + e.getMessage());
        }

        return meteo;
    }

    private PrevisionMeteo parsePrevision(JsonObject item, String ville) {
        PrevisionMeteo prev = new PrevisionMeteo();
        prev.setVille(ville);

        try {
            long timestamp = item.get("dt").getAsLong();
            prev.setDate(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp), ZoneId.systemDefault()));

            JsonObject main = item.get("main").getAsJsonObject();
            prev.setTemperature(main.get("temp").getAsDouble());
            prev.setTemperatureRessentie(main.get("feels_like").getAsDouble());
            prev.setHumidite(main.get("humidity").getAsInt());
            prev.setPression(main.get("pressure").getAsDouble());

            if (item.has("wind")) {
                JsonObject wind = item.get("wind").getAsJsonObject();
                prev.setVitesseVent(wind.get("speed").getAsDouble());
                if (wind.has("deg")) prev.setDirectionVent(wind.get("deg").getAsInt());
            }

            JsonObject weather = item.get("weather").getAsJsonArray().get(0).getAsJsonObject();
            prev.setDescription(weather.get("description").getAsString());
            prev.setIcone(weather.get("icon").getAsString());

            if (item.has("pop")) {
                prev.setProbabilitePluie(item.get("pop").getAsDouble() * 100);
            }

            if (item.has("rain")) {
                JsonObject rain = item.get("rain").getAsJsonObject();
                if (rain.has("3h")) prev.setQuantitePluie(rain.get("3h").getAsDouble());
            }

            if (item.has("clouds")) {
                JsonObject clouds = item.get("clouds").getAsJsonObject();
                prev.setNuages(clouds.get("all").getAsInt());
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur parsing prévision: " + e.getMessage());
        }

        return prev;
    }

    private PrevisionMeteo getFallbackData(String ville) {
        PrevisionMeteo fallback = new PrevisionMeteo();
        fallback.setVille(ville);
        fallback.setDate(LocalDateTime.now());
        fallback.setTemperature(20.0);
        fallback.setTemperatureRessentie(19.0);
        fallback.setHumidite(65);
        fallback.setPression(1013);
        fallback.setVitesseVent(10.0);
        fallback.setDescription("Données temporaires");
        fallback.setIcone("01d");
        return fallback;
    }

    private List<PrevisionMeteo> getFallbackPrevisions(String ville) {
        List<PrevisionMeteo> previsions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 8; i++) {
            PrevisionMeteo prev = new PrevisionMeteo();
            prev.setVille(ville);
            prev.setDate(now.plusHours(i * 3));
            prev.setTemperature(18 + (i % 5));
            prev.setHumidite(60 + (i % 20));
            prev.setDescription(i % 2 == 0 ? "Ensoleillé" : "Nuageux");
            prev.setProbabilitePluie(i * 5);
            previsions.add(prev);
        }
        return previsions;
    }

    private void verifierEtCreerAlertes(PrevisionMeteo meteo) {
        if (meteo == null) return;

        if (meteo.getQuantitePluie() > 10) {
            creerAlerte("PLUIE", "🌧️ Alerte fortes pluies",
                    String.format("Précipitations: %.1f mm", meteo.getQuantitePluie()),
                    "DANGER", meteo);
        }
        else if (meteo.getProbabilitePluie() > 70 && meteo.getQuantitePluie() > 2) {
            creerAlerte("PLUIE", "🌧️ Pluie annoncée",
                    String.format("Probabilité: %.0f%%, Quantité: %.1f mm",
                            meteo.getProbabilitePluie(), meteo.getQuantitePluie()),
                    "WARNING", meteo);
        }

        if (meteo.getTemperature() < 2) {
            creerAlerte("GEL", "❄️ Risque de gel",
                    String.format("Température: %.1f°C", meteo.getTemperature()),
                    meteo.getTemperature() < 0 ? "DANGER" : "WARNING", meteo);
        }

        if (meteo.getTemperature() > 35) {
            creerAlerte("CANICULE", "☀️ Alerte canicule",
                    String.format("Température: %.1f°C", meteo.getTemperature()),
                    "DANGER", meteo);
        }

        if (meteo.getVitesseVent() > 15) {
            double ventKmh = meteo.getVitesseVent() * 3.6;
            creerAlerte("VENT", "💨 Vent fort",
                    String.format("Vent: %.0f km/h", ventKmh),
                    meteo.getVitesseVent() > 20 ? "DANGER" : "WARNING", meteo);
        }

        if (meteo.getHumidite() > 80 && meteo.getTemperature() > 15 && meteo.getTemperature() < 25) {
            creerAlerte("MALADIE", "🦠 Risque de maladies",
                    "Conditions favorables au mildiou",
                    "WARNING", meteo);
        }
    }

    private void creerAlerte(String type, String titre, String message, String niveau, PrevisionMeteo meteo) {
        boolean existe = alertes.stream()
                .anyMatch(a -> a.getType().equals(type) &&
                        a.getDatePrevue() != null &&
                        a.getDatePrevue().toLocalDate().equals(meteo.getDate().toLocalDate()) &&
                        Math.abs(a.getDatePrevue().getHour() - meteo.getDate().getHour()) < 6);

        if (!existe) {
            AlerteMeteo alerte = new AlerteMeteo(titre, message, type, meteo.getDate(), niveau);
            alertes.add(alerte);
            notificationService.envoyerNotification(titre, message, niveau);
            System.out.println("🚨 Alerte: " + titre);
        }
    }

    public List<AlerteMeteo> getAlertes() { return new ArrayList<>(alertes); }

    public List<AlerteMeteo> getAlertesNonLues() {
        return alertes.stream().filter(a -> !a.isLue()).toList();
    }

    public void marquerAlerteLue(int index) {
        if (index >= 0 && index < alertes.size()) alertes.get(index).setLue(true);
    }

    public void marquerToutesLues() { alertes.forEach(a -> a.setLue(true)); }

    public void viderCache() {
        cacheMeteo.clear();
        cachePrevisions.clear();
        System.out.println("🗑️ Cache vidé");
    }

    private static class CacheEntry {
        Object data;
        long timestamp;
        CacheEntry(Object data) { this.data = data; this.timestamp = System.currentTimeMillis(); }
        boolean isExpired() { return System.currentTimeMillis() - timestamp > CACHE_DURATION; }
    }
}
package modules.parcelle.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MeteoService {

    private static final String API_KEY = "86575dfd48be37dbc34a0cbc7459c77e";

    public static String getMeteo(String ville) {

        try {
            String urlString =
                    "https://api.openweathermap.org/data/2.5/weather?q="
                            + ville
                            + ",TN&appid=" + API_KEY
                            + "&units=metric&lang=fr";

            URL url = new URL(urlString);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return "Météo indisponible (Ville introuvable)";
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String json = response.toString();

            // 🌡 Température
            String temperature = json.split("\"temp\":")[1].split(",")[0];

            // 💧 Humidité
            String humidity = json.split("\"humidity\":")[1].split("}")[0];

            // ☁ Description
            String description = json.split("\"description\":\"")[1].split("\"")[0];

            // 🌧 Vérifier si pluie existe
            String pluie = "Non";
            if (json.contains("\"rain\"")) {
                pluie = "Oui";
            }

            return "🌡 " + temperature + "°C | 💧 " + humidity +
                    " | 🌧 Pluie : " + pluie;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur météo";
        }
    }
}
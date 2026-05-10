package modules.parcelle.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIService {

    private static final String API_KEY = System.getenv("GROQ_API_KEY");

    public static String poserQuestion(String question) {

        try {
            URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            // Construction propre du JSON avec Gson
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("model", "llama-3.1-8b-instant");

            JsonArray messages = new JsonArray();
            
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "Tu es un expert agricole qui aide les agriculteurs.");
            messages.add(systemMessage);
            
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", question);
            messages.add(userMessage);

            jsonRequest.add("messages", messages);

            String jsonPayload = jsonRequest.toString();

            // Envoi de la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine).append("\n"); // Conserver les retours à la ligne JSON
                }
            }

            if (responseCode >= 200 && responseCode < 300) {
                // Parsing correct du format de réponse de l'API OpenAI / Groq
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    
                    if (message != null && message.has("content")) {
                        return message.get("content").getAsString(); // Gère les retours à la ligne et guillemets autom. !
                    }
                }
                return "Réponse de l'IA illisible.";
            } else {
                return "Erreur API (" + responseCode + "): " + response.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur IA : " + e.getMessage();
        }
    }
}
package modules.user.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RecaptchaService {

    private static final String SECRET_KEY = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe";

    public static boolean verifyToken(String token) {

        if (token == null || token.isEmpty()) {
            return false;
        }

        try {

            String urlString =
                    "https://www.google.com/recaptcha/api/siteverify"
                            + "?secret=" + SECRET_KEY
                            + "&response=" + token;

            URL url = new URL(urlString);

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String result = response.toString();

            return result.contains("\"success\": true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

package modules.user.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    // Email expéditeur
    private static final String FROM = "zidisamir993@gmail.com";

    // ⚠ App Password Gmail (16 caractères, sans espaces)
    private static final String APP_PASSWORD = "iwvumgnfcowzdgcv";

    public static boolean sendOTP(String toEmail, String otpCode) {

        try {

            Properties props = new Properties();

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Timeout pour éviter blocage
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(FROM, APP_PASSWORD);
                        }
                    });

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );

            message.setSubject("🔐 Code de vérification AGRI");

            // Email HTML moderne
            String htmlContent =
                    "<div style='font-family:Arial;padding:20px'>" +
                            "<h2 style='color:#0f6b3b'>AGRI Samir</h2>" +
                            "<p>Votre code de vérification est :</p>" +
                            "<h1 style='letter-spacing:5px; color:#000;'>" + otpCode + "</h1>" +
                            "<p>Ce code est valide pendant 10 minutes.</p>" +
                            "<p style='color:gray;font-size:12px'>Ne partagez jamais ce code.</p>" +
                            "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("Email envoyé avec succès à " + toEmail);
            return true;

        } catch (Exception e) {
            System.out.println("Erreur envoi email : " + e.getMessage());
            return false;
        }
    }
}

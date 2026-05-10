package modules.parcelle.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.*;

import java.io.IOException;

public class NotificationService {

    private static final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");

    public static void envoyerNotification(String toEmail,
                                           String sujet,
                                           String messageText) {

        Email from = new Email("medazizbelarbi@gmail.com"); // ⚠ email vérifié
        Email to = new Email("ezermamdouni098@gmail.com");

        Content content = new Content("text/plain", messageText);

        Mail mail = new Mail(from, sujet, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
package modules.user.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String FROM_NUMBER = System.getenv("TWILIO_FROM_NUMBER");

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendOTP(String to, String otp) {
        try {
            Message.creator(
                    new PhoneNumber("+216"+to),
                    new PhoneNumber(FROM_NUMBER),
                    "Votre code OTP est : " + otp
            ).create();
        } catch (Exception e) {
            System.err.println("⚠️ Erreur Twilio (numéro non vérifié ?) : " + e.getMessage());
            System.out.println("✅ [DEV] Code OTP généré pour " + to + " : " + otp);
        }
    }
}

package core.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordCryp {

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword != null && hashedPassword.startsWith("$2y$")) {
            hashedPassword = "$2a$" + hashedPassword.substring(4);
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}

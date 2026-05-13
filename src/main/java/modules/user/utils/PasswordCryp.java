package modules.user.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordCryp {

    /**
     * Hash a password (Default Java format: $2a$)
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Check password with support for PHP hashes ($2y$)
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2")) {
            return false;
        }

        // 🔄 التعديل السحري: تحويل $2y$ لـ $2a$ باش الـ Java تقبلها
        String compatibleHash = hashedPassword;
        if (hashedPassword.startsWith("$2y$")) {
            compatibleHash = "$2a$" + hashedPassword.substring(4);
        }

        try {
            return BCrypt.checkpw(plainPassword, compatibleHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
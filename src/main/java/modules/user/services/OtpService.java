package modules.user.services;

import core.database.DBConnection;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Base64;

public class OtpService {

    private static final int EXPIRATION_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;

    /* ================= GENERATE OTP ================= */

    public static String generateOTP() {

        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 6 digits
        System.out.println(String.valueOf(otp));
        return String.valueOf(otp);

    }

    /* ================= HASH OTP ================= */

    private static String hashOTP(String otp) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(otp.getBytes());

        return Base64.getEncoder().encodeToString(hash);
    }

    /* ================= SAVE OTP ================= */

    public static void saveOTP(int userId, String otp) throws Exception {

        String hashedOtp = hashOTP(otp);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "REPLACE INTO user_otp (user_id, otp_code, expires_at, attempts) VALUES (?, ?, ?, 0)"
             )) {

            ps.setInt(1, userId);
            ps.setString(2, hashedOtp);
            ps.setTimestamp(3,
                    Timestamp.valueOf(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)));

            ps.executeUpdate();
        }
    }

    /* ================= VERIFY OTP ================= */

    public static boolean verifyOTP(int userId, String otpInput) throws Exception {

        String hashedInput = hashOTP(otpInput);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT otp_code, expires_at, attempts FROM user_otp WHERE user_id=?"
             )) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next())
                    return false;

                int attempts = rs.getInt("attempts");

                if (attempts >= MAX_ATTEMPTS)
                    return false;

                LocalDateTime expires =
                        rs.getTimestamp("expires_at").toLocalDateTime();

                if (LocalDateTime.now().isAfter(expires))
                    return false;

                String storedHash = rs.getString("otp_code");

                if (!storedHash.equals(hashedInput)) {

                    incrementAttempts(conn, userId);
                    return false;
                }

                deleteOTP(conn, userId);
                return true;
            }
        }
    }

    /* ================= INCREMENT ATTEMPTS ================= */

    private static void incrementAttempts(Connection conn, int userId) throws Exception {

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE user_otp SET attempts = attempts + 1 WHERE user_id=?"
        )) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /* ================= DELETE OTP ================= */

    private static void deleteOTP(Connection conn, int userId) throws Exception {

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM user_otp WHERE user_id=?"
        )) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /* ================= FORCE INVALIDATE ================= */

    public static void invalidateOtp(int userId) throws Exception {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM user_otp WHERE user_id=?"
             )) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}

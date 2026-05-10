package modules.user.services;

import core.database.DBConnection;
import core.utils.PasswordCryp;

import java.sql.*;
import java.time.LocalDateTime;

public class ForgotPasswordService {

    public static boolean generateAndSendOTP(String email) {

        try (Connection con = DBConnection.getConnection()) {

            // Vérifier si user existe
            PreparedStatement checkUser = con.prepareStatement(
                    "SELECT * FROM user WHERE email = ?");
            checkUser.setString(1, email);
            ResultSet rs = checkUser.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String otp = OtpService.generateOTP();

            // Supprimer ancien OTP
            PreparedStatement delete = con.prepareStatement(
                    "DELETE FROM password_reset WHERE email = ?");
            delete.setString(1, email);
            delete.executeUpdate();

            // Ajouter nouveau OTP
            PreparedStatement insert = con.prepareStatement(
                    "INSERT INTO password_reset(email, code, expiry) VALUES (?, ?, ?)");
            insert.setString(1, email);
            insert.setString(2, otp);
            insert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));

            insert.executeUpdate();

            // Envoyer Email
            return EmailService.sendOTP(email, otp);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verifyOTP(String email, String code) {

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM password_reset WHERE email = ? AND code = ? AND expiry > NOW()");

            ps.setString(1, email);
            ps.setString(2, code);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean resetPassword(String email, String newPassword) {

        try (Connection con = DBConnection.getConnection()) {

            // 🔐 Hash du mot de passe
            String hashedPassword = PasswordCryp.hashPassword(newPassword);

            PreparedStatement update = con.prepareStatement(
                    "UPDATE user SET password_hash = ? WHERE email = ?");

            update.setString(1, hashedPassword);
            update.setString(2, email);

            int rows = update.executeUpdate();

            // Supprimer OTP après succès
            PreparedStatement delete = con.prepareStatement(
                    "DELETE FROM password_reset WHERE email = ?");
            delete.setString(1, email);
            delete.executeUpdate();

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

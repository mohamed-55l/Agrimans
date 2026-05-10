package modules.user.services;

import core.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserDAO {

    public static void insertUser(
            String name,
            String email,
            String phone,
            String password,
            String otp
    ) {

        try {

            Connection conn = DBConnection.getConnection();

            String sql =
                    "INSERT INTO user(full_name,email,phone,password_hash,role,verification_code,is_verified) "
                            + "VALUES(?,?,?,?,?,?,false)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);
            ps.setString(5, "USER");
            ps.setString(6, otp);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static modules.user.models.User findById(int id) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT id, full_name, email, phone, role FROM user WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new modules.user.models.User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

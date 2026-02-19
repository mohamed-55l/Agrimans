package org.example.dao;

import org.example.model.user;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private Connection cnx;

    public UserDAO() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    public boolean register(user user) {
        System.out.println("=== Registering new user ===");
        System.out.println("Name: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Role: " + user.getRole());

        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                System.out.println("✅ User registered successfully! ID: " + user.getId() + " as " + user.getRole());
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println("❌ Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public user login(String email, String password) {
        System.out.println("=== Attempting login ===");
        System.out.println("Email: " + email);

        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user user = new user();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));

                System.out.println("✅ Login successful! User: " + user.getName() + " (Role: " + user.getRole() + ")");
                return user;
            } else {
                System.out.println("❌ Login failed: Invalid email or password");
                return null;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error checking email: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public user getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user user = new user();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                return user;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error getting user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateUser(user user) {
        String sql = "UPDATE users SET name = ?, email = ?, role = ? WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean changeUserRole(int userId, String newRole) {
        System.out.println("=== Changing user role ===");
        System.out.println("User ID: " + userId);
        System.out.println("New Role: " + newRole);

        if (!newRole.equals("BUYER") && !newRole.equals("SELLER") && !newRole.equals("AGRIMAN")) {
            System.out.println("❌ Invalid role: " + newRole);
            return false;
        }

        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, newRole);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ User role changed successfully to: " + newRole);
                return true;
            } else {
                System.out.println("❌ Failed to change role: User not found");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error changing role: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<user> getAllUsers() {
        List<user> userList = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                user user = new user();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                userList.add(user);
            }

            System.out.println("✅ Found " + userList.size() + " users");
            return userList;

        } catch (SQLException e) {
            System.out.println("❌ Error getting all users: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<user> getAllSellers() {
        List<user> userList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'SELLER' OR role = 'AGRIMAN' ORDER BY id";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                user user = new user();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                userList.add(user);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error getting sellers: " + e.getMessage());
            e.printStackTrace();
        }

        return userList;
    }
}
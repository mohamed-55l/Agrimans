package modules.user.services;

import core.database.Mydb;
import modules.user.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des utilisateurs
 */
public class UserService {

    private Connection cnx = Mydb.getInstance().getCnx();

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Créer un nouvel utilisateur
     */
    public void create(User user) throws SQLException {
        String sql = "INSERT INTO user (nom, prenom, email, password, telephone, role) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getTelephone());
        ps.setString(6, user.getRole());

        ps.executeUpdate();
        System.out.println("✅ Utilisateur créé: " + user.getEmail());
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<User> getAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY role, nom, prenom";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            User user = extractUserFromResultSet(rs);
            list.add(user);
        }

        return list;
    }

    /**
     * Récupérer un utilisateur par son ID
     */
    public User getById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return extractUserFromResultSet(rs);
        }

        return null;
    }

    /**
     * Mettre à jour un utilisateur
     */
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, telephone = ?, role = ?, actif = ? " +
                "WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getTelephone());
        ps.setString(5, user.getRole());
        ps.setBoolean(6, user.isActif());
        ps.setInt(7, user.getId());

        ps.executeUpdate();
        System.out.println("✏️ Utilisateur mis à jour: " + user.getEmail());
    }

    /**
     * Supprimer un utilisateur
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("❌ Utilisateur supprimé, ID: " + id);
    }

    // =====================================================
    // MÉTHODES SPÉCIFIQUES
    // =====================================================

    /**
     * Authentifier un utilisateur
     */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ? AND actif = TRUE";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return extractUserFromResultSet(rs);
        }

        return null;
    }

    /**
     * Récupérer tous les agriculteurs
     */
    public List<User> getAgriculteurs() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = 'AGRICULTEUR' ORDER BY nom, prenom";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            User user = extractUserFromResultSet(rs);
            list.add(user);
        }

        return list;
    }

    /**
     * Compter le nombre d'utilisateurs
     */
    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM user";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }

    /**
     * Compter le nombre d'agriculteurs
     */
    public int countAgriculteurs() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM user WHERE role = 'AGRICULTEUR'";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }

    /**
     * Chercher un utilisateur par email
     */
    public User getByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return extractUserFromResultSet(rs);
        }

        return null;
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    public void setActif(int userId, boolean actif) throws SQLException {
        String sql = "UPDATE user SET actif = ? WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setBoolean(1, actif);
        ps.setInt(2, userId);
        ps.executeUpdate();

        System.out.println((actif ? "✅" : "⛔") + " Utilisateur " + (actif ? "activé" : "désactivé"));
    }

    // =====================================================
    // MÉTHODES PRIVÉES
    // =====================================================

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setTelephone(rs.getString("telephone"));
        user.setRole(rs.getString("role"));
        user.setActif(rs.getBoolean("actif"));

        Timestamp timestamp = rs.getTimestamp("date_creation");
        if (timestamp != null) {
            user.setDateCreation(timestamp.toLocalDateTime());
        }

        return user;
    }
}
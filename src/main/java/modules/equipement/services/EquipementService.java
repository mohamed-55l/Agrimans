package modules.equipement.services;

import core.database.Mydb;
import core.utils.AlertUtils;
import modules.equipement.models.Equipement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des équipements
 *
 * RÔLE: Toutes les opérations CRUD sur la table 'equipement'
 * Pattern: Data Access Object (DAO)
 */
public class EquipementService {

    // =====================================================
    // CONNEXION À LA BASE DE DONNÉES
    // =====================================================

    private Connection cnx = Mydb.getInstance().getCnx();

    // =====================================================
    // OPERATIONS CRUD
    // =====================================================

    /**
     * AJOUTER un nouvel équipement
     * @param equipement L'équipement à ajouter (sans ID)
     * @throws SQLException En cas d'erreur SQL
     *
     * SQL: INSERT INTO equipement (nom, type, prix, disponibilite, user_id)
     */
    public void create(Equipement equipement) throws SQLException {
        // Requête SQL paramétrée (évite les injections)
        String sql = "INSERT INTO equipement (nom, type, prix, disponibilite, user_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        // PreparedStatement pour sécuriser les paramètres
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            // Remplacer les ? par les valeurs
            ps.setString(1, equipement.getNom());
            ps.setString(2, equipement.getType());
            ps.setFloat(3, equipement.getPrix());
            ps.setString(4, equipement.getDisponibilite());
            ps.setInt(5, equipement.getUserId());  // ← Important pour plus tard

            // Exécuter la requête
            ps.executeUpdate();

            System.out.println("✅ Équipement ajouté: " + equipement.getNom());
        }
    }

    /**
     * RÉCUPÉRER tous les équipements
     * @return Liste de tous les équipements
     * @throws SQLException En cas d'erreur SQL
     *
     * SQL: SELECT * FROM equipement
     */
    public List<Equipement> getAll() throws SQLException {
        List<Equipement> list = new ArrayList<>();

        String sql = "SELECT * FROM equipement ORDER BY id DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            // Parcourir les résultats
            while (rs.next()) {
                // Créer un objet Equipement à partir de la ligne courante
                Equipement equipement = extractEquipementFromResultSet(rs);
                list.add(equipement);
            }
        }

        return list;
    }

    /**
     * RÉCUPÉRER un équipement par son ID
     * @param id L'ID recherché
     * @return L'équipement trouvé, ou null
     * @throws SQLException En cas d'erreur SQL
     */
    public Equipement getById(int id) throws SQLException {
        String sql = "SELECT * FROM equipement WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractEquipementFromResultSet(rs);
                }
            }
        }

        return null;
    }

    /**
     * RÉCUPÉRER les équipements d'un utilisateur spécifique
     * @param userId L'ID de l'utilisateur
     * @return Liste des équipements de cet utilisateur
     * @throws SQLException En cas d'erreur SQL
     *
     * SQL: SELECT * FROM equipement WHERE user_id = ?
     *
     * ⚠️ Cette méthode sera utilisée PLUS TARD quand on aura le module User
     */
    public List<Equipement> getByUserId(int userId) throws SQLException {
        List<Equipement> list = new ArrayList<>();

        String sql = "SELECT * FROM equipement WHERE user_id = ? ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Equipement equipement = extractEquipementFromResultSet(rs);
                    list.add(equipement);
                }
            }
        }

        return list;
    }

    /**
     * MODIFIER un équipement existant
     * @param equipement L'équipement avec les nouvelles valeurs
     * @throws SQLException En cas d'erreur SQL
     *
     * SQL: UPDATE equipement SET ... WHERE id = ?
     */
    public void update(Equipement equipement) throws SQLException {
        String sql = "UPDATE equipement SET nom = ?, type = ?, prix = ?, disponibilite = ? " +
                "WHERE id = ?";
        // Note: On ne modifie PAS le user_id ici (propriétaire ne change pas)

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, equipement.getNom());
            ps.setString(2, equipement.getType());
            ps.setFloat(3, equipement.getPrix());
            ps.setString(4, equipement.getDisponibilite());
            ps.setInt(5, equipement.getId());

            ps.executeUpdate();

            System.out.println("✏️ Équipement modifié, ID: " + equipement.getId());
        }
    }

    /**
     * SUPPRIMER un équipement
     * @param id L'ID de l'équipement à supprimer
     * @throws SQLException En cas d'erreur SQL
     *
     * SQL: DELETE FROM equipement WHERE id = ?
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM equipement WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("❌ Équipement supprimé, ID: " + id);
        }
    }

    // =====================================================
    // MÉTHODES SPÉCIFIQUES
    // =====================================================

    /**
     * Compter le nombre total d'équipements
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM equipement";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    /**
     * Compter les équipements en panne
     */
    public int countEnPanne() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM equipement WHERE disponibilite = 'Non disponible'";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    /**
     * Compter les équipements disponibles
     */
    public int countDisponibles() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM equipement WHERE disponibilite = 'Disponible'";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    /**
     * Compter les équipements en maintenance
     */
    public int countEnMaintenance() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM equipement WHERE disponibilite = 'En maintenance'";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    /**
     * RECHERCHER des équipements par nom ou type
     * @param motCle Mot-clé à rechercher
     */
    public List<Equipement> search(String motCle) throws SQLException {
        List<Equipement> list = new ArrayList<>();

        String sql = "SELECT * FROM equipement WHERE nom LIKE ? OR type LIKE ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String pattern = "%" + motCle + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Equipement equipement = extractEquipementFromResultSet(rs);
                    list.add(equipement);
                }
            }
        }

        return list;
    }

    /**
     * Mettre à jour l'état d'un équipement
     */
    public void updateEtat(int id, String nouvelEtat) throws SQLException {
        String sql = "UPDATE equipement SET disponibilite = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nouvelEtat);
            ps.setInt(2, id);
            ps.executeUpdate();

            System.out.println("🔄 État modifié pour équipement ID " + id + " → " + nouvelEtat);
        }
    }

    // =====================================================
    // MÉTHODE PRIVÉE: Convertir ResultSet en objet Equipement
    // =====================================================

    /**
     * Extrait un objet Equipement à partir d'un ResultSet
     * @param rs ResultSet positionné sur une ligne
     * @return L'objet Equipement correspondant
     */
    private Equipement extractEquipementFromResultSet(ResultSet rs) throws SQLException {
        Equipement equipement = new Equipement();

        equipement.setId(rs.getInt("id"));
        equipement.setNom(rs.getString("nom"));
        equipement.setType(rs.getString("type"));
        equipement.setPrix(rs.getFloat("prix"));
        equipement.setDisponibilite(rs.getString("disponibilite"));
        equipement.setUserId(rs.getInt("user_id"));  // ← Important pour plus tard

        return equipement;
    }
}
package modules.review.services;

import core.database.Mydb;
import modules.equipement.models.Equipement;
import modules.review.models.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des reviews
 *
 * RÔLE: Toutes les opérations CRUD sur la table 'review'
 */
public class ReviewService {

    // =====================================================
    // CONNEXION À LA BASE DE DONNÉES
    // =====================================================

    private Connection cnx = Mydb.getInstance().getCnx();

    // =====================================================
    // OPERATIONS CRUD
    // =====================================================

    /**
     * AJOUTER une nouvelle review
     * @param review La review à ajouter
     * @throws SQLException En cas d'erreur SQL
     */
    public void create(Review review) throws SQLException {
        String sql = "INSERT INTO review (commentaire, note, date_review, equipement_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, review.getCommentaire());
            ps.setFloat(2, review.getNote());
            ps.setDate(3, review.getDateReview());
            ps.setInt(4, review.getEquipementId());
            ps.setInt(5, review.getUserId());  // ← Important pour plus tard

            ps.executeUpdate();

            System.out.println("✅ Review ajoutée pour équipement ID: " + review.getEquipementId());
        }
    }

    /**
     * RÉCUPÉRER toutes les reviews avec les infos des équipements
     * @return Liste de toutes les reviews
     * @throws SQLException En cas d'erreur SQL
     *
     * SQL: SELECT avec JOINTURE pour avoir le nom de l'équipement
     */
    public List<Review> getAll() throws SQLException {
        List<Review> list = new ArrayList<>();

        String sql = "SELECT r.*, e.id as e_id, e.nom as e_nom, e.type as e_type, " +
                "e.prix as e_prix, e.disponibilite as e_dispo " +
                "FROM review r " +
                "JOIN equipement e ON r.equipement_id = e.id " +
                "ORDER BY r.date_review DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Review review = extractReviewFromResultSet(rs);

                // Créer l'objet Equipement associé
                Equipement equipement = new Equipement();
                equipement.setId(rs.getInt("e_id"));
                equipement.setNom(rs.getString("e_nom"));
                equipement.setType(rs.getString("e_type"));
                equipement.setPrix(rs.getFloat("e_prix"));
                equipement.setDisponibilite(rs.getString("e_dispo"));

                review.setEquipement(equipement);

                list.add(review);
            }
        }

        return list;
    }

    /**
     * RÉCUPÉRER les reviews d'un équipement spécifique
     * @param equipementId L'ID de l'équipement
     * @return Liste des reviews pour cet équipement
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Review> getByEquipementId(int equipementId) throws SQLException {
        List<Review> list = new ArrayList<>();

        String sql = "SELECT * FROM review WHERE equipement_id = ? ORDER BY date_review DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, equipementId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = extractReviewFromResultSet(rs);
                    list.add(review);
                }
            }
        }

        return list;
    }

    /**
     * RÉCUPÉRER les reviews d'un utilisateur spécifique
     * @param userId L'ID de l'utilisateur
     * @return Liste des reviews de cet utilisateur
     * @throws SQLException En cas d'erreur SQL
     *
     * ⚠️ Cette méthode sera utilisée PLUS TARD quand on aura le module User
     */
    public List<Review> getByUserId(int userId) throws SQLException {
        List<Review> list = new ArrayList<>();

        String sql = "SELECT r.*, e.id as e_id, e.nom as e_nom, e.type as e_type " +
                "FROM review r " +
                "JOIN equipement e ON r.equipement_id = e.id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.date_review DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = extractReviewFromResultSet(rs);

                    // Créer l'objet Equipement associé
                    Equipement equipement = new Equipement();
                    equipement.setId(rs.getInt("e_id"));
                    equipement.setNom(rs.getString("e_nom"));
                    equipement.setType(rs.getString("e_type"));

                    review.setEquipement(equipement);

                    list.add(review);
                }
            }
        }

        return list;
    }

    /**
     * RÉCUPÉRER une review par son ID
     */
    public Review getById(int id) throws SQLException {
        String sql = "SELECT * FROM review WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractReviewFromResultSet(rs);
                }
            }
        }

        return null;
    }

    /**
     * MODIFIER une review
     */
    public void update(Review review) throws SQLException {
        String sql = "UPDATE review SET commentaire = ?, note = ? WHERE id = ?";
        // Note: On ne modifie ni la date, ni l'équipement, ni l'utilisateur

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, review.getCommentaire());
            ps.setFloat(2, review.getNote());
            ps.setInt(3, review.getId());

            ps.executeUpdate();

            System.out.println("✏️ Review modifiée, ID: " + review.getId());
        }
    }

    /**
     * SUPPRIMER une review
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM review WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("❌ Review supprimée, ID: " + id);
        }
    }

    // =====================================================
    // MÉTHODES STATISTIQUES
    // =====================================================

    /**
     * Calculer la note moyenne d'un équipement
     */
    public double getAverageNoteByEquipement(int equipementId) throws SQLException {
        String sql = "SELECT AVG(note) as moyenne FROM review WHERE equipement_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, equipementId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        }

        return 0.0;
    }

    /**
     * Compter le nombre de reviews pour un équipement
     */
    public int countByEquipement(int equipementId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM review WHERE equipement_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, equipementId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    /**
     * Compter le nombre total de reviews
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM review";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    /**
     * Note moyenne globale
     */
    public double getAverageNote() throws SQLException {
        String sql = "SELECT AVG(note) as moyenne FROM review";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("moyenne");
            }
        }

        return 0.0;
    }

    // =====================================================
    // MÉTHODE PRIVÉE: Convertir ResultSet en objet Review
    // =====================================================

    private Review extractReviewFromResultSet(ResultSet rs) throws SQLException {
        Review review = new Review();

        review.setId(rs.getInt("id"));
        review.setCommentaire(rs.getString("commentaire"));
        review.setNote(rs.getFloat("note"));
        review.setDateReview(rs.getDate("date_review"));
        review.setEquipementId(rs.getInt("equipement_id"));
        review.setUserId(rs.getInt("user_id"));  // ← Important pour plus tard

        return review;
    }
}
package modules.review.services;

import modules.review.models.Review;
import modules.equipement.models.Equipement;
import core.database.Mydb;
import core.database.IService;  // ← IMPORTANT

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewService implements IService<Review> {

    Connection cnx = Mydb.getInstance().getCnx();

    public void create(Review r) throws SQLException {
        String sql = "INSERT INTO review(commentaire, note, date_review, equipement_id) VALUES(?,?,?,?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, r.getCommentaire());
        ps.setFloat(2, r.getNote());  // float au lieu de int
        ps.setDate(3, r.getDateReview());
        ps.setInt(4, r.getEquipementId());

        ps.executeUpdate();
        System.out.println("✅ Review ajoutée");
    }

    public List<Review> getAll() throws SQLException {
        List<Review> list = new ArrayList<>();

        String sql = "SELECT r.*, e.id as e_id, e.nom as e_nom, e.type as e_type, e.prix as e_prix, e.disponibilite as e_dispo " +
                "FROM review r " +
                "JOIN equipement e ON r.equipement_id = e.id";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Review r = new Review();
            r.setId(rs.getInt("id"));
            r.setCommentaire(rs.getString("commentaire"));
            r.setNote(rs.getFloat("note"));  // float
            r.setDateReview(rs.getDate("date_review"));
            r.setEquipementId(rs.getInt("equipement_id"));

            // Créer l'objet Equipement pour la jointure
            Equipement e = new Equipement();
            e.setId(rs.getInt("e_id"));
            e.setNom(rs.getString("e_nom"));
            e.setType(rs.getString("e_type"));
            e.setPrix(rs.getFloat("e_prix"));
            e.setDisponibilite(rs.getString("e_dispo"));

            r.setEquipement(e);

            list.add(r);
        }

        return list;
    }

    public Review getById(int id) throws SQLException {
        String sql = "SELECT r.*, e.id as e_id, e.nom as e_nom, e.type as e_type, e.prix as e_prix, e.disponibilite as e_dispo " +
                "FROM review r " +
                "JOIN equipement e ON r.equipement_id = e.id " +
                "WHERE r.id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Review r = new Review();
            r.setId(rs.getInt("id"));
            r.setCommentaire(rs.getString("commentaire"));
            r.setNote(rs.getFloat("note"));
            r.setDateReview(rs.getDate("date_review"));
            r.setEquipementId(rs.getInt("equipement_id"));

            Equipement e = new Equipement();
            e.setId(rs.getInt("e_id"));
            e.setNom(rs.getString("e_nom"));
            e.setType(rs.getString("e_type"));
            e.setPrix(rs.getFloat("e_prix"));
            e.setDisponibilite(rs.getString("e_dispo"));

            r.setEquipement(e);

            return r;
        }

        return null;
    }

    public void update(Review r) throws SQLException {
        String sql = "UPDATE review SET commentaire=?, note=?, date_review=?, equipement_id=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, r.getCommentaire());
        ps.setFloat(2, r.getNote());
        ps.setDate(3, r.getDateReview());
        ps.setInt(4, r.getEquipementId());
        ps.setInt(5, r.getId());

        ps.executeUpdate();
        System.out.println("✏️ Review modifiée");
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM review WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("❌ Review supprimée");
    }

    // Méthode pour les jointures (optionnelle)
    public List<String> getAllWithEquipement() throws SQLException {
        List<String> list = new ArrayList<>();

        String sql = "SELECT r.commentaire, r.note, e.nom " +
                "FROM review r " +
                "JOIN equipement e ON r.equipement_id = e.id";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            String row = rs.getString("commentaire") +
                    " | Note : " + rs.getInt("note") +
                    " | Équipement : " + rs.getString("nom");
            list.add(row);
        }

        return list;
    }


}
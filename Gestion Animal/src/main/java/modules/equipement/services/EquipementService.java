package modules.equipement.services;  // ← CRITIQUE : package doit correspondre au dossier

import modules.equipement.models.Equipement;  // ← model déplacé
import core.database.Mydb;                     // ← database déplacé
import core.database.IService;                  // ← interface déplacée

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class EquipementService
        implements IService<Equipement> {

    Connection cnx =
            Mydb.getInstance().getCnx();

    // ✅ CREATE - Ajouter userId
    @Override
    public void create(Equipement e) throws SQLException {
        String sql = "INSERT INTO equipement(nom, type, prix, disponibilite, user_id) " +
                "VALUES(?, ?, ?, ?, ?)";  // ← AJOUTER ?

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, e.getNom());
        ps.setString(2, e.getType());
        ps.setFloat(3, e.getPrix());
        ps.setString(4, e.getDisponibilite());
        ps.setInt(5, e.getUserId());  // ← AJOUTER CETTE LIGNE

        ps.executeUpdate();
    }

    // ✏️ UPDATE
    @Override
    public void update(Equipement e)
            throws SQLException {

        String sql =
                "UPDATE equipement SET nom=?,type=?,prix=?,disponibilite=? WHERE id=?";

        PreparedStatement ps =
                cnx.prepareStatement(sql);

        ps.setString(1, e.getNom());
        ps.setString(2, e.getType());
        ps.setFloat(3, e.getPrix());
        ps.setString(4, e.getDisponibilite());
        ps.setInt(5, e.getId());


        ps.executeUpdate();
        System.out.println("✏️ Equipement modifié");
    }

    // ❌ DELETE
    @Override
    public void delete(int id)
            throws SQLException {

        String sql =
                "DELETE FROM equipement WHERE id=?";

        PreparedStatement ps =
                cnx.prepareStatement(sql);

        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("❌ Equipement supprimé");
    }

    // ✅ GET ALL - Récupérer aussi userId
    @Override
    public List<Equipement> getAll() throws SQLException {
        List<Equipement> list = new ArrayList<>();
        String sql = "SELECT * FROM equipement";  // user_id est dans SELECT *

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Equipement e = new Equipement(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("type"),
                    rs.getFloat("prix"),
                    rs.getString("disponibilite"),
                    rs.getInt("user_id")  // ← AJOUTER CET ARGUMENT
            );
            list.add(e);
        }
        return list;
    }

    // 🔍 GET BY ID  ← (méthode manquante)
    @Override
    public Equipement getById(int id)
            throws SQLException {

        String sql =
                "SELECT * FROM equipement WHERE id=?";

        PreparedStatement ps =
                cnx.prepareStatement(sql);

        ps.setInt(1, id);

        ResultSet rs =
                ps.executeQuery();

        if (rs.next()) {

            return new Equipement(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("type"),
                    rs.getFloat("prix"),
                    rs.getString("disponibilite"),
                    rs.getInt("user_id")
            );
        }

        return null;
    }

    // ✅ NOUVELLE MÉTHODE - Pour plus tard (optionnel)
    public List<Equipement> getByUserId(int userId) throws SQLException {
        List<Equipement> list = new ArrayList<>();
        String sql = "SELECT * FROM equipement WHERE user_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Equipement e = new Equipement(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("type"),
                    rs.getFloat("prix"),
                    rs.getString("disponibilite"),
                    rs.getInt("user_id")
            );
            list.add(e);
        }
        return list;
    }

}


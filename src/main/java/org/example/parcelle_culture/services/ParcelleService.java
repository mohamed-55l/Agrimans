package org.example.parcelle_culture.services;

import org.example.parcelle_culture.entities.Parcelle;
import org.example.parcelle_culture.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParcelleService {

    private Connection conn;

    public ParcelleService() {
        conn = DBConnection.getInstance().getConnection();
    }

    // CREATE
    public void ajouterParcelle(Parcelle p) {
        String sql = "INSERT INTO parcelle (nom, superficie, localisation, type_sol, utilisateur_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSuperficie());
            ps.setString(3, p.getLocalisation());
            ps.setString(4, p.getTypeSol());
            ps.setInt(5, p.getUtilisateurId());

            ps.executeUpdate();
            System.out.println("Parcelle ajoutée avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<Parcelle> afficherParcelles() {
        List<Parcelle> list = new ArrayList<>();
        String sql = "SELECT * FROM parcelle";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Parcelle p = new Parcelle(
                        rs.getInt("id_parcelle"),
                        rs.getString("nom"),
                        rs.getDouble("superficie"),
                        rs.getString("localisation"),
                        rs.getString("type_sol"),
                        rs.getInt("utilisateur_id")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public void modifierParcelle(Parcelle p) {
        String sql = "UPDATE parcelle SET nom=?, superficie=?, localisation=?, type_sol=? WHERE id_parcelle=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSuperficie());
            ps.setString(3, p.getLocalisation());
            ps.setString(4, p.getTypeSol());
            ps.setInt(5, p.getIdParcelle());

            ps.executeUpdate();
            System.out.println("Parcelle modifiée");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimerParcelle(int id) {
        String sql = "DELETE FROM parcelle WHERE id_parcelle=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Parcelle supprimée");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

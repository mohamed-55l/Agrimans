package org.example.parcelle_culture.services;

import org.example.parcelle_culture.entities.Culture;
import org.example.parcelle_culture.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CultureService {

    private Connection conn;

    public CultureService() {
        conn = DBConnection.getInstance().getConnection();
    }

    // CREATE
    public void ajouterCulture(Culture c) {
        String sql = "INSERT INTO culture (nom, type_culture, date_plantation, date_recolte_prevue, etat_culture,parcelle_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getTypeCulture());
            ps.setDate(3, Date.valueOf(c.getDatePlantation()));
            ps.setDate(4, Date.valueOf(c.getDateRecoltePrevue()));
            ps.setString(5, c.getEtatCulture());
            ps.setInt(6, c.getParcelleId());


            ps.executeUpdate();
            System.out.println("Culture ajoutée avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<Culture> afficherCultures() {
        List<Culture> list = new ArrayList<>();
        String sql = "SELECT * FROM culture";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Culture c = new Culture(
                        rs.getInt("id_culture"),
                        rs.getString("nom"),
                        rs.getString("type_culture"),
                        rs.getDate("date_plantation").toLocalDate(),
                        rs.getDate("date_recolte_prevue").toLocalDate(),
                        rs.getString("etat_culture"),
                        rs.getInt("parcelle_id")
                );
                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public void modifierCulture(Culture c) {
        String sql = "UPDATE culture SET nom=?, type_culture=?, date_plantation=?, date_recolte_prevue=?, etat_culture=? WHERE id_culture=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getTypeCulture());
            ps.setDate(3, Date.valueOf(c.getDatePlantation()));
            ps.setDate(4, Date.valueOf(c.getDateRecoltePrevue()));
            ps.setString(5, c.getEtatCulture());
            ps.setInt(6, c.getIdCulture());

            ps.executeUpdate();
            System.out.println("Culture modifiée");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimerCulture(int id) {
        String sql = "DELETE FROM culture WHERE id_culture=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Culture supprimée");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void afficherCulturesAvecParcelle() {

        String sql = """
            SELECT c.id_culture,
                   c.nom AS culture_nom,
                   c.type_culture,
                   c.date_plantation,
                   c.date_recolte_prevue,
                   c.etat_culture,
                   p.nom AS parcelle_nom
            FROM culture c
            JOIN parcelle p
            ON c.parcelle_id = p.id_parcelle
            """;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                System.out.println("Culture: " + rs.getString("culture_nom"));
                System.out.println("Type: " + rs.getString("type_culture"));
                System.out.println("Parcelle: " + rs.getString("parcelle_nom"));
                System.out.println("--------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

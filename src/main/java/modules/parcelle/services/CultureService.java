package modules.parcelle.services;

import modules.parcelle.models.Culture;
import core.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CultureService {

    private Connection conn;

    public CultureService() {
        conn = DBConnection.getConnection();
    }

    // Helper null-safe : évite le NPE si la date SQL est NULL en BDD
    private java.time.LocalDate toLocalDate(Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }

    // CREATE
    public void ajouterCulture(Culture c) {
        if (conn == null) { System.err.println("[CultureService] Connection BDD nulle — ajouterCulture ignoré."); return; }
        String sql = "INSERT INTO culture (nom, type_culture, date_plantation, date_recolte_prevue, etat_culture,parcelle_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getTypeCulture());
            ps.setDate(3, c.getDatePlantation() != null ? Date.valueOf(c.getDatePlantation()) : null);
            ps.setDate(4, c.getDateRecoltePrevue() != null ? Date.valueOf(c.getDateRecoltePrevue()) : null);
            ps.setString(5, c.getEtatCulture());
            ps.setInt(6, c.getParcelleId());

            ps.executeUpdate();
            System.out.println("Culture ajoutée avec succès");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<Culture> afficherCultures() {
        List<Culture> list = new ArrayList<>();

        if (conn == null) {
            System.err.println("[CultureService] Connection BDD nulle — afficherCultures ignoré.");
            return list;
        }

        String sql = "SELECT * FROM culture";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Culture c = new Culture(
                        rs.getInt("id_culture"),
                        rs.getString("nom"),
                        rs.getString("type_culture"),
                        toLocalDate(rs.getDate("date_plantation")),      // null-safe
                        toLocalDate(rs.getDate("date_recolte_prevue")),  // null-safe
                        rs.getString("etat_culture"),
                        rs.getInt("parcelle_id")
                );
                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public void modifierCulture(Culture c) {
        if (conn == null) { System.err.println("[CultureService] Connection BDD nulle — modifierCulture ignoré."); return; }
        String sql = "UPDATE culture SET nom=?, type_culture=?, date_plantation=?, date_recolte_prevue=?, etat_culture=? WHERE id_culture=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getTypeCulture());
            ps.setDate(3, c.getDatePlantation() != null ? Date.valueOf(c.getDatePlantation()) : null);
            ps.setDate(4, c.getDateRecoltePrevue() != null ? Date.valueOf(c.getDateRecoltePrevue()) : null);
            ps.setString(5, c.getEtatCulture());
            ps.setInt(6, c.getIdCulture());

            ps.executeUpdate();
            System.out.println("Culture modifiée");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimerCulture(int id) {
        if (conn == null) { System.err.println("[CultureService] Connection BDD nulle — supprimerCulture ignoré."); return; }
        String sql = "DELETE FROM culture WHERE id_culture=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Culture supprimée");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void afficherCulturesAvecParcelle() {
        if (conn == null) { System.err.println("[CultureService] Connection BDD nulle — afficherCulturesAvecParcelle ignoré."); return; }

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

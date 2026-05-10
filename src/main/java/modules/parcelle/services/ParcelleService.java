package modules.parcelle.services;

import modules.parcelle.models.Parcelle;
import core.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParcelleService {

    private Connection conn;

    public ParcelleService() {
        conn = DBConnection.getConnection();
    }

    // CREATE
    public void ajouterParcelle(Parcelle p) {

        String sql = "INSERT INTO parcelle (nom, superficie, localisation, type_sol, utilisateur_id, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSuperficie());
            ps.setString(3, p.getLocalisation());
            ps.setString(4, p.getTypeSol());
            ps.setInt(5, p.getUtilisateurId());
            ps.setDouble(6, p.getLatitude());
            ps.setDouble(7, p.getLongitude());

            ps.executeUpdate();

            System.out.println("Parcelle ajoutée avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur BDD : " + e.getMessage());
        }
    }

    // READ (avec nom utilisateur)
    public List<Parcelle> afficherParcelles() {

        List<Parcelle> list = new ArrayList<>();

        if (conn == null) {
            System.err.println("[ParcelleService] Connection BDD nulle — afficherParcelles ignoré.");
            return list;
        }

        String sql = """
                SELECT p.*, COALESCE(u.full_name, 'Inconnu') AS utilisateur_nom
                FROM parcelle p
                LEFT JOIN user u
                ON p.utilisateur_id = u.id
                """;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Parcelle p = new Parcelle(
                        rs.getInt("id_parcelle"),
                        rs.getString("nom"),
                        rs.getDouble("superficie"),
                        rs.getString("localisation"),
                        rs.getString("type_sol"),
                        rs.getInt("utilisateur_id"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                );

                // Garantir que utilisateurNom n'est jamais null
                String nom = rs.getString("utilisateur_nom");
                p.setUtilisateurNom(nom != null ? nom : "Inconnu");

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public void modifierParcelle(Parcelle p) {

        if (conn == null) { System.err.println("[ParcelleService] Connection BDD nulle — modifierParcelle ignoré."); return; }

        String sql = "UPDATE parcelle SET nom=?, superficie=?, localisation=?, type_sol=?, latitude=?, longitude=? WHERE id_parcelle=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSuperficie());
            ps.setString(3, p.getLocalisation());
            ps.setString(4, p.getTypeSol());
            ps.setDouble(5, p.getLatitude());
            ps.setDouble(6, p.getLongitude());
            ps.setInt(7, p.getIdParcelle());

            ps.executeUpdate();

            System.out.println("Parcelle modifiée");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimerParcelle(int id) {

        if (conn == null) { System.err.println("[ParcelleService] Connection BDD nulle — supprimerParcelle ignoré."); return; }

        String sql = "DELETE FROM parcelle WHERE id_parcelle=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("Parcelle supprimée");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // STATISTIQUE
    public List<Object[]> getParcellesParLocalisation() {

        List<Object[]> list = new ArrayList<>();

        if (conn == null) {
            System.err.println("[ParcelleService] Connection BDD nulle — getParcellesParLocalisation ignoré.");
            return list;
        }

        String sql = "SELECT localisation, COUNT(*) AS total FROM parcelle GROUP BY localisation";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Object[] row = new Object[2];
                row[0] = rs.getString("localisation");
                row[1] = rs.getInt("total");

                list.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
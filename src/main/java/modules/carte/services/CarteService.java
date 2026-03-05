package modules.carte.services;

import modules.carte.models.EquipementGeo;
import modules.carte.models.Garage;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarteService {

    private Connection cnx;
    private EquipementService equipementService;

    public CarteService() {
        this.cnx = core.database.Mydb.getInstance().getCnx();
        this.equipementService = new EquipementService();
    }

    // =====================================================
    // CRUD GARAGES
    // =====================================================

    public void createGarage(Garage garage) throws SQLException {
        String sql = "INSERT INTO garage (nom, adresse, latitude, longitude, capacite, responsable, telephone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, garage.getNom());
        ps.setString(2, garage.getAdresse());
        ps.setDouble(3, garage.getLatitude());
        ps.setDouble(4, garage.getLongitude());
        ps.setInt(5, garage.getCapacite());
        ps.setString(6, garage.getResponsable());
        ps.setString(7, garage.getTelephone());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            garage.setId(rs.getInt(1));
        }

        System.out.println("✅ Garage créé: " + garage.getNom());
    }

    public List<Garage> getAllGarages() throws SQLException {
        List<Garage> list = new ArrayList<>();
        String sql = "SELECT * FROM garage ORDER BY nom";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Garage garage = new Garage();
            garage.setId(rs.getInt("id"));
            garage.setNom(rs.getString("nom"));
            garage.setAdresse(rs.getString("adresse"));
            garage.setLatitude(rs.getDouble("latitude"));
            garage.setLongitude(rs.getDouble("longitude"));
            garage.setCapacite(rs.getInt("capacite"));
            garage.setResponsable(rs.getString("responsable"));
            garage.setTelephone(rs.getString("telephone"));

            // Charger les équipements de ce garage
            chargerEquipementsGarage(garage);

            list.add(garage);
        }

        return list;
    }

    public Garage getGarageById(int id) throws SQLException {
        String sql = "SELECT * FROM garage WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Garage garage = new Garage();
            garage.setId(rs.getInt("id"));
            garage.setNom(rs.getString("nom"));
            garage.setAdresse(rs.getString("adresse"));
            garage.setLatitude(rs.getDouble("latitude"));
            garage.setLongitude(rs.getDouble("longitude"));
            garage.setCapacite(rs.getInt("capacite"));
            garage.setResponsable(rs.getString("responsable"));
            garage.setTelephone(rs.getString("telephone"));

            chargerEquipementsGarage(garage);

            return garage;
        }

        return null;
    }

    public void updateGarage(Garage garage) throws SQLException {
        String sql = "UPDATE garage SET nom = ?, adresse = ?, latitude = ?, longitude = ?, " +
                "capacite = ?, responsable = ?, telephone = ? WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, garage.getNom());
        ps.setString(2, garage.getAdresse());
        ps.setDouble(3, garage.getLatitude());
        ps.setDouble(4, garage.getLongitude());
        ps.setInt(5, garage.getCapacite());
        ps.setString(6, garage.getResponsable());
        ps.setString(7, garage.getTelephone());
        ps.setInt(8, garage.getId());

        ps.executeUpdate();
        System.out.println("✏️ Garage modifié: " + garage.getNom());
    }

    public void deleteGarage(int id) throws SQLException {
        // D'abord, mettre à jour les équipements pour enlever la référence au garage
        String updateEquipements = "UPDATE equipement_geo SET garage_id = NULL WHERE garage_id = ?";
        PreparedStatement ps1 = cnx.prepareStatement(updateEquipements);
        ps1.setInt(1, id);
        ps1.executeUpdate();

        // Ensuite, supprimer le garage
        String sql = "DELETE FROM garage WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("❌ Garage supprimé, ID: " + id);
    }

    // =====================================================
    // GESTION DES ÉQUIPEMENTS DANS LES GARAGES
    // =====================================================

    public void assignerEquipementAGarage(int equipementId, int garageId) throws SQLException {
        String sql = "UPDATE equipement_geo SET garage_id = ?, statut_garage = 'DANS_GARAGE', " +
                "derniere_localisation = NOW() WHERE equipement_id = ?";

        // Vérifier d'abord si l'équipement existe dans la table geo
        String checkSql = "SELECT * FROM equipement_geo WHERE equipement_id = ?";
        PreparedStatement checkPs = cnx.prepareStatement(checkSql);
        checkPs.setInt(1, equipementId);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next()) {
            // Mise à jour
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, garageId);
            ps.setInt(2, equipementId);
            ps.executeUpdate();
        } else {
            // Insertion
            String insertSql = "INSERT INTO equipement_geo (equipement_id, garage_id, statut_garage, derniere_localisation) " +
                    "VALUES (?, ?, 'DANS_GARAGE', NOW())";
            PreparedStatement ps = cnx.prepareStatement(insertSql);
            ps.setInt(1, equipementId);
            ps.setInt(2, garageId);
            ps.executeUpdate();
        }

        System.out.println("📍 Équipement " + equipementId + " assigné au garage " + garageId);
    }

    public void changerStatutEquipement(int equipementId, String nouveauStatut) throws SQLException {
        String sql = "UPDATE equipement_geo SET statut_garage = ?, derniere_localisation = NOW() " +
                "WHERE equipement_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, nouveauStatut);
        ps.setInt(2, equipementId);
        ps.executeUpdate();

        System.out.println("🔄 Statut équipement " + equipementId + " → " + nouveauStatut);
    }

    public void mettreAJourPosition(int equipementId, double latitude, double longitude) throws SQLException {
        String sql = "UPDATE equipement_geo SET latitude = ?, longitude = ?, position_gps = ?, " +
                "derniere_localisation = NOW() WHERE equipement_id = ?";

        String positionGps = latitude + "," + longitude;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDouble(1, latitude);
        ps.setDouble(2, longitude);
        ps.setString(3, positionGps);
        ps.setInt(4, equipementId);
        ps.executeUpdate();

        System.out.println("📍 Position mise à jour pour équipement " + equipementId);
    }

    private void chargerEquipementsGarage(Garage garage) throws SQLException {
        String sql = "SELECT eg.*, e.nom, e.type, e.prix, e.disponibilite " +
                "FROM equipement_geo eg " +
                "JOIN equipement e ON eg.equipement_id = e.id " +
                "WHERE eg.garage_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, garage.getId());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            EquipementGeo equipement = new EquipementGeo();
            equipement.setId(rs.getInt("equipement_id"));
            equipement.setNom(rs.getString("nom"));
            equipement.setType(rs.getString("type"));
            equipement.setPrix(rs.getFloat("prix"));
            equipement.setDisponibilite(rs.getString("disponibilite"));
            equipement.setGarageId(garage.getId());
            equipement.setGarageNom(garage.getNom());
            equipement.setStatutGarage(rs.getString("statut_garage"));
            equipement.setPositionGPS(rs.getString("position_gps"));

            Timestamp ts = rs.getTimestamp("derniere_localisation");
            if (ts != null) {
                equipement.setDerniereLocalisation(ts.toLocalDateTime());
            }

            garage.addEquipement(equipement);
        }
    }

    public List<EquipementGeo> getEquipementsHorsGarage() throws SQLException {
        List<EquipementGeo> list = new ArrayList<>();

        String sql = "SELECT eg.*, e.nom, e.type, e.prix, e.disponibilite " +
                "FROM equipement_geo eg " +
                "JOIN equipement e ON eg.equipement_id = e.id " +
                "WHERE eg.garage_id IS NULL OR eg.statut_garage != 'DANS_GARAGE'";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            EquipementGeo equipement = new EquipementGeo();
            equipement.setId(rs.getInt("equipement_id"));
            equipement.setNom(rs.getString("nom"));
            equipement.setType(rs.getString("type"));
            equipement.setPrix(rs.getFloat("prix"));
            equipement.setDisponibilite(rs.getString("disponibilite"));
            equipement.setGarageId(rs.getInt("garage_id"));
            equipement.setStatutGarage(rs.getString("statut_garage"));
            equipement.setPositionGPS(rs.getString("position_gps"));

            list.add(equipement);
        }

        return list;
    }
}
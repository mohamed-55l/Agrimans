package modules.demande.services;

import core.database.Mydb;
import modules.demande.models.Demande;
import modules.equipement.models.Equipement;
import modules.equipement.services.EquipementService;
import modules.user.models.User;
import modules.user.services.UserService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeService {

    private Connection cnx = Mydb.getInstance().getCnx();
    private UserService userService = new UserService();
    private EquipementService equipementService = new EquipementService();

    // =====================================================
    // CRUD
    // =====================================================

    /**
     * Créer une nouvelle demande
     */
    public void create(Demande demande) throws SQLException {
        String sql = "INSERT INTO demande (agriculteur_id, equipement_id, commentaire, statut, date_demande) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, demande.getAgriculteur().getId());
        ps.setInt(2, demande.getEquipement().getId());
        ps.setString(3, demande.getDescription());
        ps.setString(4, "EN_ATTENTE");
        ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            demande.setId(rs.getInt(1));
        }

        System.out.println("✅ Demande créée: " + demande.getDescription());
    }

    /**
     * Récupérer toutes les demandes (pour ADMIN)
     */
    public List<Demande> getAll() throws SQLException {
        List<Demande> list = new ArrayList<>();

        String sql = "SELECT d.*, " +
                "u.id as u_id, u.full_name as u_nom, u.email, " +
                "e.id as e_id, e.nom as e_nom, e.type as e_type " +
                "FROM demande d " +
                "JOIN user u ON d.agriculteur_id = u.id " +
                "JOIN equipement e ON d.equipement_id = e.id " +
                "ORDER BY d.date_demande DESC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(extraireDemande(rs));
        }

        return list;
    }

    /**
     * Récupérer les demandes d'un agriculteur
     */
    public List<Demande> getByAgriculteurId(int agriculteurId) throws SQLException {
        List<Demande> list = new ArrayList<>();

        String sql = "SELECT d.*, " +
                "u.id as u_id, u.full_name as u_nom, u.email, " +
                "e.id as e_id, e.nom as e_nom, e.type as e_type " +
                "FROM demande d " +
                "JOIN user u ON d.agriculteur_id = u.id " +
                "JOIN equipement e ON d.equipement_id = e.id " +
                "WHERE d.agriculteur_id = ? " +
                "ORDER BY d.date_demande DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, agriculteurId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extraireDemande(rs));
        }

        return list;
    }

    /**
     * Mettre à jour le statut d'une demande (pour ADMIN)
     */
    public void updateStatut(int demandeId, String nouveauStatut, String reponse) throws SQLException {
        String sql = "UPDATE demande SET statut = ?, reponse_chef = ?, date_traitement = ? WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, nouveauStatut);
        ps.setString(2, reponse);
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(4, demandeId);

        ps.executeUpdate();
        System.out.println("✏️ Demande " + demandeId + " → " + nouveauStatut);
    }

    /**
     * Supprimer une demande
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM demande WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("❌ Demande supprimée: " + id);
    }

    /**
     * Compter les demandes en attente
     */
    public int countEnAttente() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM demande WHERE statut = 'EN_ATTENTE'";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) return rs.getInt("total");
        return 0;
    }

    // =====================================================
    // Méthode privée pour extraire une demande
    // =====================================================

    private Demande extraireDemande(ResultSet rs) throws SQLException {
        Demande demande = new Demande();

        // Infos demande
        demande.setId(rs.getInt("id"));
        demande.setDescription(rs.getString("description"));  // ← Important !
        demande.setStatut(rs.getString("statut"));
        demande.setReponseChef(rs.getString("reponse_chef"));

        Timestamp dateDemande = rs.getTimestamp("date_demande");
        if (dateDemande != null) {
            demande.setDateDemande(dateDemande.toLocalDateTime());
        }

        Timestamp dateTraitement = rs.getTimestamp("date_traitement");
        if (dateTraitement != null) {
            demande.setDateTraitement(dateTraitement.toLocalDateTime());
        }

        // Créer l'agriculteur (corrigé - sans prenom)
        User agriculteur = new User();
        agriculteur.setId(rs.getInt("u_id"));

        // 👉 CORRECTION ICI : utiliser u_nom seulement
        String nomComplet = rs.getString("u_nom");
        agriculteur.setNom(nomComplet);
        agriculteur.setPrenom(""); // Valeur par défaut

        agriculteur.setEmail(rs.getString("email"));
        demande.setAgriculteur(agriculteur);

        // Créer l'équipement
        Equipement equipement = new Equipement();
        equipement.setId(rs.getInt("e_id"));
        equipement.setNom(rs.getString("e_nom"));
        equipement.setType(rs.getString("e_type"));
        demande.setEquipement(equipement);

        return demande;
    }


    // =====================================================
// NOUVELLES MÉTHODES
// =====================================================

    /**
     * Mettre à jour une demande complète
     */
    public void update(Demande demande) throws SQLException {
        String sql = "UPDATE demande SET " +
                "description = ?, " +
                "quantite = ?, " +
                "equipement_id = ?, " +
                "nom_equipement = ?, " +
                "type_demande = ? " +
                "WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, demande.getDescription());
        ps.setInt(2, demande.getQuantite());

        if (demande.getEquipement() != null) {
            ps.setInt(3, demande.getEquipement().getId());
        } else {
            ps.setNull(3, Types.INTEGER);
        }

        ps.setString(4, demande.getNomEquipement());
        ps.setString(5, demande.getTypeDemande());
        ps.setInt(6, demande.getId());

        ps.executeUpdate();
        System.out.println("✏️ Demande mise à jour: " + demande.getId());
    }

    /**
     * Récupérer les demandes par statut
     */
    public List<Demande> getByStatut(String statut) throws SQLException {
        List<Demande> list = new ArrayList<>();

        String sql = "SELECT d.*, " +
                "u.id as u_id, u.full_name as u_nom, u.email, " +
                "e.id as e_id, e.nom as e_nom, e.type as e_type " +
                "FROM demande d " +
                "LEFT JOIN user u ON d.agriculteur_id = u.id " +
                "LEFT JOIN equipement e ON d.equipement_id = e.id " +
                "WHERE d.statut = ? " +
                "ORDER BY d.date_demande DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, statut);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extraireDemande(rs));
        }

        return list;
    }





}
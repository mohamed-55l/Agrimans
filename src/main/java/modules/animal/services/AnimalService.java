package modules.animal.services;

import core.database.IService;
import core.database.Mydb;
import modules.animal.models.Animal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnimalService implements IService<Animal> {

    private Connection cnx = Mydb.getInstance().getCnx();

    @Override
    public void create(Animal animal) throws SQLException {
        String sql = "INSERT INTO animal (nom, espece, race, poids, etatSante, userId) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, animal.getNom());
            ps.setString(2, animal.getEspece());
            ps.setString(3, animal.getRace());
            ps.setFloat(4, animal.getPoids());
            ps.setString(5, animal.getEtatSante());
            ps.setInt(6, animal.getUserId());
            ps.executeUpdate();
            System.out.println("✅ Animal ajouté: " + animal.getNom());
        }
    }

    @Override
    public void update(Animal animal) throws SQLException {
        String sql = "UPDATE animal SET nom = ?, espece = ?, race = ?, poids = ?, etatSante = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, animal.getNom());
            ps.setString(2, animal.getEspece());
            ps.setString(3, animal.getRace());
            ps.setFloat(4, animal.getPoids());
            ps.setString(5, animal.getEtatSante());
            ps.setInt(6, animal.getId());
            ps.executeUpdate();
            System.out.println("✏️ Animal modifié, ID: " + animal.getId());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM animal WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("❌ Animal supprimé, ID: " + id);
        }
    }

    @Override
    public List<Animal> getAll() throws SQLException {
        List<Animal> list = new ArrayList<>();
        String sql = "SELECT * FROM animal ORDER BY id DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public Animal getById(int id) throws SQLException {
        String sql = "SELECT * FROM animal WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Animal> getByUserId(int userId) throws SQLException {
        List<Animal> list = new ArrayList<>();
        String sql = "SELECT * FROM animal WHERE userId = ? ORDER BY id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Animal> search(String keyword) throws SQLException {
        List<Animal> list = new ArrayList<>();
        String sql = "SELECT * FROM animal WHERE nom LIKE ? OR espece LIKE ? OR race LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    private Animal extractFromResultSet(ResultSet rs) throws SQLException {
        Animal animal = new Animal();
        animal.setId(rs.getInt("id"));
        animal.setNom(rs.getString("nom"));
        animal.setEspece(rs.getString("espece"));
        animal.setRace(rs.getString("race"));
        animal.setPoids(rs.getFloat("poids"));
        animal.setEtatSante(rs.getString("etatSante"));
        animal.setUserId(rs.getInt("userId"));
        return animal;
    }
}

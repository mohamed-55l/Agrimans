package modules.marketplace.services;

import core.database.DBConnection;
import modules.marketplace.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[ProductService] Connection BDD nulle."); return products; }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    products.add(extractFromResultSet(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> getProductsBySeller(int sellerId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE seller_id = ? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[ProductService] Connection BDD nulle."); return products; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sellerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        products.add(extractFromResultSet(rs));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO products (seller_id, name, description, price, quantity, category, supplier, image, expiry_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[ProductService] Connection BDD nulle."); return; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, product.getSellerId());
                pstmt.setString(2, product.getName());
                pstmt.setString(3, product.getDescription());
                pstmt.setFloat(4, product.getPrice());
                pstmt.setInt(5, product.getQuantity());
                pstmt.setString(6, product.getCategory());
                pstmt.setString(7, product.getSupplier());
                pstmt.setString(8, product.getImage());
                if (product.getExpiryDate() != null) {
                    pstmt.setDate(9, product.getExpiryDate());
                } else {
                    pstmt.setNull(9, Types.DATE);
                }
                pstmt.executeUpdate();
                System.out.println("[ProductService] Produit ajouté : " + product.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[ProductService] Connection BDD nulle."); return; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Product extractFromResultSet(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getInt("seller_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getFloat("price"),
            rs.getInt("quantity"),
            rs.getString("category"),
            rs.getString("supplier"),
            rs.getString("image"),
            rs.getDate("expiry_date")
        );
    }
}

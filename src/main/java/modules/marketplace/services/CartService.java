package modules.marketplace.services;

import core.database.DBConnection;
import modules.marketplace.models.CartItem;
import modules.marketplace.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Schéma BDD :
 *   carts      : id, buyer_id
 *   cart_items : id, cart_id, product_id, quantity
 *   products   : id, seller_id, name, price, description, image, category, supplier, expiry_date
 */
public class CartService {

    /** Récupère ou crée le cart_id pour un utilisateur donné */
    private int getOrCreateCart(Connection conn, int userId) throws SQLException {
        // Chercher un panier existant
        String sel = "SELECT id FROM carts WHERE buyer_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        // Créer un nouveau panier
        String ins = "INSERT INTO carts (buyer_id) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Impossible de créer le panier pour user_id=" + userId);
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.*, p.name, p.price, p.description, p.image, p.category, p.supplier " +
                     "FROM cart_items ci " +
                     "JOIN carts c ON ci.cart_id = c.id " +
                     "JOIN products p ON ci.product_id = p.id " +
                     "WHERE c.buyer_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[CartService] Connection BDD nulle."); return items; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        CartItem item = new CartItem(
                            rs.getInt("id"),
                            userId,
                            rs.getInt("product_id"),
                            rs.getFloat("quantity"),
                            null  // pas de added_at dans cart_items
                        );
                        Product p = new Product();
                        p.setId(rs.getInt("product_id"));
                        p.setName(rs.getString("name"));
                        p.setPrice(rs.getFloat("price"));
                        p.setDescription(rs.getString("description"));
                        p.setImageUrl(rs.getString("image"));
                        p.setCategoryName(rs.getString("category"));
                        p.setSupplier(rs.getString("supplier"));
                        item.setProduct(p);
                        items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public void addToCart(int userId, int productId, float quantity) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[CartService] Connection BDD nulle."); return; }
            int cartId = getOrCreateCart(conn, userId);

            // Vérifier si le produit est déjà dans le panier
            String checkSql = "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, cartId);
                ps.setInt(2, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Mettre à jour la quantité
                        int cartItemId = rs.getInt("id");
                        float currentQty = rs.getFloat("quantity");
                        String updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                        try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                            upd.setFloat(1, currentQty + quantity);
                            upd.setInt(2, cartItemId);
                            upd.executeUpdate();
                        }
                    } else {
                        // Insérer un nouvel article
                        String insertSql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)";
                        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                            ins.setInt(1, cartId);
                            ins.setInt(2, productId);
                            ins.setFloat(3, quantity);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateQuantity(int cartItemId, int newQuantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newQuantity);
                pstmt.setInt(2, cartItemId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void removeFromCart(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[CartService] Connection BDD nulle."); return; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, cartItemId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCart(int userId) {
        String sql = "DELETE ci FROM cart_items ci " +
                     "JOIN carts c ON ci.cart_id = c.id " +
                     "WHERE c.buyer_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) { System.err.println("[CartService] Connection BDD nulle."); return; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package org.example.dao;

import org.example.model.*;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.*;

public class CartDAO {

    Connection cnx = MyDatabase.getInstance().getConnection();

    // Get or create cart for user
    public int getOrCreateCart(int userId) {
        System.out.println("=== Getting/Creating cart for user ID: " + userId + " ===");

        // First check if user already has a cart (using buyer_id)
        String checkSql = "SELECT id FROM carts WHERE buyer_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(checkSql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cartId = rs.getInt("id");
                System.out.println("✅ Found existing cart with ID: " + cartId + " for buyer: " + userId);
                return cartId;
            } else {
                System.out.println("No existing cart found for buyer: " + userId);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error checking existing cart: " + e.getMessage());
            e.printStackTrace();
        }

        // If no cart exists, create one (using buyer_id)
        String createSql = "INSERT INTO carts (buyer_id) VALUES (?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Cart creation rows affected: " + rowsAffected);

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newCartId = generatedKeys.getInt(1);
                System.out.println("✅ Created new cart with ID: " + newCartId + " for buyer: " + userId);
                return newCartId;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error creating new cart: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("❌ Failed to get or create cart for buyer: " + userId);
        return -1;
    }

    public void addToCart(int userId, int productId, int qty) {
        System.out.println("=== Adding to cart ===");
        System.out.println("Buyer ID: " + userId);
        System.out.println("Product ID: " + productId);
        System.out.println("Quantity: " + qty);

        // Get or create cart for user
        int cartId = getOrCreateCart(userId);

        if (cartId == -1) {
            System.out.println("❌ ERROR: Could not get or create cart!");
            return;
        }

        System.out.println("Using cart ID: " + cartId);

        // Check if product already exists in cart
        String checkSql = "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try {
            PreparedStatement checkPs = cnx.prepareStatement(checkSql);
            checkPs.setInt(1, cartId);
            checkPs.setInt(2, productId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Update existing cart item
                int existingId = rs.getInt("id");
                int existingQty = rs.getInt("quantity");
                int newQty = existingQty + qty;

                String updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                PreparedStatement updatePs = cnx.prepareStatement(updateSql);
                updatePs.setInt(1, newQty);
                updatePs.setInt(2, existingId);
                int updated = updatePs.executeUpdate();

                System.out.println("🔄 Updated existing cart item: ID=" + existingId +
                        ", Old quantity=" + existingQty +
                        ", New quantity=" + newQty +
                        ", Rows affected: " + updated);
            } else {
                // Insert new cart item
                String insertSql = "INSERT INTO cart_items(cart_id, product_id, quantity) VALUES(?,?,?)";
                PreparedStatement insertPs = cnx.prepareStatement(insertSql);
                insertPs.setInt(1, cartId);
                insertPs.setInt(2, productId);
                insertPs.setInt(3, qty);
                int inserted = insertPs.executeUpdate();

                System.out.println("✅ Inserted new cart item: Cart ID=" + cartId +
                        ", Product ID=" + productId +
                        ", Quantity=" + qty +
                        ", Rows affected: " + inserted);
            }
        } catch (SQLException e) {
            System.out.println("❌ ERROR adding to cart: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== Add to cart complete ===");
    }

    public List<CartItem> getCartItems(int userId) {
        System.out.println("=== Getting cart items for buyer ID: " + userId + " ===");

        List<CartItem> list = new ArrayList<>();

        // First get user's cart
        int cartId = getOrCreateCart(userId);

        if (cartId == -1) {
            System.out.println("❌ ERROR: Could not get cart for buyer!");
            return list;
        }

        System.out.println("Found cart ID: " + cartId + " for buyer: " + userId);

        String sql = """
            SELECT ci.id as cart_item_id, ci.quantity, 
                   p.id as product_id, p.name, p.description, p.price, p.quantity as stock, p.image, p.seller_id
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            WHERE ci.cart_id = ?
            """;

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, cartId);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                Product p = new Product();
                p.setId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("stock"));
                p.setImage(rs.getString("image"));
                p.setSellerId(rs.getInt("seller_id"));

                CartItem ci = new CartItem();
                ci.setId(rs.getInt("cart_item_id"));
                ci.setQuantity(rs.getInt("quantity"));
                ci.setProduct(p);

                list.add(ci);
                System.out.println("  📦 Item " + count + ": " + p.getName() +
                        " x" + ci.getQuantity() +
                        " = $" + (p.getPrice() * ci.getQuantity()));
            }

            System.out.println("✅ Total items in cart: " + list.size());

        } catch (SQLException e) {
            System.out.println("❌ ERROR getting cart items: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public void removeFromCart(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, cartItemId);
            int deleted = ps.executeUpdate();
            System.out.println("🗑️ Removed " + deleted + " item from cart (ID: " + cartItemId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearCart(int userId) {
        int cartId = getOrCreateCart(userId);
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, cartId);
            int deleted = ps.executeUpdate();
            System.out.println("🗑️ Cleared " + deleted + " items from cart for buyer: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
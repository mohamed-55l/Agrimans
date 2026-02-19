package org.example.dao;

import org.example.model.Product;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    Connection cnx = MyDatabase.getInstance().getConnection();

    public void add(Product p) {
        String sql = "INSERT INTO products(name, description, price, quantity, image, seller_id, category, supplier, expiry_date) VALUES(?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getQuantity());
            ps.setString(5, p.getImage());
            ps.setInt(6, p.getSellerId());
            ps.setString(7, p.getCategory());
            ps.setString(8, p.getSupplier());
            ps.setString(9, p.getExpiryDate());
            ps.executeUpdate();
            System.out.println("✅ Product added successfully: " + p.getName());
        } catch (SQLException e) {
            System.out.println("❌ Error adding product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setImage(rs.getString("image"));
                p.setSellerId(rs.getInt("seller_id"));

                try { p.setCategory(rs.getString("category")); } catch (SQLException e) { p.setCategory("OTHER"); }
                try { p.setSupplier(rs.getString("supplier")); } catch (SQLException e) { p.setSupplier("Local Farm"); }
                try { p.setExpiryDate(rs.getString("expiry_date")); } catch (SQLException e) { p.setExpiryDate("Dec 31, 2026"); }

                list.add(p);
            }
            System.out.println("✅ Loaded " + list.size() + " products");
        } catch (SQLException e) {
            System.out.println("❌ Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<Product> findByCategory(String category) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setImage(rs.getString("image"));
                p.setSellerId(rs.getInt("seller_id"));
                p.setCategory(rs.getString("category"));
                p.setSupplier(rs.getString("supplier"));
                p.setExpiryDate(rs.getString("expiry_date"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Product findById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setImage(rs.getString("image"));
                p.setSellerId(rs.getInt("seller_id"));
                p.setCategory(rs.getString("category"));
                p.setSupplier(rs.getString("supplier"));
                p.setExpiryDate(rs.getString("expiry_date"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
package modules.marketplace.models;

import java.sql.Date;

public class Product {
    private int id;
    private int sellerId;       // BDD: seller_id
    private String name;
    private String description;
    private float price;
    private int quantity;       // BDD: int(11)
    private String category;    // BDD: category
    private String supplier;
    private String image;       // BDD: image
    private Date expiryDate;

    public Product() {}

    public Product(int id, int sellerId, String name, String description,
                   float price, int quantity, String category,
                   String supplier, String image, Date expiryDate) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.supplier = supplier;
        this.image = image;
        this.expiryDate = expiryDate;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    /** Alias rétro-compatible pour ProductController */
    public int getUserId() { return sellerId; }
    public void setUserId(int userId) { this.sellerId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** Alias rétro-compatible (anciennement float) */
    public void setQuantity(float quantity) { this.quantity = (int) quantity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    /** Alias rétro-compatible pour les contrôleurs qui utilisent getCategoryName() */
    public String getCategoryName() { return category; }
    public void setCategoryName(String categoryName) { this.category = categoryName; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    /** Alias rétro-compatible pour les contrôleurs qui utilisent getImageUrl() */
    public String getImageUrl() { return image; }
    public void setImageUrl(String imageUrl) { this.image = imageUrl; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    @Override
    public String toString() { return name + " (" + price + " TND)"; }
}

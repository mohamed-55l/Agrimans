package modules.marketplace.models;

public class CartItem {
    private int id;
    private int userId;
    private int productId;
    private float quantity;
    private java.sql.Timestamp addedAt;
    
    // Join properties
    private Product product;

    public CartItem() {}

    public CartItem(int id, int userId, int productId, float quantity, java.sql.Timestamp addedAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public float getQuantity() { return quantity; }
    public void setQuantity(float quantity) { this.quantity = quantity; }
    public java.sql.Timestamp getAddedAt() { return addedAt; }
    public void setAddedAt(java.sql.Timestamp addedAt) { this.addedAt = addedAt; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}

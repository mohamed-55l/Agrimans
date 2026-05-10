package modules.marketplace.models;

import java.sql.Timestamp;

public class Order {
    private int id;
    private int userId;
    private float totalAmount;
    private String status;
    private Timestamp orderDate;

    public Order() {}

    public Order(int id, int userId, float totalAmount, String status, Timestamp orderDate) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public float getTotalAmount() { return totalAmount; }
    public void setTotalAmount(float totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
}

package com.example.petify;

public class OrderModel {

    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private double totalAmount;
    private String status;
    private long createdAt;

    // Required empty constructor for Firestore
    public OrderModel() {}

    public OrderModel(String id, String userId, String userName, String userEmail,
                      double totalAmount, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}

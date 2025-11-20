package com.example.petify;

public class PaymentModel {

    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private double amount;
    private String status;
    private long createdAt;

    // Required empty constructor for Firestore
    public PaymentModel() {}

    public PaymentModel(String id, String userId, String userName, String userEmail,
                        double amount, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}

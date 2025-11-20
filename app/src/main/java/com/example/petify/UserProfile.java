package com.example.petify;

public class UserProfile {

    private String id;
    private String name;
    private String email;
    private String role;

    private String addressLine;
    private String postalCode;
    private String city;
    private String country;

    public UserProfile() {
        // required by Firestore
    }

    public UserProfile(String id, String name, String email, String role,
                       String addressLine, String postalCode, String city, String country) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.addressLine = addressLine;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAddressLine() { return addressLine; }
    public String getPostalCode() { return postalCode; }
    public String getCity() { return city; }
    public String getCountry() { return country; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
}

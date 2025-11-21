package com.example.petify;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserPaymentActivity extends AppCompatActivity {

    private Button paymentButton;
    private TextView tvAmount;

    // Stripe test keys
    private final String PublishableKey =
            "xxx";
    private final String SecretKey =
            "xx";

    private final String CustomersURL    = "https://api.stripe.com/v1/customers";
    private final String EphemeralKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private final String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId = null;
    private String EphemeralKey;
    private String ClientSecret;

    private PaymentSheet paymentSheet;

    // Stripe amount (in cents) and currency
    private String Amount;            // e.g. "1500"
    private final String Currency = "usd";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_payment);

        paymentButton = findViewById(R.id.payment);
        tvAmount      = findViewById(R.id.tvAmount);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Get total from ShoppingCartActivity
        double total = getIntent().getDoubleExtra("totalAmount", 0.0);
        tvAmount.setText(String.format("Total: $%.2f", total));

        long amountInCents = Math.round(total * 100);
        Amount = String.valueOf(amountInCents);

        // Stripe init
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        paymentButton.setOnClickListener(v -> {
            if (CustomerId != null && !CustomerId.isEmpty()) {
                paymentFlow();
            } else {
                Toast.makeText(this, "Stripe customer not ready yet", Toast.LENGTH_SHORT).show();
            }
        });

        // Start Stripe flow by creating customer
        createCustomer();
    }

    // ----------------- STRIPE API CALLS -----------------

    private void createCustomer() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                CustomersURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        CustomerId = object.getString("id");
                        Log.d("Stripe", "Customer created: " + CustomerId);
                        getEphemeralKey();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this,
                                "Error creating customer: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Error creating customer: " + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void getEphemeralKey() {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                EphemeralKeyURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        EphemeralKey = object.getString("id");
                        Log.d("Stripe", "Ephemeral key: " + EphemeralKey);
                        getClientSecret(CustomerId, EphemeralKey);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this,
                                "Error getting ephemeral key: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Error getting ephemeral key: " + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Stripe-Version", "2022-11-15");
                return headers;
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void getClientSecret(String customerId, String ephemeralKey) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ClientSecretURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        ClientSecret = object.getString("client_secret");
                        Log.d("Stripe", "Client secret: " + ClientSecret);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this,
                                "Error getting client secret: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Error getting client secret: " + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("amount", Amount);
                params.put("currency", Currency);
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void paymentFlow() {
        if (ClientSecret == null || ClientSecret.isEmpty()) {
            Toast.makeText(this, "Client secret not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        paymentSheet.presentWithPaymentIntent(
                ClientSecret,
                new PaymentSheet.Configuration(
                        "Petify",
                        new PaymentSheet.CustomerConfiguration(
                                CustomerId,
                                EphemeralKey
                        )
                )
        );
    }

    private void onPaymentResult(@NonNull PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show();
            saveOrderAndClearCart();
        } else {
            Toast.makeText(this, "Payment failed or cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------- FIRESTORE ORDER + PAYMENT -----------------

    private void saveOrderAndClearCart() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String uid   = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();
        double total = getIntent().getDoubleExtra("totalAmount", 0.0);
        long now     = System.currentTimeMillis();

        // First load user profile so we can include address
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    UserProfile profile = doc.toObject(UserProfile.class);

                    String name    = profile != null && profile.getName() != null
                            ? profile.getName() : "";
                    String line    = profile != null ? profile.getAddressLine()   : null;
                    String pc      = profile != null ? profile.getPostalCode()    : null;
                    String city    = profile != null ? profile.getCity()          : null;
                    String country = profile != null ? profile.getCountry()       : null;

                    // Build order
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("userId", uid);
                    orderData.put("userName", name);
                    orderData.put("userEmail", email);
                    orderData.put("totalAmount", total);
                    orderData.put("status", "paid");
                    orderData.put("createdAt", now);

                    orderData.put("shippingAddressLine", line);
                    orderData.put("shippingPostalCode", pc);
                    orderData.put("shippingCity", city);
                    orderData.put("shippingCountry", country);

                    db.collection("orders")
                            .add(orderData)
                            .addOnSuccessListener(orderRef -> {
                                // Also create payment document
                                Map<String, Object> paymentData = new HashMap<>();
                                paymentData.put("orderId", orderRef.getId());
                                paymentData.put("userId", uid);
                                paymentData.put("userName", name);
                                paymentData.put("userEmail", email);
                                paymentData.put("amount", total);
                                paymentData.put("status", "completed");
                                paymentData.put("method", "stripe");
                                paymentData.put("createdAt", now);

                                db.collection("payments")
                                        .add(paymentData)
                                        .addOnSuccessListener(paymentRef -> {
                                            // Clear cart after saving payment
                                            clearCart(uid);
                                        })
                                        .addOnFailureListener(e -> {
                                            // even if payment doc fails, still clear cart
                                            clearCart(uid);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this,
                                        "Failed to save order: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void clearCart(String uid) {
        db.collection("users")
                .document(uid)
                .collection("cartItems")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot d : snapshot) {
                        d.getReference().delete();
                    }
                    finish(); // back to previous screen (cart → profile/home)
                })
                .addOnFailureListener(e -> finish());
    }
}

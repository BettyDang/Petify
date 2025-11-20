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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

    // put Stripe TEST keys here
    private String PublishableKey = "xxx";
    private String SecretKey = "xxxx";

    private String CustomersURL = "https://api.stripe.com/v1/customers";
    private String EphericalKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId = null;
    private String EphericalKey;
    private String ClientSecret;

    private PaymentSheet paymentSheet;

    // Amount in cents as string
    private String Amount;
    private String Currency = "usd";  // you can change to "cad" if you want

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_payment);

        paymentButton = findViewById(R.id.payment);
        tvAmount = findViewById(R.id.tvAmount);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get total amount from cart (sent by ShoppingCartActivity)
        double total = getIntent().getDoubleExtra("totalAmount", 0.0);
        tvAmount.setText(String.format("Total: $%.2f", total));

        // Convert dollars to cents (e.g. 20.00 -> "2000")
        long amountInCents = Math.round(total * 100);
        Amount = String.valueOf(amountInCents);

        // Initialize Stripe
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        paymentButton.setOnClickListener(view -> {
            if (CustomerId != null && !CustomerId.isEmpty()) {
                paymentFlow();
            } else {
                Toast.makeText(UserPaymentActivity.this, "Customer ID is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Create customer on Stripe and continue the flow
        createCustomer();
    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        CustomerId = object.getString("id");
                        Log.d("Stripe", "Customer created: " + CustomerId);

                        if (CustomerId != null && !CustomerId.isEmpty()) {
                            getEphericalKey();
                        } else {
                            Toast.makeText(UserPaymentActivity.this, "Failed to create customer", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(UserPaymentActivity.this, "Error creating customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(UserPaymentActivity.this, "Error creating customer: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEphericalKey() {
        StringRequest request = new StringRequest(Request.Method.POST, EphericalKeyURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        EphericalKey = object.getString("id");
                        Log.d("Stripe", "Ephemeral Key created: " + EphericalKey);

                        if (EphericalKey != null && !EphericalKey.isEmpty()) {
                            getClientSecret(CustomerId, EphericalKey);
                        } else {
                            Toast.makeText(UserPaymentActivity.this, "Failed to fetch ephemeral key", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(UserPaymentActivity.this, "Error fetching ephemeral key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(UserPaymentActivity.this, "Error fetching ephemeral key: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {

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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephemeralKey) {
        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        ClientSecret = object.getString("client_secret");
                        Log.d("Stripe", "Client Secret created: " + ClientSecret);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(UserPaymentActivity.this, "Error fetching client secret: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(UserPaymentActivity.this, "Error fetching client secret: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {

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
                params.put("amount", Amount);        // dynamic amount in cents
                params.put("currency", Currency);
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void paymentFlow() {
        if (ClientSecret != null && !ClientSecret.isEmpty()) {
            paymentSheet.presentWithPaymentIntent(
                    ClientSecret,
                    new PaymentSheet.Configuration(
                            "Petify",
                            new PaymentSheet.CustomerConfiguration(
                                    CustomerId,
                                    EphericalKey
                            )
                    )
            );
        } else {
            Toast.makeText(UserPaymentActivity.this, "Client Secret not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPaymentResult(@NonNull PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            // After success: save order + clear cart + go back
            saveOrderAndClearCart();
        } else {
            Toast.makeText(this, "Payment Failed or Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderAndClearCart() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        double total = getIntent().getDoubleExtra("totalAmount", 0.0);
        long now = System.currentTimeMillis();

        // Very simple order doc (no items details here to keep code short – you can extend later)
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", uid);
        orderData.put("totalAmount", total);
        orderData.put("status", "paid");
        orderData.put("createdAt", now);

        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(orderRef -> {
                    // Clear cart items under users/{uid}/cartItems
                    db.collection("users")
                            .document(uid)
                            .collection("cartItems")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                snapshot.getDocuments().forEach(doc -> doc.getReference().delete());
                                // Back to main page
                                finish();
                            })
                            .addOnFailureListener(e -> finish());
                })
                .addOnFailureListener(e -> finish());
    }
}

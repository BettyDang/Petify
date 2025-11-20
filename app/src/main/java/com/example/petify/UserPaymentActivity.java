package com.example.petify;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentSnapshot;
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
    private EditText etAddressLine, etPostalCode, etCity, etCountry;

    // Stripe keys
    private String PublishableKey = "xxx";
    private String SecretKey = "xxx";

    private String CustomersURL    = "https://api.stripe.com/v1/customers";
    private String EphericalKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId = null;
    private String EphericalKey;
    private String ClientSecret;

    private PaymentSheet paymentSheet;

    // Amount in cents (string, for Stripe)
    private String Amount;
    private String Currency = "usd";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // address pieces
    private String addrLine, postalCode, city, country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_payment);

        paymentButton   = findViewById(R.id.payment);
        tvAmount        = findViewById(R.id.tvAmount);
        etAddressLine   = findViewById(R.id.etAddressLine);
        etPostalCode    = findViewById(R.id.etPostalCode);
        etCity          = findViewById(R.id.etCity);
        etCountry       = findViewById(R.id.etCountry);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Get total from intent
        double total = getIntent().getDoubleExtra("totalAmount", 0.0);
        tvAmount.setText(String.format("Total: $%.2f", total));

        long amountInCents = Math.round(total * 100);
        Amount = String.valueOf(amountInCents);

        // Stripe init
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        paymentButton.setOnClickListener(view -> {
            addrLine   = etAddressLine.getText().toString().trim();
            postalCode = etPostalCode.getText().toString().trim();
            city       = etCity.getText().toString().trim();
            country    = etCountry.getText().toString().trim();

            if (addrLine.isEmpty()) {
                etAddressLine.setError("Required");
                etAddressLine.requestFocus();
                return;
            }
            if (postalCode.isEmpty()) {
                etPostalCode.setError("Required");
                etPostalCode.requestFocus();
                return;
            }
            if (city.isEmpty()) {
                etCity.setError("Required");
                etCity.requestFocus();
                return;
            }
            if (country.isEmpty()) {
                etCountry.setError("Required");
                etCountry.requestFocus();
                return;
            }

            if (CustomerId != null && !CustomerId.isEmpty()) {
                paymentFlow();
            } else {
                Toast.makeText(UserPaymentActivity.this, "Customer ID is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Start Stripe flow: create customer first
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
                params.put("amount", Amount);
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
            saveOrderPaymentAndAddress();
        } else {
            Toast.makeText(this, "Payment Failed or Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderPaymentAndAddress() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        final String uid   = auth.getCurrentUser().getUid();
        final double total = getIntent().getDoubleExtra("totalAmount", 0.0);
        final long now     = System.currentTimeMillis();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {

                    String name  = doc.contains("name")  ? doc.getString("name")  : "";
                    String email = doc.contains("email") ? doc.getString("email") : auth.getCurrentUser().getEmail();

                    // Order data
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("userId", uid);
                    orderData.put("userName", name);
                    orderData.put("userEmail", email);
                    orderData.put("totalAmount", total);
                    orderData.put("status", "paid");
                    orderData.put("createdAt", now);
                    orderData.put("addressLine", addrLine);
                    orderData.put("postalCode", postalCode);
                    orderData.put("city", city);
                    orderData.put("country", country);

                    db.collection("orders")
                            .add(orderData)
                            .addOnSuccessListener(orderRef -> {

                                // Payment data
                                Map<String, Object> paymentData = new HashMap<>();
                                paymentData.put("userId", uid);
                                paymentData.put("userName", name);
                                paymentData.put("userEmail", email);
                                paymentData.put("amount", total);
                                paymentData.put("status", "completed");
                                paymentData.put("createdAt", now);
                                paymentData.put("orderId", orderRef.getId());
                                paymentData.put("method", "Stripe");
                                paymentData.put("addressLine", addrLine);
                                paymentData.put("postalCode", postalCode);
                                paymentData.put("city", city);
                                paymentData.put("country", country);

                                db.collection("payments")
                                        .add(paymentData)
                                        .addOnSuccessListener(paymentRef -> {

                                            // Save address on user profile (4 fields)
                                            Map<String, Object> addressUpdate = new HashMap<>();
                                            addressUpdate.put("addressLine", addrLine);
                                            addressUpdate.put("postalCode", postalCode);
                                            addressUpdate.put("city", city);
                                            addressUpdate.put("country", country);

                                            db.collection("users")
                                                    .document(uid)
                                                    .update(addressUpdate);

                                            // Clear cart
                                            db.collection("users")
                                                    .document(uid)
                                                    .collection("cartItems")
                                                    .get()
                                                    .addOnSuccessListener(snapshot -> {
                                                        for (QueryDocumentSnapshot d : snapshot) {
                                                            d.getReference().delete();
                                                        }
                                                        Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Order saved, but failed to clear cart", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    });

                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed to save payment: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                        );

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save order: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

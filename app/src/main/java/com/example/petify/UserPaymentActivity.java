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

    // Stripe test keys
    private final String PublishableKey =
            "xxx";
    private final String SecretKey =
            "xxx";

    private static final String CustomersURL    = "https://api.stripe.com/v1/customers";
    private static final String EphemeralKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private static final String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId  = null;
    private String EphemeralKey;
    private String ClientSecret;

    private PaymentSheet paymentSheet;

    private String Amount;
    private final String Currency = "usd";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private double totalAmount;

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

        totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);
        tvAmount.setText(String.format("Total: $%.2f", totalAmount));
        Amount = String.valueOf(Math.round(totalAmount * 100));   // dollars → cents

        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        loadUserAddress();

        paymentButton.setOnClickListener(v -> {
            if (totalAmount <= 0) {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validateAddressFields()) return;


            saveAddressToProfile(() -> {
                if (CustomerId != null && !CustomerId.isEmpty()) {
                    paymentFlow();
                } else {
                    Toast.makeText(this,
                            "Stripe customer not ready yet. Please wait a moment.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        createCustomer();
    }


    private void loadUserAddress() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    UserProfile p = doc.toObject(UserProfile.class);
                    if (p != null) {
                        if (p.getAddressLine() != null)
                            etAddressLine.setText(p.getAddressLine());
                        if (p.getPostalCode() != null)
                            etPostalCode.setText(p.getPostalCode());
                        if (p.getCity() != null)
                            etCity.setText(p.getCity());
                        if (p.getCountry() != null)
                            etCountry.setText(p.getCountry());
                    }
                });
    }

    private boolean validateAddressFields() {
        if (etAddressLine.getText().toString().trim().isEmpty()) {
            etAddressLine.setError("Required");
            etAddressLine.requestFocus();
            return false;
        }
        if (etPostalCode.getText().toString().trim().isEmpty()) {
            etPostalCode.setError("Required");
            etPostalCode.requestFocus();
            return false;
        }
        if (etCity.getText().toString().trim().isEmpty()) {
            etCity.setError("Required");
            etCity.requestFocus();
            return false;
        }
        if (etCountry.getText().toString().trim().isEmpty()) {
            etCountry.setError("Required");
            etCountry.requestFocus();
            return false;
        }
        return true;
    }

    private void saveAddressToProfile(Runnable onSaved) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("addressLine", etAddressLine.getText().toString().trim());
        data.put("postalCode", etPostalCode.getText().toString().trim());
        data.put("city",       etCity.getText().toString().trim());
        data.put("country",    etCountry.getText().toString().trim());

        db.collection("users").document(uid)
                .update(data)
                .addOnSuccessListener(unused -> onSaved.run())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save address: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }


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



    private void saveOrderAndClearCart() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String uid   = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();
        long now     = System.currentTimeMillis();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", uid);
        orderData.put("userEmail", email);
        orderData.put("totalAmount", totalAmount);
        orderData.put("status", "paid");
        orderData.put("createdAt", now);

        orderData.put("shippingAddressLine", etAddressLine.getText().toString().trim());
        orderData.put("shippingPostalCode",  etPostalCode.getText().toString().trim());
        orderData.put("shippingCity",        etCity.getText().toString().trim());
        orderData.put("shippingCountry",     etCountry.getText().toString().trim());

        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(orderRef -> {

                    Map<String, Object> paymentData = new HashMap<>();
                    paymentData.put("orderId",   orderRef.getId());
                    paymentData.put("userId",    uid);
                    paymentData.put("userEmail", email);
                    paymentData.put("amount",    totalAmount);
                    paymentData.put("status",    "completed");
                    paymentData.put("method",    "stripe");
                    paymentData.put("createdAt", now);

                    db.collection("payments")
                            .add(paymentData)
                            .addOnSuccessListener(ref -> clearCart(uid))
                            .addOnFailureListener(e -> clearCart(uid));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to save order: " + e.getMessage(),
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
                    finish();
                })
                .addOnFailureListener(e -> finish());
    }
}

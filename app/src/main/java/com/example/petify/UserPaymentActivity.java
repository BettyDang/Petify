package com.example.petify;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserPaymentActivity extends AppCompatActivity{
    private Button payment;

    private String CustomersURL = "https://api.stripe.com/v1/customers";
    private String EphericalKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId = null;  // Initialize CustomerId as null
    private String EphericalKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;
    private String Amount = "20000";  // Amount in cents
    private String Currency = "usd";  // Default currency

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_cart);

        payment = findViewById(R.id.payment);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            PaymentConfiguration.init(this, PublishableKey);
            paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

            payment.setOnClickListener(view -> {
                if (CustomerId != null && !CustomerId.isEmpty()) {
                    paymentFlow();
                } else {
                    createCustomer(uid);  // Pass Firebase UID
                }
            });

        } else {
            Toast.makeText(UserPaymentActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void createCustomer(String uid) {
        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    CustomerId = object.getString("id");
                    Log.d("Stripe", "Customer created: " + CustomerId);
                    Toast.makeText(UserPaymentActivity.this, "Customer ID: " + CustomerId, Toast.LENGTH_SHORT).show();

                    // Now that CustomerId is available, fetch the Ephemeral Key
                    if (CustomerId != null && !CustomerId.isEmpty()) {
                        getEphericalKey();
                    } else {
                        Toast.makeText(UserPaymentActivity.this, "Failed to create customer", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserPaymentActivity.this, "Error creating customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserPaymentActivity.this, "Error creating customer: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
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
        StringRequest request = new StringRequest(Request.Method.POST, EphericalKeyURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    EphericalKey = object.getString("id");
                    Log.d("Stripe", "Ephemeral Key created: " + EphericalKey);

                    // Now get the Client Secret after the Ephemeral Key is fetched
                    if (EphericalKey != null && !EphericalKey.isEmpty()) {
                        getClientSecret(CustomerId, EphericalKey);
                    } else {
                        Toast.makeText(UserPaymentActivity.this, "Failed to fetch ephemeral key", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserPaymentActivity.this, "Error fetching ephemeral key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserPaymentActivity.this, "Error fetching ephemeral key: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Stripe-Version", "2022-11-15");
                return headers;
            }

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
        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    ClientSecret = object.getString("client_secret");
                    Log.d("Stripe", "Client Secret created: " + ClientSecret);
                    Toast.makeText(UserPaymentActivity.this, "Client Secret: " + ClientSecret, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserPaymentActivity.this, "Error fetching client secret: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserPaymentActivity.this, "Error fetching client secret: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }

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
            paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration("Stripe", new PaymentSheet.CustomerConfiguration(
                    CustomerId, EphericalKey
            )));
        } else {
            Toast.makeText(UserPaymentActivity.this, "Client Secret not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            //you also need to send the firebase as well
        } else {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
        }
    }



}

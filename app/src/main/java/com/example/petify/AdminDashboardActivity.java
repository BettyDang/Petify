package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";

    private Button btnAddProductShortcut;
    private Button btnViewProducts;
    private Button btnViewOrders;
    private Button btnViewPayments;
    private Button btnViewUsers;

    private LinearLayout cardProducts;
    private LinearLayout cardOrders;
    private LinearLayout cardPayments;
    private LinearLayout cardUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Buttons
        btnAddProductShortcut = findViewById(R.id.btnAddProductShortcut);
        btnViewProducts = findViewById(R.id.btnViewProducts);
        btnViewOrders = findViewById(R.id.btnViewOrders);
        btnViewPayments = findViewById(R.id.btnViewPayments);
        btnViewUsers = findViewById(R.id.btnViewUsers);

        // Cards (optional)
        cardProducts = findViewById(R.id.cardProducts);
        cardOrders = findViewById(R.id.cardOrders);
        cardPayments = findViewById(R.id.cardPayments);
        cardUsers = findViewById(R.id.cardUsers);

        // Safety check – if something is null, log it instead of crashing
        if (btnAddProductShortcut == null || btnViewProducts == null ||
                btnViewOrders == null || btnViewPayments == null || btnViewUsers == null) {

            Log.e(TAG, "Some views are null. Check IDs in activity_admin_dashboard.xml");
            Toast.makeText(this,
                    "Layout/ID mismatch in AdminDashboardActivity",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Add Product → go to Add Product screen
        btnAddProductShortcut.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this,
                        AdminAddProductActivity.class)));

        // View Products
        btnViewProducts.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this,
                        AdminProductsActivity.class)));


        // View Orders
        btnViewOrders.setOnClickListener(v ->
                Toast.makeText(this, "Open orders list (admin)", Toast.LENGTH_SHORT).show());

        // View Payments
        btnViewPayments.setOnClickListener(v ->
                Toast.makeText(this, "Open payments list (admin)", Toast.LENGTH_SHORT).show());

        // View Users
        btnViewUsers.setOnClickListener(v ->
                Toast.makeText(this, "Open users list (admin)", Toast.LENGTH_SHORT).show());

        // Optional: cards tap the same as buttons
        if (cardProducts != null) {
            cardProducts.setOnClickListener(v -> btnViewProducts.performClick());
        }
        if (cardOrders != null) {
            cardOrders.setOnClickListener(v -> btnViewOrders.performClick());
        }
        if (cardPayments != null) {
            cardPayments.setOnClickListener(v -> btnViewPayments.performClick());
        }
        if (cardUsers != null) {
            cardUsers.setOnClickListener(v -> btnViewUsers.performClick());
        }
    }
}

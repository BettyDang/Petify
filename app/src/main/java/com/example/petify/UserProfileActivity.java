package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private Button btnAccount, btnChangePassword, btnOrders,
            btnBacktoMainPage, btnShoppingCart, btnLogout;
    private TextView tvUserName;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tvUserName = findViewById(R.id.tvUserName);
        btnAccount = findViewById(R.id.btnAccount);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnOrders = findViewById(R.id.btnOrders);
        btnBacktoMainPage = findViewById(R.id.btnBacktoMainPage);
        btnShoppingCart = findViewById(R.id.btnShoppingCart);
        btnLogout = findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserName();

        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(this, UserChangePasswordActivity.class)));

        btnShoppingCart.setOnClickListener(v ->
                startActivity(new Intent(this, ShoppingCartActivity.class)));

        // Order and payment history screens can be added later
        btnOrders.setOnClickListener(v ->
                Toast.makeText(this, "Order history not implemented yet", Toast.LENGTH_SHORT).show());



        btnBacktoMainPage.setOnClickListener(v ->
                startActivity(new Intent(this, UserHomePageActivity.class)));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthOptionsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserName() {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) {
                            name = "User";
                        }
                        tvUserName.setText("Hello, " + name);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }
}

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

    private Button btnAccount, btnChangePassword, btnOrders, btnPayments,
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
        btnPayments = findViewById(R.id.btnPayments);

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

        btnBacktoMainPage.setOnClickListener(v ->
                startActivity(new Intent(this, UserHomePageActivity.class)));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AuthOptionsActivity.class));
            finish();
        });
    }

    private void loadUserName() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvUserName.setText("Hello, " + name);
                    }
                });
    }
}

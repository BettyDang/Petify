package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private Button btnAccount, btnChangePassword, btnOrders,
            btnBacktoMainPage, btnShoppingCart, btnLogout;
    private TextView tvUserName, tvUserAddressLine, tvUserAddressMeta;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        tvUserName        = findViewById(R.id.tvUserName);
        tvUserAddressLine = findViewById(R.id.tvUserAddressLine);
        tvUserAddressMeta = findViewById(R.id.tvUserAddressMeta);

        btnAccount        = findViewById(R.id.btnAccount);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnOrders         = findViewById(R.id.btnOrders);
        btnBacktoMainPage = findViewById(R.id.btnBacktoMainPage);
        btnShoppingCart   = findViewById(R.id.btnShoppingCart);
        btnLogout         = findViewById(R.id.btnLogout);

        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(UserProfileActivity.this, EditProfileActivity.class)));

        btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(UserProfileActivity.this, UserChangePasswordActivity.class)));

        btnShoppingCart.setOnClickListener(v ->
                startActivity(new Intent(UserProfileActivity.this, ShoppingCartActivity.class)));

        btnBacktoMainPage.setOnClickListener(v ->
                startActivity(new Intent(UserProfileActivity.this, UserHomePageActivity.class)));

        // Orders button can later open user order-history activity

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent i = new Intent(UserProfileActivity.this, AuthOptionsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finishAffinity();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    UserProfile profile = doc.toObject(UserProfile.class);
                    if (profile != null) {
                        String name = profile.getName() != null ? profile.getName() : "User";
                        tvUserName.setText("Hello, " + name);

                        String line = profile.getAddressLine();
                        String pc   = profile.getPostalCode();
                        String c    = profile.getCity();
                        String co   = profile.getCountry();

                        boolean hasAny =
                                (line != null && !line.trim().isEmpty()) ||
                                        (pc != null && !pc.trim().isEmpty())   ||
                                        (c != null && !c.trim().isEmpty())    ||
                                        (co != null && !co.trim().isEmpty());

                        if (!hasAny) {
                            tvUserAddressLine.setText("No address set");
                            tvUserAddressMeta.setText("");
                        } else {
                            if (line != null && !line.trim().isEmpty()) {
                                tvUserAddressLine.setText(line);
                            } else {
                                tvUserAddressLine.setText("");
                            }

                            StringBuilder meta = new StringBuilder();
                            if (pc != null && !pc.trim().isEmpty()) {
                                meta.append(pc);
                            }
                            if (c != null && !c.trim().isEmpty()) {
                                if (meta.length() > 0) meta.append(", ");
                                meta.append(c);
                            }
                            if (co != null && !co.trim().isEmpty()) {
                                if (meta.length() > 0) meta.append(", ");
                                meta.append(co);
                            }
                            tvUserAddressMeta.setText(meta.toString());
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

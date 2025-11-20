package com.example.petify;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private Button btnSaveProfile, btnBackToProfile;
    private EditText etName, etAddressLine, etPostalCode, etCity, etCountry;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        btnSaveProfile   = findViewById(R.id.btnSaveProfile);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);
        etName           = findViewById(R.id.etName);
        etAddressLine    = findViewById(R.id.etAddressLine);
        etPostalCode     = findViewById(R.id.etPostalCode);
        etCity           = findViewById(R.id.etCity);
        etCountry        = findViewById(R.id.etCountry);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        loadCurrentData();

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnBackToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadCurrentData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    UserProfile profile = doc.toObject(UserProfile.class);
                    if (profile != null) {
                        if (profile.getName() != null)
                            etName.setText(profile.getName());
                        if (profile.getAddressLine() != null)
                            etAddressLine.setText(profile.getAddressLine());
                        if (profile.getPostalCode() != null)
                            etPostalCode.setText(profile.getPostalCode());
                        if (profile.getCity() != null)
                            etCity.setText(profile.getCity());
                        if (profile.getCountry() != null)
                            etCountry.setText(profile.getCountry());
                    }
                });
    }

    private void saveProfile() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        String name   = etName.getText().toString().trim();
        String line   = etAddressLine.getText().toString().trim();
        String pc     = etPostalCode.getText().toString().trim();
        String c      = etCity.getText().toString().trim();
        String co     = etCountry.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("addressLine", line);
        updates.put("postalCode", pc);
        updates.put("city", c);
        updates.put("country", co);

        db.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditProfileActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

package com.example.petify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoSignUp;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseUtils.getAuth();
        db = FirebaseUtils.getFirestore();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in as admin...");
        progressDialog.setCancelable(false);

        etEmail = findViewById(R.id.etAdminEmail);
        etPassword = findViewById(R.id.etAdminPassword);
        btnLogin = findViewById(R.id.btnAdminLogin);
        tvGoSignUp = findViewById(R.id.tvAdminGoSignup);

        btnLogin.setOnClickListener(v -> loginAdmin());
        tvGoSignUp.setOnClickListener(v -> {
            startActivity(new Intent(AdminLoginActivity.this, SignUpActivity.class));
        });

        TextView tvForgot = findViewById(R.id.tvAdminForgotPassword);

        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your admin email first");
                etEmail.requestFocus();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(AdminLoginActivity.this,
                                    "Reset email sent",
                                    Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(AdminLoginActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        });

    }

    private void loginAdmin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Required");
            etPassword.requestFocus();
            return;
        }

        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(this::handleAdminDoc)
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(AdminLoginActivity.this,
                                        "Error reading user data",
                                        Toast.LENGTH_LONG).show();
                                auth.signOut();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AdminLoginActivity.this,
                            "Login failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void handleAdminDoc(DocumentSnapshot doc) {
        progressDialog.dismiss();

        if (!doc.exists()) {
            Toast.makeText(this, "User record not found.", Toast.LENGTH_LONG).show();
            auth.signOut();
            return;
        }

        String role = doc.getString("role");
        if ("admin".equals(role)) {
            Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finishAffinity();
        } else {
            Toast.makeText(this,
                    "This account is not an admin account. Use User Login.",
                    Toast.LENGTH_LONG).show();
            auth.signOut();
        }
    }
}

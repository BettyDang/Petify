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

public class UserLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoSignUp;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        auth = FirebaseUtils.getAuth();
        db = FirebaseUtils.getFirestore();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        etEmail = findViewById(R.id.etUserEmail);
        etPassword = findViewById(R.id.etUserPassword);
        btnLogin = findViewById(R.id.btnUserLogin);
        tvGoSignUp = findViewById(R.id.tvUserGoSignup);

        btnLogin.setOnClickListener(v -> loginUser());
        tvGoSignUp.setOnClickListener(v -> {
            startActivity(new Intent(UserLoginActivity.this, SignUpActivity.class));
        });
        TextView tvForgot = findViewById(R.id.tvUserForgotPassword);

        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email first");
                etEmail.requestFocus();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(UserLoginActivity.this,
                                    "Reset link sent to your email",
                                    Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(UserLoginActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        });

    }

    private void loginUser() {
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
                            .addOnSuccessListener(this::handleUserDoc)
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(UserLoginActivity.this,
                                        "Error reading user data",
                                        Toast.LENGTH_LONG).show();
                                auth.signOut();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UserLoginActivity.this,
                            "Login failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void handleUserDoc(DocumentSnapshot doc) {
        progressDialog.dismiss();

        if (!doc.exists()) {
            Toast.makeText(this, "User record not found.", Toast.LENGTH_LONG).show();
            auth.signOut();
            return;
        }

        Boolean blocked = doc.getBoolean("blocked");
        if (blocked != null && blocked) {
            Toast.makeText(this,
                    "Your account has been blocked. Please contact support.",
                    Toast.LENGTH_LONG).show();
            auth.signOut();
            return;
        }

        String role = doc.getString("role");
        if ("user".equals(role)) {
            Intent intent = new Intent(UserLoginActivity.this, UserHomePageActivity.class);
            startActivity(intent);
            finishAffinity();
        } else {
            Toast.makeText(this,
                    "This account is not a user account. Use Admin Login.",
                    Toast.LENGTH_LONG).show();
            auth.signOut();
        }
    }
}

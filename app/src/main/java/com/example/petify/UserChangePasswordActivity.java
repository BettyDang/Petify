package com.example.petify;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class UserChangePasswordActivity extends AppCompatActivity {

    private EditText editOldPw, editNewPw, editConfirmNewPw;
    private Button btnSavePassword, btnBackToProfile;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_change_password);

        auth = FirebaseAuth.getInstance();

        editOldPw = findViewById(R.id.editOldPw);
        editNewPw = findViewById(R.id.editNewPw);
        editConfirmNewPw = findViewById(R.id.editConfirmNewPw);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        btnSavePassword.setOnClickListener(v -> changePassword());
        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String oldPw = editOldPw.getText().toString().trim();
        String newPw = editNewPw.getText().toString().trim();
        String confirmPw = editConfirmNewPw.getText().toString().trim();

        // basic validation
        if (TextUtils.isEmpty(oldPw)) {
            editOldPw.setError("Required");
            editOldPw.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPw)) {
            editNewPw.setError("Required");
            editNewPw.requestFocus();
            return;
        }
        if (newPw.length() < 6) {
            editNewPw.setError("At least 6 characters");
            editNewPw.requestFocus();
            return;
        }
        if (!newPw.equals(confirmPw)) {
            editConfirmNewPw.setError("Passwords do not match");
            editConfirmNewPw.requestFocus();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = auth.getCurrentUser().getEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email not available. Cannot change password.", Toast.LENGTH_LONG).show();
            return;
        }

        // re-authenticate with old password
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPw);

        btnSavePassword.setEnabled(false);

        auth.getCurrentUser().reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    // now update password
                    auth.getCurrentUser().updatePassword(newPw)
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(this,
                                        "Password updated successfully.",
                                        Toast.LENGTH_LONG).show();

                                // clear fields
                                editOldPw.setText("");
                                editNewPw.setText("");
                                editConfirmNewPw.setText("");

                                btnSavePassword.setEnabled(true);
                                finish(); // back to profile
                            })
                            .addOnFailureListener(e -> {
                                btnSavePassword.setEnabled(true);
                                Toast.makeText(this,
                                        "Failed to update password: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSavePassword.setEnabled(true);
                    // likely wrong old password
                    Toast.makeText(this,
                            "Re-authentication failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}

package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AuthOptionsActivity extends AppCompatActivity {

    private Button btnCreateAccount, btnUserLogin, btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_options);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnUserLogin = findViewById(R.id.btnUserLogin);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(AuthOptionsActivity.this, SignUpActivity.class));
        });

        btnUserLogin.setOnClickListener(v -> {
            startActivity(new Intent(AuthOptionsActivity.this, UserLoginActivity.class));
        });

        btnAdminLogin.setOnClickListener(v -> {
            startActivity(new Intent(AuthOptionsActivity.this, AdminLoginActivity.class));
        });
    }
}

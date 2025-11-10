package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    Button btnAccount, btnChangePassword, btnOrders, btnPayments, btnBacktoMainPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        btnAccount = findViewById(R.id.btnAccount);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnOrders = findViewById(R.id.btnOrders);
        btnPayments = findViewById(R.id.btnPayments);
        btnBacktoMainPage = findViewById(R.id.btnBacktoMainPage);

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UserChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        btnBacktoMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UserHomePageActivity.class);
                startActivity(intent);
            }
        });



    }
}
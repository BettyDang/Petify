package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class UserChangePasswordActivity extends AppCompatActivity {

    Button btnSavePassword, btnBackToProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_change_password);

        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        btnBackToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserChangePasswordActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });


    }
}
package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserHomePageActivity extends AppCompatActivity {

    private RecyclerView rvProduct;

    ImageView imgCart, imgProfile;
    private UserProductAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_page);

        db = FirebaseUtils.getFirestore();

        rvProduct = findViewById(R.id.rvProduct);
        rvProduct.setLayoutManager(new LinearLayoutManager(this));

        imgCart = findViewById(R.id.imgCart);
        imgProfile = findViewById(R.id.imgProfile);

        adapter = new UserProductAdapter(this);
        rvProduct.setAdapter(adapter);

        loadProducts();

        imgCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserHomePageActivity.this, ShoppingCartActivity.class);
                startActivity(intent);
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserHomePageActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadProducts() {
        db.collection("products")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Product p = doc.toObject(Product.class);
                        list.add(p);
                    }
                    adapter.setItems(list);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        UserHomePageActivity.this,
                        "Failed to load products: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }
}

package com.example.petify;

import android.os.Bundle;
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
    private UserProductAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_page);

        db = FirebaseUtils.getFirestore();

        rvProduct = findViewById(R.id.rvProduct);
        rvProduct.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserProductAdapter(this);
        rvProduct.setAdapter(adapter);

        loadProducts();
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

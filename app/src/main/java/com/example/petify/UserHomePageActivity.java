package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserHomePageActivity extends AppCompatActivity implements UserProductAdapter.OnProductActionListener {

    private RecyclerView rvProduct;
    private ImageView imgCart, imgProfile;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<Product> productList = new ArrayList<>();
    private UserProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_main_page);

        auth = FirebaseUtils.getAuth();
        db = FirebaseUtils.getFirestore();

        rvProduct = findViewById(R.id.rvProduct);
        imgCart = findViewById(R.id.imgCart);
        imgProfile = findViewById(R.id.imgProfile);
        EditText editSearch = findViewById(R.id.editSearch);

        rvProduct.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProductAdapter(this, productList, this);
        rvProduct.setAdapter(adapter);

        imgCart.setOnClickListener(v ->
                startActivity(new Intent(UserHomePageActivity.this, ShoppingCartActivity.class)));

        imgProfile.setOnClickListener(v ->
                startActivity(new Intent(UserHomePageActivity.this, UserProfileActivity.class)));

        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());
                        productList.add(p);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load products: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onAddToCart(@NonNull Product product) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        DocumentReference cartRef = db.collection("users")
                .document(uid)
                .collection("cartItems")
                .document(product.getId());

        cartRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                cartRef.update("quantity", FieldValue.increment(1));
            } else {
                CartItem newItem = new CartItem(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        product.getImageUrl(),
                        1
                );
                cartRef.set(newItem);
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

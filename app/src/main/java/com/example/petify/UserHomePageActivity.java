package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserHomePageActivity extends AppCompatActivity
        implements UserProductAdapter.OnProductActionListener {

    private RecyclerView rvProduct;
    private ImageView imgCart, imgProfile, imgFavorites;
    private EditText editSearch;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<Product> productList = new ArrayList<>();
    private final Set<String> favoriteIds = new HashSet<>();

    private UserProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_page);

        auth = FirebaseUtils.getAuth();
        db   = FirebaseUtils.getFirestore();

        rvProduct     = findViewById(R.id.rvProduct);
        imgCart       = findViewById(R.id.imgCart);
        imgProfile    = findViewById(R.id.imgProfile);
        imgFavorites  = findViewById(R.id.imgFavorites);  // IMPORTANT
        editSearch    = findViewById(R.id.editSearch);

        rvProduct.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProductAdapter(this, productList, favoriteIds, this);
        rvProduct.setAdapter(adapter);

        imgCart.setOnClickListener(v ->
                startActivity(new Intent(this, ShoppingCartActivity.class)));

        imgProfile.setOnClickListener(v ->
                startActivity(new Intent(this, UserProfileActivity.class)));

        imgFavorites.setOnClickListener(v ->
                startActivity(new Intent(this, UserFavoritesActivity.class)));

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadFavorites();
        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(q -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : q) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());
                        productList.add(p);
                    }
                    adapter.updateProducts(productList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load products: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadFavorites() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(snapshot -> {
                    favoriteIds.clear();
                    snapshot.getDocuments().forEach(doc -> favoriteIds.add(doc.getId()));
                    adapter.refreshFavorites();
                });
    }

    @Override
    public void onProductClick(@NonNull Product product) {
        Intent intent = new Intent(this, UserProductDetailActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("title", product.getTitle());
        intent.putExtra("category", product.getCategory());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("description", product.getDescription());
        intent.putExtra("imageUrl", product.getImageUrl());
        startActivity(intent);
    }

    @Override
    public void onAddToCart(@NonNull Product product) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        DocumentReference ref = db.collection("users")
                .document(uid)
                .collection("cartItems")
                .document(product.getId());

        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) ref.update("quantity", FieldValue.increment(1));
            else ref.set(new CartItem(product.getId(), product.getTitle(),
                    product.getPrice(), product.getImageUrl(), 1));

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onToggleFavorite(@NonNull Product product, boolean newFavoriteState) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        DocumentReference favRef = db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(product.getId());

        if (newFavoriteState) {
            favoriteIds.add(product.getId());
            favRef.set(new FavoriteItem(product.getId(), product.getTitle(),
                    product.getPrice(), product.getImageUrl(),
                    System.currentTimeMillis()));
        } else {
            favoriteIds.remove(product.getId());
            favRef.delete();
        }
    }


    public static class FavoriteItem {
        public String productId;
        public String title;
        public double price;
        public String imageUrl;
        public long createdAt;

        public FavoriteItem() {}

        public FavoriteItem(String productId, String title, double price,
                            String imageUrl, long createdAt) {
            this.productId = productId;
            this.title = title;
            this.price = price;
            this.imageUrl = imageUrl;
            this.createdAt = createdAt;
        }
    }
}

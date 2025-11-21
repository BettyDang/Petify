package com.example.petify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.URL;

public class UserProductDetailActivity extends AppCompatActivity {

    private ImageView imgDetailProduct;
    private TextView tvDetailTitle, tvDetailCategory, tvDetailPrice, tvDetailDescription;
    private Button btnDetailAddToCart;

    private String productId;
    private String title;
    private String category;
    private double price;
    private String imageUrl;
    private String description;


    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_product_detail);

        auth = FirebaseUtils.getAuth();
        db   = FirebaseUtils.getFirestore();

        imgDetailProduct   = findViewById(R.id.imgDetailProduct);
        tvDetailTitle      = findViewById(R.id.tvDetailTitle);
        tvDetailCategory   = findViewById(R.id.tvDetailCategory);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailPrice      = findViewById(R.id.tvDetailPrice);
        btnDetailAddToCart = findViewById(R.id.btnDetailAddToCart);

        // Read data from intent
        Intent i = getIntent();
        productId = i.getStringExtra("productId");
        title     = i.getStringExtra("title");
        category  = i.getStringExtra("category");
        price     = i.getDoubleExtra("price", 0.0);
        description = i.getStringExtra("description");
        imageUrl  = i.getStringExtra("imageUrl");

        // Bind to UI
        tvDetailTitle.setText(title);
        tvDetailCategory.setText(category);
        tvDetailDescription.setText(description);
        tvDetailPrice.setText(String.format("$%.2f", price));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    InputStream in = new URL(imageUrl).openStream();
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    imgDetailProduct.post(() -> imgDetailProduct.setImageBitmap(bmp));
                } catch (Exception e) {
                    imgDetailProduct.post(() ->
                            imgDetailProduct.setImageResource(android.R.color.darker_gray));
                }
            }).start();
        } else {
            imgDetailProduct.setImageResource(android.R.color.darker_gray);
        }

        btnDetailAddToCart.setOnClickListener(v -> addToCartAndGoToCart());
    }

    private void addToCartAndGoToCart() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        DocumentReference cartRef = db.collection("users")
                .document(uid)
                .collection("cartItems")
                .document(productId);

        cartRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // increase quantity
                cartRef.update("quantity", FieldValue.increment(1));
            } else {
                // create new cart item
                CartItem newItem = new CartItem(
                        productId,
                        title,
                        price,
                        imageUrl,
                        1
                );
                cartRef.set(newItem);
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();

            // Go to cart screen
            Intent intent = new Intent(UserProductDetailActivity.this, ShoppingCartActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

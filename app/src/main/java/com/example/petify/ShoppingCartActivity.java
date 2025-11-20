package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {

    private RecyclerView recyclerView;
    private TextView cartTotal;
    private Button btnContinueShopping, btnPayment;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        auth = FirebaseUtils.getAuth();
        db = FirebaseUtils.getFirestore();

        recyclerView = findViewById(R.id.recyclerView);
        cartTotal = findViewById(R.id.cart_total);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        btnPayment = findViewById(R.id.payment);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, cartItems, this);
        recyclerView.setAdapter(adapter);

        btnContinueShopping.setOnClickListener(v -> {
            // go back to user home
            finish();
        });

        btnPayment.setOnClickListener(v -> {
            double total = 0;
            for (CartItem item : cartItems) {
                total += item.getPrice() * item.getQuantity();
            }

            Intent intent = new Intent(ShoppingCartActivity.this, UserPaymentActivity.class);
            intent.putExtra("totalAmount", total); // in dollars
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("cartItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CartItem item = doc.toObject(CartItem.class);
                        cartItems.add(item);
                    }
                    adapter.notifyDataSetChanged();
                    updateTotal();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load cart: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        cartTotal.setText(String.format("$%.2f", total));
    }

    @Override
    public void onIncreaseQuantity(CartItem item) {
        changeQuantity(item, item.getQuantity() + 1);
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        int newQty = item.getQuantity() - 1;
        if (newQty <= 0) {
            deleteItem(item);
        } else {
            changeQuantity(item, newQty);
        }
    }

    private void changeQuantity(CartItem item, int newQty) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("cartItems")
                .document(item.getProductId())
                .update("quantity", newQty)
                .addOnSuccessListener(unused -> {
                    item.setQuantity(newQty);
                    adapter.notifyDataSetChanged();
                    updateTotal();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update quantity: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void deleteItem(CartItem item) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("cartItems")
                .document(item.getProductId())
                .delete()
                .addOnSuccessListener(unused -> {
                    cartItems.remove(item);
                    adapter.notifyDataSetChanged();
                    updateTotal();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to remove item: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

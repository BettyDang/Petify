package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity {

    private ListView lvProducts;
    private Button btnAddProduct;

    private FirebaseFirestore db;
    private final List<Product> productList = new ArrayList<>();
    private AdminProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products); // your list XML

        db = FirebaseUtils.getFirestore();

        lvProducts = findViewById(R.id.lvProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        adapter = new AdminProductAdapter(
                this,
                productList,
                new AdminProductAdapter.OnProductActionListener() {
                    @Override
                    public void onEdit(Product product) {
                        Intent intent = new Intent(AdminProductsActivity.this,
                                AdminEditProductActivity.class);
                        intent.putExtra("productId", product.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(Product product) {
                        deleteProduct(product);
                    }
                }
        );
        lvProducts.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(this, AdminAddProductActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    productList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            productList.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load products: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void deleteProduct(Product product) {
        db.collection("products")
                .document(product.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                    productList.remove(product);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Delete failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}

package com.example.petify;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AdminEditProductActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private EditText edtTitle, edtPrice, edtStock, edtCategory, edtDescription;
    private Button btnSelectImage, btnRemoveImage, btnSave;

    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    private String productId;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_editor);

        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Missing product id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        db = FirebaseUtils.getFirestore();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        imgProduct = findViewById(R.id.imgProduct);
        edtTitle = findViewById(R.id.edtTitle);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtCategory = findViewById(R.id.edtCategory);
        edtDescription = findViewById(R.id.edtDescription);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        btnSave = findViewById(R.id.btnSave);


        btnSelectImage.setOnClickListener(v ->
                Toast.makeText(this,
                        "Image change not implemented in this version",
                        Toast.LENGTH_SHORT).show());


        btnRemoveImage.setOnClickListener(v -> {
            imageUrl = null;
            imgProduct.setImageDrawable(null);  // clear preview
            Toast.makeText(this,
                    "Image removed. Tap Save to apply.",
                    Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> saveChanges());

        loadProduct();
    }


    private void loadProduct() {
        progressDialog.setMessage("Loading product...");
        progressDialog.show();

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressDialog.dismiss();
                    if (!doc.exists()) {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    Product p = doc.toObject(Product.class);
                    if (p == null) {
                        Toast.makeText(this, "Invalid product data", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    edtTitle.setText(p.getTitle());
                    edtPrice.setText(String.valueOf(p.getPrice()));
                    edtStock.setText(String.valueOf(p.getStock()));
                    edtCategory.setText(p.getCategory());
                    edtDescription.setText(p.getDescription());
                    imageUrl = p.getImageUrl();

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        loadImageFromUrl(imageUrl);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Failed to load: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }


    private void loadImageFromUrl(String urlString) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream input = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                runOnUiThread(() -> imgProduct.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void saveChanges() {
        String title = edtTitle.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String stockStr = edtStock.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Required");
            edtTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            edtPrice.setError("Required");
            edtPrice.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(stockStr)) {
            edtStock.setError("Required");
            edtStock.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(category)) {
            edtCategory.setError("Required");
            edtCategory.requestFocus();
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or stock", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Saving changes...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("price", price);
        updates.put("stock", stock);
        updates.put("category", category);
        updates.put("description", description);


        if (imageUrl == null || imageUrl.isEmpty()) {
            updates.put("imageUrl", null);
        } else {
            updates.put("imageUrl", imageUrl);
        }

        db.collection("products")
                .document(productId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    finish(); // go back to list
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Save failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}

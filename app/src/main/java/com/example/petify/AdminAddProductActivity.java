package com.example.petify;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AdminAddProductActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private Button btnSelectImage, btnSave;
    private EditText edtTitle, edtPrice, edtStock, edtCategory, edtDescription;

    private Uri selectedImageUri = null;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imgProduct.setImageURI(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);

        db = FirebaseUtils.getFirestore();
        storageRef = FirebaseStorage.getInstance().getReference("product_images");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving product...");
        progressDialog.setCancelable(false);

        imgProduct = findViewById(R.id.imgProduct);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);

        edtTitle = findViewById(R.id.edtTitle);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtCategory = findViewById(R.id.edtCategory);
        edtDescription = findViewById(R.id.edtDescription);

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
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
        if (selectedImageUri == null) {
            Toast.makeText(this, "Select an image", Toast.LENGTH_SHORT).show();
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

        progressDialog.show();

        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveProductToFirestore(title, price, stock, category, description, imageUrl);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AdminAddProductActivity.this,
                            "Image upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void saveProductToFirestore(String title, double price, int stock,
                                        String category, String description, String imageUrl) {

        String id = db.collection("products").document().getId();
        long now = System.currentTimeMillis();

        Product product = new Product(id, title, price, stock,
                category, description, imageUrl, now);

        db.collection("products")
                .document(id)
                .set(product)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(AdminAddProductActivity.this,
                            "Product added", Toast.LENGTH_SHORT).show();
                    finish(); // back to dashboard
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AdminAddProductActivity.this,
                            "Failed to save product: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}

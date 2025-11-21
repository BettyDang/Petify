package com.example.petify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserFavoritesActivity extends AppCompatActivity {

    private ListView lvFavorites;
    private Button btnBackMainPage;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<Product> favorites = new ArrayList<>();
    private FavoriteItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favorites);

        auth = FirebaseUtils.getAuth();
        db   = FirebaseUtils.getFirestore();

        lvFavorites      = findViewById(R.id.lvFavorites);
        btnBackMainPage  = findViewById(R.id.btnBackMainPage);

        adapter = new FavoriteItemAdapter(this, favorites);
        lvFavorites.setAdapter(adapter);

        btnBackMainPage.setOnClickListener(v ->
                startActivity(new Intent(UserFavoritesActivity.this, UserHomePageActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(q -> {
                    favorites.clear();
                    q.getDocuments().forEach(doc -> {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            favorites.add(p);
                        }
                    });
                    adapter.notifyDataSetChanged();
                });
    }
}

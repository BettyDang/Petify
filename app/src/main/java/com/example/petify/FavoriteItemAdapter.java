package com.example.petify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class FavoriteItemAdapter extends BaseAdapter {

    private final Context context;
    private final List<Product> favorites;
    private final LayoutInflater inflater;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FavoriteItemAdapter(Context context, List<Product> favorites) {
        this.context = context;
        this.favorites = favorites;
        this.inflater = LayoutInflater.from(context);
        this.db = FirebaseUtils.getFirestore();
        this.auth = FirebaseUtils.getAuth();
    }

    @Override
    public int getCount() {
        return favorites.size();
    }

    @Override
    public Product getItem(int position) {
        return favorites.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView imgProduct;
        ImageView btnFavorite;
        TextView title;
        TextView category;
        TextView price;
        Button btnAddToCart;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.user_item_product, parent, false);
            h = new ViewHolder();
            h.imgProduct   = convertView.findViewById(R.id.imgProductItem);
            h.title        = convertView.findViewById(R.id.tvProductTitle);
            h.category     = convertView.findViewById(R.id.tvProductCategory);
            h.price        = convertView.findViewById(R.id.tvProductPrice);
            h.btnFavorite  = convertView.findViewById(R.id.btnFavorite);
            h.btnAddToCart = convertView.findViewById(R.id.btnAddToCart);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Product p = getItem(position);

        h.title.setText(p.getTitle());
        h.category.setText(p.getCategory());
        h.price.setText("$" + String.format("%.2f", p.getPrice()));


        h.imgProduct.setImageDrawable(null);

        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            new Thread(() -> {
                try {
                    InputStream in = new URL(p.getImageUrl()).openStream();
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    new Handler(Looper.getMainLooper()).post(() ->
                            h.imgProduct.setImageBitmap(bmp)
                    );
                } catch (Exception ignored) {

                }
            }).start();
        }


        h.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);


        h.btnFavorite.setOnClickListener(v -> {
            removeFavorite(p);
            favorites.remove(p);
            notifyDataSetChanged();
        });


        h.btnAddToCart.setOnClickListener(v -> addToCart(p));

        return convertView;
    }

    private void removeFavorite(Product p) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(p.getId())
                .delete();
    }

    private void addToCart(Product p) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        DocumentReference cartRef = db.collection("users")
                .document(uid)
                .collection("cartItems")
                .document(p.getId());

        cartRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                cartRef.update("quantity", FieldValue.increment(1));
            } else {
                CartItem item = new CartItem(
                        p.getId(),
                        p.getTitle(),
                        p.getPrice(),
                        p.getImageUrl(),
                        1
                );
                cartRef.set(item);
            }
        });
    }
}

package com.example.petify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserProductAdapter extends RecyclerView.Adapter<UserProductAdapter.ProductViewHolder> {

    public interface OnProductActionListener {
        void onProductClick(@NonNull Product product);
        void onAddToCart(@NonNull Product product);
        void onToggleFavorite(@NonNull Product product, boolean newFavoriteState);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final OnProductActionListener listener;

    // full list + filtered list for search
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> visibleProducts = new ArrayList<>();

    // set of productIds that are favorite for this user
    private final Set<String> favoriteIds;

    public UserProductAdapter(Context context,
                              List<Product> initialProducts,
                              Set<String> favoriteIds,
                              OnProductActionListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.favoriteIds = favoriteIds;

        if (initialProducts != null) {
            allProducts.addAll(initialProducts);
            visibleProducts.addAll(initialProducts);
        }
    }

    // Call this when you reload products from Firestore
    public void updateProducts(List<Product> newProducts) {
        allProducts.clear();
        visibleProducts.clear();

        if (newProducts != null) {
            allProducts.addAll(newProducts);
            visibleProducts.addAll(newProducts);
        }
        notifyDataSetChanged();
    }


    public void refreshFavorites() {
        notifyDataSetChanged();
    }


    public void filter(String query) {
        visibleProducts.clear();

        if (query == null || query.trim().isEmpty()) {
            visibleProducts.addAll(allProducts);
        } else {
            String lower = query.trim().toLowerCase();
            for (Product p : allProducts) {
                if ((p.getTitle() != null && p.getTitle().toLowerCase().contains(lower)) ||
                        (p.getCategory() != null && p.getCategory().toLowerCase().contains(lower))) {
                    visibleProducts.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.user_item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = visibleProducts.get(position);

        holder.tvTitle.setText(product.getTitle());
        holder.tvCategory.setText(product.getCategory());
        holder.tvPrice.setText(String.format("$%.2f", product.getPrice()));


        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    InputStream in = new URL(imageUrl).openStream();
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    holder.imgProduct.post(() -> holder.imgProduct.setImageBitmap(bmp));
                } catch (Exception e) {
                    holder.imgProduct.post(() ->
                            holder.imgProduct.setImageResource(android.R.color.darker_gray));
                }
            }).start();
        } else {
            holder.imgProduct.setImageResource(android.R.color.darker_gray);
        }


        boolean isFav = favoriteIds != null && favoriteIds.contains(product.getId());
        updateFavoriteIcon(holder.btnFavorite, isFav);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(product);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            boolean currentlyFav = favoriteIds != null && favoriteIds.contains(product.getId());
            boolean newState = !currentlyFav;

            updateFavoriteIcon(holder.btnFavorite, newState);

            if (listener != null) listener.onToggleFavorite(product, newState);
        });
    }

    private void updateFavoriteIcon(ImageView iv, boolean isFav) {
        if (isFav) {
            iv.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            iv.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public int getItemCount() {
        return visibleProducts.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvCategory, tvPrice;
        ImageView btnFavorite;
        Button btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct   = itemView.findViewById(R.id.imgProductItem);
            tvTitle      = itemView.findViewById(R.id.tvProductTitle);
            tvCategory   = itemView.findViewById(R.id.tvProductCategory);
            tvPrice      = itemView.findViewById(R.id.tvProductPrice);
            btnFavorite  = itemView.findViewById(R.id.btnFavorite);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}

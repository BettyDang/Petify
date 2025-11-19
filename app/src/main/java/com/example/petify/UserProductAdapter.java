package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class UserProductAdapter extends RecyclerView.Adapter<UserProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> products = new ArrayList<>();

    public UserProductAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<Product> newItems) {
        products.clear();
        products.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.user_item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = products.get(position);

        holder.tvTitle.setText(p.getTitle());
        holder.tvCategory.setText(p.getCategory());
        holder.tvPrice.setText("$" + String.format("%.2f", p.getPrice()));

        String imageUrl = p.getImageUrl();

        // reset to gray placeholder first
        holder.imgProduct.setImageBitmap(null);

        if (imageUrl == null || imageUrl.isEmpty()) {
            // no image URL -> leave placeholder
            return;
        }

        // Very simple network image loader (no Glide, no extra Gradle deps)
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                java.io.InputStream input = connection.getInputStream();
                final android.graphics.Bitmap bitmap =
                        android.graphics.BitmapFactory.decodeStream(input);

                // Update ImageView on UI thread
                ((android.app.Activity) context).runOnUiThread(() ->
                        holder.imgProduct.setImageBitmap(bitmap)
                );
            } catch (Exception e) {
                e.printStackTrace();
                // if it fails, we just keep the placeholder
            }
        }).start();
    }


    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvCategory, tvPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProductItem);
            tvTitle = itemView.findViewById(R.id.tvProductTitle);
            tvCategory = itemView.findViewById(R.id.tvProductCategory);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}

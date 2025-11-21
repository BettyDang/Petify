package com.example.petify;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserProductAdapter extends RecyclerView.Adapter<UserProductAdapter.ProductViewHolder> {

    public interface OnProductActionListener {
        void onAddToCart(Product product);
    }

    private Context context;
    private List<Product> products;
    private OnProductActionListener listener;

    public UserProductAdapter(Context context, List<Product> products, OnProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvTitle.setText(product.getTitle());
        holder.tvCategory.setText(product.getCategory());
        holder.tvPrice.setText("$" + product.getPrice());

        // Load image (simple manual way)
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            new Thread(() -> {
                try {
                    java.io.InputStream in = new java.net.URL(product.getImageUrl()).openStream();
                    android.graphics.Bitmap bmp =
                            android.graphics.BitmapFactory.decodeStream(in);

                    holder.imgProduct.post(() -> holder.imgProduct.setImageBitmap(bmp));

                } catch (Exception e) {
                    holder.imgProduct.post(() ->
                            holder.imgProduct.setImageResource(android.R.color.darker_gray));
                }
            }).start();
        } else {
            holder.imgProduct.setImageResource(android.R.color.darker_gray);
        }

        // Add-to-cart button (same behavior as before)
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(product);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("title", product.getTitle());
            intent.putExtra("category", product.getCategory());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("description", product.getDescription());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvCategory, tvPrice;
        Button btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProductItem);
            tvTitle = itemView.findViewById(R.id.tvProductTitle);
            tvCategory = itemView.findViewById(R.id.tvProductCategory);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}

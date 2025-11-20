package com.example.petify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartActionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
    }

    private final Context context;
    private final List<CartItem> items;
    private final OnCartActionListener listener;

    public CartAdapter(Context context, List<CartItem> items, OnCartActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.user_item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);

        holder.tvItemName.setText(item.getTitle());
        holder.tvPrice.setText("Price: $" + item.getPrice());
        holder.etQuantity.setText(String.valueOf(item.getQuantity()));

        String url = item.getImageUrl();

        if (url != null && !url.isEmpty()) {
            holder.imgProductImage.setImageResource(android.R.color.darker_gray);

            new Thread(() -> {
                try {
                    InputStream in = new URL(url).openStream();
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    holder.imgProductImage.post(() -> holder.imgProductImage.setImageBitmap(bmp));
                } catch (Exception e) {
                    holder.imgProductImage.post(() ->
                            holder.imgProductImage.setImageResource(android.R.color.darker_gray));
                }
            }).start();
        } else {
            // If imageUrl is null/missing we show the default icon
            holder.imgProductImage.setImageResource(android.R.drawable.gallery_thumb);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onIncreaseQuantity(item);
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) listener.onDecreaseQuantity(item);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProductImage;
        TextView tvItemName, tvPrice;
        Button btnMinus, btnAdd;
        EditText etQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProductImage = itemView.findViewById(R.id.ImgProductImage);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            etQuantity.setKeyListener(null);
        }
    }
}

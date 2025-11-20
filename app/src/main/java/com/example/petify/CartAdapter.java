package com.example.petify;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Product> productList;
    private static OnItemInteractionListener listener;

    public CartAdapter(List<Product> productList, OnItemInteractionListener listener){
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, position);

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvPrice;
        ImageView ivProductImage;
        Button btnAdd, btnMinus;
        EditText etQuantity;

        public CartViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivProductImage = itemView.findViewById(R.id.ImgProductImage);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            etQuantity = itemView.findViewById(R.id.etQuantity);
        }
        @SuppressLint("NotifyDataSetChanged")
        public void bind(Product product, int position) {
            tvItemName.setText(product.getId());
            tvPrice.setText((int) product.getPrice());

            btnAdd.setOnClickListener(v -> {
                try {
                    int quantity = Integer.parseInt(etQuantity.getText().toString());
                    quantity++;
                    etQuantity.setText(String.valueOf(quantity));

                    listener.onQuantityChanged();
                } catch (NumberFormatException e) {
                    etQuantity.setText("1");
                }
            });
            btnMinus.setOnClickListener(v -> {
                try {
                    int quantity = Integer.parseInt(etQuantity.getText().toString());
                    if (quantity > 1) {
                        quantity--;
                        etQuantity.setText(String.valueOf(quantity));
                    } else if (quantity == 1) {
                        quantity = 0;
                        etQuantity.setText(String.valueOf(quantity));
                    }

                    listener.onQuantityChanged();
                } catch (NumberFormatException e) {
                    etQuantity.setText("1");
                }
            });
        }
    }

    public interface OnItemInteractionListener {
        void onItemRemoved(int position);
        void onQuantityChanged();
    }
}

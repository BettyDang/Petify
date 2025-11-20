package com.example.petify;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartActivity extends AppCompatActivity implements CartAdapter.OnItemInteractionListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<Product> productList;
    private TextView cartTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartTotal = findViewById(R.id.cart_total);

        productList = new ArrayList<>();
//        productList.add(new Product("Cat Mouse", "$19.99", R.drawable.ic_launcher_foreground));
//        productList.add(new Product("Cat Bed", "$59.99", R.drawable.ic_launcher_foreground));

        adapter = new CartAdapter(productList, this);
        recyclerView.setAdapter(adapter);

        calculateTotal();
    }

    @Override
    public void onItemRemoved(int position) {
        productList.remove(position);
        adapter.notifyItemRemoved(position);
        calculateTotal();
    }

    @Override
    public void onQuantityChanged() {
        calculateTotal();
    }
    private void calculateTotal() {
        double total = 0;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View view = recyclerView.getChildAt(i);

            EditText etQuantity = view.findViewById(R.id.etQuantity);
            String quantityText = etQuantity.getText().toString();

            int quantity = (quantityText.isEmpty()) ? 0 : Integer.parseInt(quantityText);

            TextView tvPrice = view.findViewById(R.id.tvPrice);
            String priceText = tvPrice.getText().toString().replace("$", "");
            double price = Double.parseDouble(priceText);

            total += price * quantity;
        }

        String formattedTotal = String.format("%.2f", total);
        cartTotal.setText("$" + formattedTotal);
    }


}




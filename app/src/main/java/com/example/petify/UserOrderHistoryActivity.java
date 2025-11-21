package com.example.petify;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserOrderHistoryActivity extends AppCompatActivity {

    private ListView listViewOrderHistory;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<OrderModel> orderList = new ArrayList<>();
    private UserOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order_history);

        listViewOrderHistory = findViewById(R.id.listViewOrderHistory);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseUtils.getFirestore();

        adapter = new UserOrderAdapter(this, orderList);
        listViewOrderHistory.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserOrders();
    }

    private void loadUserOrders() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("userId", uid)   // only this user's orders
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderModel order = doc.toObject(OrderModel.class);
                        order.setId(doc.getId());
                        orderList.add(order);
                    }

                    // sort by createdAt DESC on client side
                    Collections.sort(orderList,
                            (o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load orders: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}

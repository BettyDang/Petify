package com.example.petify;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private ListView lvOrders;
    private FirebaseFirestore db;
    private List<OrderModel> orderList = new ArrayList<>();
    private AdminOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        lvOrders = findViewById(R.id.lvOrders);
        db = FirebaseUtils.getFirestore();

        adapter = new AdminOrderAdapter(this, orderList);
        lvOrders.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        OrderModel order = doc.toObject(OrderModel.class);
                        order.setId(doc.getId());

                        String userId = order.getUserId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {

                                    String name = userDoc.getString("name");
                                    String email = userDoc.getString("email");

                                    order.setUserName(name);
                                    order.setUserEmail(email);

                                    adapter.notifyDataSetChanged();
                                });

                        orderList.add(order);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load orders: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}

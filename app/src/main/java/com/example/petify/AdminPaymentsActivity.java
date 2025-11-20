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

public class AdminPaymentsActivity extends AppCompatActivity {

    private ListView lvPayments;
    private FirebaseFirestore db;
    private List<PaymentModel> paymentList = new ArrayList<>();
    private AdminPaymentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payments);

        lvPayments = findViewById(R.id.lvPayments);
        db = FirebaseUtils.getFirestore();

        adapter = new AdminPaymentAdapter(this, paymentList);
        lvPayments.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPayments();
    }

    private void loadPayments() {
        db.collection("payments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    paymentList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        PaymentModel payment = doc.toObject(PaymentModel.class);
                        payment.setId(doc.getId());

                        String userId = payment.getUserId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {

                                    String name = userDoc.getString("name");
                                    String email = userDoc.getString("email");

                                    payment.setUserName(name);
                                    payment.setUserEmail(email);

                                    adapter.notifyDataSetChanged();
                                });

                        paymentList.add(payment);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load payments: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}

package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AdminPaymentAdapter extends BaseAdapter {

    private Context context;
    private List<PaymentModel> paymentList;

    public AdminPaymentAdapter(Context context, List<PaymentModel> paymentList) {
        this.context = context;
        this.paymentList = paymentList;
    }

    @Override
    public int getCount() {
        return paymentList.size();
    }

    @Override
    public Object getItem(int position) {
        return paymentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.payment_item_admin, parent, false);
        }

        TextView tvPaymentId = convertView.findViewById(R.id.tvPaymentId);
        TextView tvPaymentMeta = convertView.findViewById(R.id.tvPaymentMeta);
        TextView tvPaymentAmount = convertView.findViewById(R.id.tvPaymentAmount);
        TextView tvPaymentMethod = convertView.findViewById(R.id.tvPaymentMethod);
        TextView tvPaymentStatus = convertView.findViewById(R.id.tvPaymentStatus);

        PaymentModel payment = paymentList.get(position);

        // Payment ID
        tvPaymentId.setText("Payment #" + payment.getId());

        // Name + Email
        String meta = "";

        if (payment.getUserName() != null)
            meta += payment.getUserName();

        if (payment.getUserEmail() != null)
            meta += " • " + payment.getUserEmail();

        tvPaymentMeta.setText(meta);

        // Amount
        tvPaymentAmount.setText("Amount: $" + String.format("%.2f", payment.getAmount()));

        // Method
        tvPaymentMethod.setText("Method: Stripe");

        // Status
        tvPaymentStatus.setText("Status: " + payment.getStatus());

        return convertView;
    }

}

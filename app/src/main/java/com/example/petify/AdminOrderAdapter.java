package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AdminOrderAdapter extends BaseAdapter {

    private Context context;
    private List<OrderModel> orderList;

    public AdminOrderAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.order_item_admin, parent, false);
        }

        TextView tvOrderId = convertView.findViewById(R.id.tvOrderId);
        TextView tvOrderMeta = convertView.findViewById(R.id.tvOrderMeta);
        TextView tvOrderAmount = convertView.findViewById(R.id.tvOrderAmount);
        TextView tvOrderStatus = convertView.findViewById(R.id.tvOrderStatus);

        OrderModel order = orderList.get(position);

        // Order ID
        tvOrderId.setText("Order #" + order.getId());

        // Name + Email only
        String meta = "";

        if (order.getUserName() != null)
            meta += order.getUserName();

        if (order.getUserEmail() != null)
            meta += " • " + order.getUserEmail();

        tvOrderMeta.setText(meta);

        // Amount
        tvOrderAmount.setText("Total: $" + String.format("%.2f", order.getTotalAmount()));

        // Status
        tvOrderStatus.setText("Status: " + order.getStatus());

        return convertView;
    }

}

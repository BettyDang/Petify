package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class UserOrderAdapter extends BaseAdapter {

    private final Context context;
    private final List<OrderModel> orders;
    private final LayoutInflater inflater;

    public UserOrderAdapter(Context context, List<OrderModel> orders) {
        this.context = context;
        this.orders  = orders;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return orders != null ? orders.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tvOrderId;
        TextView tvAmountStatus;
        TextView tvAddressLine;
        TextView tvAddressMeta;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.user_item_order, parent, false);
            h = new ViewHolder();
            h.tvOrderId       = convertView.findViewById(R.id.tvUserOrderId);
            h.tvAmountStatus  = convertView.findViewById(R.id.tvUserOrderAmountStatus);
            h.tvAddressLine   = convertView.findViewById(R.id.tvUserOrderAddressLine);
            h.tvAddressMeta   = convertView.findViewById(R.id.tvUserOrderAddressMeta);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        OrderModel order = orders.get(position);

        // Short order id
        String shortId = order.getId();
        if (shortId != null && shortId.length() > 8) {
            shortId = shortId.substring(0, 8);
        }
        h.tvOrderId.setText("Order #" + shortId);

        // Total + status
        double total = order.getTotalAmount();
        String status = order.getStatus() != null ? order.getStatus() : "unknown";
        h.tvAmountStatus.setText(String.format("Total: $%.2f • Status: %s", total, status));

        // Address line
        String line = order.getShippingAddressLine();
        String pc   = order.getShippingPostalCode();
        String city = order.getShippingCity();
        String country = order.getShippingCountry();

        if (line != null && !line.trim().isEmpty()) {
            h.tvAddressLine.setText(line);
        } else {
            h.tvAddressLine.setText("No address");
        }

        StringBuilder meta = new StringBuilder();
        if (pc != null && !pc.trim().isEmpty()) {
            meta.append(pc);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (meta.length() > 0) meta.append(", ");
            meta.append(city);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (meta.length() > 0) meta.append(", ");
            meta.append(country);
        }

        h.tvAddressMeta.setText(meta.toString());

        return convertView;
    }
}

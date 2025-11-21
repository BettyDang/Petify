package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AdminOrderAdapter extends BaseAdapter {

    private final Context context;
    private final List<OrderModel> orderList;
    private final LayoutInflater inflater;

    public AdminOrderAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return orderList != null ? orderList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return orderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tvOrderId;
        TextView tvOrderMeta;
        TextView tvOrderAmount;
        TextView tvOrderAddressLine;
        TextView tvOrderAddressMeta;
        TextView tvOrderStatus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_item_admin, parent, false);
            h = new ViewHolder();
            h.tvOrderId          = convertView.findViewById(R.id.tvOrderId);
            h.tvOrderMeta        = convertView.findViewById(R.id.tvOrderMeta);
            h.tvOrderAmount      = convertView.findViewById(R.id.tvOrderAmount);
            h.tvOrderAddressLine = convertView.findViewById(R.id.tvOrderAddressLine);
            h.tvOrderAddressMeta = convertView.findViewById(R.id.tvOrderAddressMeta);
            h.tvOrderStatus      = convertView.findViewById(R.id.tvOrderStatus);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        OrderModel order = orderList.get(position);

        // Shorter order id (optional)
        String shortId = order.getId();
        if (shortId != null && shortId.length() > 8) {
            shortId = shortId.substring(0, 8);
        }
        h.tvOrderId.setText("Order #" + shortId);

        // Name + Email
        StringBuilder meta = new StringBuilder();
        if (order.getUserName() != null && !order.getUserName().trim().isEmpty()) {
            meta.append(order.getUserName());
        }
        if (order.getUserEmail() != null && !order.getUserEmail().trim().isEmpty()) {
            if (meta.length() > 0) meta.append(" • ");
            meta.append(order.getUserEmail());
        }
        h.tvOrderMeta.setText(meta.toString());

        // Amount
        h.tvOrderAmount.setText(
                String.format("Total: $%.2f", order.getTotalAmount())
        );

        // Address line
        String line = order.getShippingAddressLine();
        if (line != null && !line.trim().isEmpty()) {
            h.tvOrderAddressLine.setText(line);
        } else {
            h.tvOrderAddressLine.setText("No address");
        }

        // Postal + city + country
        String pc      = order.getShippingPostalCode();
        String city    = order.getShippingCity();
        String country = order.getShippingCountry();

        StringBuilder addrMeta = new StringBuilder();
        if (pc != null && !pc.trim().isEmpty()) {
            addrMeta.append(pc);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (addrMeta.length() > 0) addrMeta.append(", ");
            addrMeta.append(city);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (addrMeta.length() > 0) addrMeta.append(", ");
            addrMeta.append(country);
        }

        h.tvOrderAddressMeta.setText(addrMeta.toString());

        // Status
        String status = order.getStatus() != null ? order.getStatus() : "unknown";
        h.tvOrderStatus.setText("Status: " + status);

        return convertView;
    }
}

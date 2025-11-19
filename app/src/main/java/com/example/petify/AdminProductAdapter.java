package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class AdminProductAdapter extends BaseAdapter {

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private final Context context;
    private final List<Product> items;
    private final OnProductActionListener listener;
    private final LayoutInflater inflater;

    public AdminProductAdapter(Context context,
                               List<Product> items,
                               OnProductActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Product getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tvTitle, tvMeta;
        Button btnEdit, btnDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.product_list_item_admin, parent, false);
            h = new ViewHolder();
            h.tvTitle = convertView.findViewById(R.id.tvTitle);
            h.tvMeta = convertView.findViewById(R.id.tvMeta);
            h.btnEdit = convertView.findViewById(R.id.btnEdit);
            h.btnDelete = convertView.findViewById(R.id.btnDelete);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Product p = getItem(position);

        h.tvTitle.setText(p.getTitle());

        String meta = "$" + String.format("%.2f", p.getPrice())
                + " • Stock: " + p.getStock();
        h.tvMeta.setText(meta);

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });

        return convertView;
    }
}

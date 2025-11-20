package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AdminUsersAdapter extends BaseAdapter {

    private final Context context;
    private final List<UserProfile> items;
    private final LayoutInflater inflater;

    public AdminUsersAdapter(Context context, List<UserProfile> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public UserProfile getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tvUserName;
        TextView tvUserMeta;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.user_item_admin, parent, false);
            h = new ViewHolder();
            h.tvUserName = convertView.findViewById(R.id.tvUserName);
            h.tvUserMeta = convertView.findViewById(R.id.tvUserMeta);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        UserProfile u = getItem(position);

        h.tvUserName.setText(u.getName());

        String meta = u.getEmail() + " • " + u.getRole();
        h.tvUserMeta.setText(meta);

        return convertView;
    }
}

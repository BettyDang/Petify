package com.example.petify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class AdminUsersAdapter extends BaseAdapter {

    public interface OnUserBlockClickListener {
        void onBlockClick(UserProfile user);
    }

    private final Context context;
    private final List<UserProfile> items;
    private final LayoutInflater inflater;
    private final OnUserBlockClickListener blockListener;

    public AdminUsersAdapter(Context context,
                             List<UserProfile> items,
                             OnUserBlockClickListener blockListener) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.blockListener = blockListener;
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
        Button btnBlockUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.user_item_admin, parent, false);
            h = new ViewHolder();
            h.tvUserName   = convertView.findViewById(R.id.tvUserName);
            h.tvUserMeta   = convertView.findViewById(R.id.tvUserMeta);
            h.btnBlockUser = convertView.findViewById(R.id.btnBlockUser);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        UserProfile u = getItem(position);


        h.tvUserName.setText(u.getName() != null ? u.getName() : "(no name)");


        String email = u.getEmail() != null ? u.getEmail() : "(no email)";
        String role  = u.getRole()  != null ? u.getRole()  : "";
        String meta  = email;
        if (!role.isEmpty()) {
            meta = email + " • " + role;
        }
        h.tvUserMeta.setText(meta);


        if (u.isBlocked()) {
            h.btnBlockUser.setText("Unblock");
        } else {
            h.btnBlockUser.setText("Block");
        }

        h.btnBlockUser.setOnClickListener(v -> {
            if (blockListener != null) {
                blockListener.onBlockClick(u);
            }
        });

        return convertView;
    }
}

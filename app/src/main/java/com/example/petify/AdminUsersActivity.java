package com.example.petify;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private ListView lvUsers;

    private FirebaseFirestore db;
    private final List<UserProfile> users = new ArrayList<>();
    private AdminUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        db = FirebaseUtils.getFirestore();

        lvUsers = findViewById(R.id.lvUsers);


        adapter = new AdminUsersAdapter(
                this,
                users,
                this::onBlockUserClicked
        );
        lvUsers.setAdapter(adapter);


        lvUsers.setOnItemLongClickListener((parent, view, position, id) -> {
            UserProfile u = users.get(position);
            showDeleteDialog(u);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    users.clear();

                    querySnapshot.getDocuments().forEach(doc -> {
                        UserProfile u = doc.toObject(UserProfile.class);

                        if (u != null) {
                            // ensure id field exists
                            if (u.getId() == null || u.getId().isEmpty()) {
                                u.setId(doc.getId());
                            }

                            // only list normal users
                            if ("user".equalsIgnoreCase(u.getRole())) {
                                users.add(u);
                            }
                        }
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load users: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void onBlockUserClicked(UserProfile user) {
        boolean newState = !user.isBlocked();

        String docId = (user.getId() != null && !user.getId().isEmpty())
                ? user.getId()
                : user.getEmail(); // fallback

        db.collection("users")
                .document(docId)
                .update("blocked", newState)
                .addOnSuccessListener(unused -> {
                    user.setBlocked(newState);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this,
                            newState ? "User blocked" : "User unblocked",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to update user: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void showDeleteDialog(UserProfile user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete user")
                .setMessage("Delete " + user.getEmail() + " from users list?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(UserProfile user) {

        String docId = (user.getId() != null && !user.getId().isEmpty())
                ? user.getId()
                : user.getEmail();

        db.collection("users")
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    users.remove(user);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Delete failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}

package com.example.petify;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
        setContentView(R.layout.activity_admin_users); // uses your second XML

        db = FirebaseUtils.getFirestore();

        lvUsers = findViewById(R.id.lvUsers);

        adapter = new AdminUsersAdapter(this, users);
        lvUsers.setAdapter(adapter);

        // long press to delete user
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

                            // set Firestore doc ID if needed
                            if (u.getId() == null || u.getId().isEmpty()) {
                                u.setId(doc.getId());
                            }

                            // filter: ONLY show normal users
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


    private void showDeleteDialog(UserProfile user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete user")
                .setMessage("Delete " + user.getEmail() + " from users list?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(UserProfile user) {
        // This deletes the document from Firestore "users" collection.
        // It does NOT delete the Firebase Auth account.
        String docId = (user.getId() != null && !user.getId().isEmpty())
                ? user.getId()
                : user.getEmail(); // fallback if you used email as docId

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

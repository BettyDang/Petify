package com.example.petify;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtils {

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }
}

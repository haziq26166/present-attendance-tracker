package com.example.present.data.repository;

import com.example.present.data.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public void getUserById(String uid, UserCallback callback) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) callback.onSuccess(user);
                    else callback.onError("User not found");
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
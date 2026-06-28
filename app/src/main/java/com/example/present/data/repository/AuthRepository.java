package com.example.present.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.present.data.model.User;

public class AuthRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    /**
     * Updated Login: No longer takes 'expectedRole'.
     * Fetches the user role from Firestore automatically after successful auth.
     */
    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Once signed in, fetch the role from the "users" collection
                    fetchUserData(authResult.getUser().getUid(), callback::onSuccess, callback::onError);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void register(String name, String email, String password, String role, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    User newUser = new User(authResult.getUser().getUid(), name, email, role);
                    db.collection("users").document(newUser.uid).set(newUser)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(newUser))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchUserData(String uid, UserDataListener listener, ErrorListener errorListener) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        listener.onData(user);
                    } else {
                        errorListener.onError("User record not found in database.");
                    }
                })
                .addOnFailureListener(e -> errorListener.onError(e.getMessage()));
    }

    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }
    public void logout() { auth.signOut(); }

    public interface UserDataListener { void onData(User user); }
    public interface ErrorListener { void onError(String error); }
}
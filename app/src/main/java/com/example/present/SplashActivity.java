package com.example.present;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.present.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        authRepository = new AuthRepository();

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            authRepository.fetchUserData(currentUser.getUid(), user -> {
                Intent intent;
                if ("admin".equals(user.role)) {
                    intent = new Intent(SplashActivity.this, AdminMainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, UserMainActivity.class);
                }
                startActivity(intent);
                finish();
            }, error -> {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            });
        }
    }
}

package com.example.present;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.present.data.repository.AuthRepository;
import com.example.present.databinding.ActivityLoginBinding;
import com.example.present.data.model.User;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authRepository = new AuthRepository();

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);

            // NOTICE: We no longer pass the "role" string here
            authRepository.login(email, pass, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    // The 'user' object returned here already contains the role from Firestore
                    Intent intent;
                    if ("admin".equalsIgnoreCase(user.role)) {
                        intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, UserMainActivity.class);
                    }
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
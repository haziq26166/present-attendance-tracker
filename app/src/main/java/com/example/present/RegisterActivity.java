package com.example.present;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.present.data.repository.AuthRepository;
import com.example.present.databinding.ActivityRegisterBinding;
import com.example.present.data.model.User;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authRepository = new AuthRepository();

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etFullName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            String confirm = binding.etConfirmPassword.getText().toString().trim();
            String role = binding.toggleUser.isChecked() ? "user" : "admin";

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            authRepository.register(name, email, pass, role, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    Intent intent = user.role.equals("admin") ?
                            new Intent(RegisterActivity.this, AdminMainActivity.class) :
                            new Intent(RegisterActivity.this, UserMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnGoToLogin.setOnClickListener(v -> finish());
    }
}
package com.example.present.ui.admin.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.present.LoginActivity;
import com.example.present.data.repository.AuthRepository;
import com.example.present.databinding.FragmentAdminProfileBinding;

public class AdminProfileFragment extends Fragment {
    private FragmentAdminProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false);
        AuthRepository auth = new AuthRepository();

        auth.fetchUserData(auth.getCurrentUser().getUid(), user -> {
            binding.tvAdminName.setText(user.displayName);
            binding.tvAdminEmail.setText(user.email);
            binding.tvAdminInitial.setText(user.displayName.substring(0, 1).toUpperCase());
        }, e -> {});

        binding.btnAdminLogout.setOnClickListener(v -> {
            auth.logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return binding.getRoot();
    }
}

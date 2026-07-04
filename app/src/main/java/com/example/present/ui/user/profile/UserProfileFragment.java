package com.example.present.ui.user.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.present.LoginActivity;
import com.example.present.data.repository.AuthRepository;
import com.example.present.databinding.FragmentUserProfileBinding;

public class UserProfileFragment extends Fragment {
    private FragmentUserProfileBinding binding;
    private AuthRepository authRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        authRepository = new AuthRepository();

        authRepository.fetchUserData(authRepository.getCurrentUser().getUid(), user -> {
            binding.tvProfileName.setText(user.displayName);
            binding.tvProfileEmail.setText(user.email);
            String initial = user.displayName != null && !user.displayName.isEmpty()
                    ? user.displayName.substring(0, 1).toUpperCase()
                    : "U";
            binding.tvAvatarInitial.setText(initial);
        }, e -> {});

        binding.btnLogout.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return binding.getRoot();
    }
}
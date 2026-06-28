package com.example.present.ui.admin.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.present.databinding.FragmentAdminHomeBinding;

public class AdminHomeFragment extends Fragment {
    private FragmentAdminHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdminHomeViewModel viewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);

        // 1. Observe Stats - Total Events Created
        viewModel.getTotalEvents().observe(getViewLifecycleOwner(), count ->
                binding.tvTotalEventsCount.setText(String.valueOf(count)));

        // 2. Welcome Message
        viewModel.getAdminData().observe(getViewLifecycleOwner(), admin -> {
            if (admin != null) {
                binding.tvAdminWelcome.setText("Welcome, " + admin.displayName);
            }
        });

        // Note: RecyclerView logic and "Recent Activity" observers have been removed
        // to match the simplified fragment_admin_home.xml layout.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
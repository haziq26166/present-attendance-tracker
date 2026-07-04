package com.example.present.ui.user.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.present.data.model.AttendanceRecord;
import com.example.present.databinding.FragmentHistoryBinding;
import java.util.ArrayList;
import java.util.List;
import com.example.present.R;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        // Setup Adapter
        adapter = new HistoryAdapter(new ArrayList<>());
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHistory.setAdapter(adapter);

        // Setup Swipe to Refresh
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh the data
            viewModel.refreshData();
            // The refresh will be stopped when data updates
        });

        // Configure SwipeRefreshLayout colors
        binding.swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorSecondary)
        );

        // Observe Data
        viewModel.getRecords().observe(getViewLifecycleOwner(), this::handleHistoryUpdate);

        // Observe Loading State
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!isLoading) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void handleHistoryUpdate(List<AttendanceRecord> records) {
        if (records == null || records.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.swipeRefreshLayout.setVisibility(View.GONE);
            binding.tvStatTotal.setText("0");
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setVisibility(View.VISIBLE);

            // This updates the list which includes Name, Venue, Photo, and Time
            adapter.updateData(records);
            binding.tvStatTotal.setText(String.valueOf(records.size()));
        }

        // Stop refreshing if it's currently showing
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
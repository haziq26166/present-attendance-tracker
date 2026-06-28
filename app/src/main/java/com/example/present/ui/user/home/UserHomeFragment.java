package com.example.present.ui.user.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.present.databinding.FragmentUserHomeBinding;
import com.example.present.util.DateTimeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class UserHomeFragment extends Fragment {
    private FragmentUserHomeBinding binding;
    private ListenerRegistration attendanceListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserHomeViewModel viewModel = new ViewModelProvider(this).get(UserHomeViewModel.class);

        // 1. Set Today's Date
        binding.tvDate.setText(DateTimeUtils.getTodayFormatted());

        // 2. Observe User Data (Welcome Message)
        viewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null && binding != null) {
                binding.tvWelcome.setText("Hello, " + user.displayName + " 👋");
            }
        });

        // Observe Total Attended Count from ViewModel
        viewModel.getTotalAttended().observe(getViewLifecycleOwner(), count -> {
            if (binding != null) {
                binding.tvTotalAttended.setText(String.valueOf(count));
            }
        });

        // 3. START REAL-TIME ATTENDANCE TRACKING
        startAttendanceListener();

        // REMOVED: Navigation to QR Scanner via button.
        // The user should now use the dedicated "Scan" tab in the Bottom Navigation bar.
        // binding.btnScanQr.setOnClickListener(...);
    }

    /**
     * Listens for changes in the 'attendance' collection in real-time.
     * This ensures the count updates immediately after a successful scan.
     */
    private void startAttendanceListener() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        if (attendanceListener != null) {
            attendanceListener.remove();
        }

        attendanceListener = FirebaseFirestore.getInstance().collection("attendance")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("HOME_DEBUG", "Listen failed: ", e);
                        if (binding != null) binding.tvTotalAttended.setText("0");
                        return;
                    }

                    if (snapshots != null && binding != null) {
                        int count = snapshots.size();
                        binding.tvTotalAttended.setText(String.valueOf(count));
                        Log.d("HOME_DEBUG", "Attendance count updated: " + count);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (attendanceListener != null) {
            attendanceListener.remove();
        }
        binding = null;
    }
}
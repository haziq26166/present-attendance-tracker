package com.example.present.ui.admin.attendees;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.present.data.model.AttendanceRecord;
import com.example.present.databinding.FragmentAttendeeListBinding;
import com.example.present.util.DateTimeUtils;
import java.io.OutputStream;
import java.util.List;

public class AttendeeListFragment extends Fragment {
    private FragmentAttendeeListBinding binding;
    private AttendeeListViewModel viewModel;
    private String eventName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAttendeeListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AttendeeListViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Get arguments (Passed from EventListFragment)
        String eventId = getArguments() != null ? getArguments().getString("eventId") : null;
        eventName = getArguments() != null ? getArguments().getString("eventName") : "Attendees";

        binding.tvAttendeeHeader.setText(eventName);
        binding.rvAttendees.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Observe Attendee Data
        viewModel.getAttendees().observe(getViewLifecycleOwner(), records -> {
            if (records != null && !records.isEmpty()) {
                binding.rvAttendees.setVisibility(View.VISIBLE);

                // Initialize Adapter with the list of attendees
                AttendeeAdapter adapter = new AttendeeAdapter(records);
                binding.rvAttendees.setAdapter(adapter);

                // Update the counter
                binding.tvPresentCount.setText(String.valueOf(records.size()));

                // Setup Export button with the current data
                binding.btnExport.setOnClickListener(v -> exportToCSV(records));
            } else {
                binding.rvAttendees.setVisibility(View.GONE);
                binding.tvPresentCount.setText("0");
                Log.d("AttendeeList", "No records found for eventId: " + eventId);
            }
        });

        // 3. Load data from Firestore via ViewModel
        if (eventId != null) {
            viewModel.loadAttendees(eventId);
        } else {
            Toast.makeText(getContext(), "Error: Missing Event ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportToCSV(List<AttendanceRecord> records) {
        // Headers for CSV
        StringBuilder csvData = new StringBuilder("Name,Email,Check-in Time,Status\n");

        for (AttendanceRecord r : records) {
            // Format timestamp using our utility or standard date format
            String timeStr = (r.getCheckedInAt() != null)
                    ? DateTimeUtils.formatTimestamp(r.getCheckedInAt().toDate().getTime())
                    : "N/A";

            // Append record row
            csvData.append(r.getDisplayName()).append(",")
                    .append(r.getEmail()).append(",")
                    .append(timeStr).append(",")
                    .append(r.getStatus()).append("\n");
        }

        try {
            ContentValues values = new ContentValues();
            String safeName = eventName.replaceAll("\\s+", "_");
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Attendance_" + safeName + ".csv");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = requireContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            if (uri != null) {
                OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(csvData.toString().getBytes());
                    outputStream.close();
                    Toast.makeText(getContext(), "CSV Saved to Downloads", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
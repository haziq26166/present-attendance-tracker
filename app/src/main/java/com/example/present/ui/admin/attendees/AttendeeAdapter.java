package com.example.present.ui.admin.attendees;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.present.data.model.AttendanceRecord;
import com.example.present.databinding.ItemAttendeeBinding;
import com.example.present.util.DateTimeUtils;
import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {
    private final List<AttendanceRecord> list;

    public AttendeeAdapter(List<AttendanceRecord> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAttendeeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = list.get(position);

        // Ensure these methods match your AttendanceRecord.java
        holder.binding.tvAttendeeName.setText(record.getDisplayName());
        holder.binding.tvAttendeeEmail.setText(record.getEmail());

        if (record.getCheckedInAt() != null) {
            holder.binding.tvCheckInTime.setText(
                    DateTimeUtils.formatTimestamp(record.getCheckedInAt().toDate().getTime())
            );
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAttendeeBinding binding;
        ViewHolder(ItemAttendeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
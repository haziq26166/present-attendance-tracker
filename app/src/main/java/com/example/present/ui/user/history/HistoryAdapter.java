package com.example.present.ui.user.history;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.present.R;
import com.example.present.data.model.AttendanceRecord;
import com.example.present.databinding.ItemHistoryBinding;
import com.example.present.util.DateTimeUtils;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<AttendanceRecord> records;

    public HistoryAdapter(List<AttendanceRecord> records) {
        this.records = records;
    }

    public void updateData(List<AttendanceRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = records.get(position);

        // 1. Set Event Name and Venue (Place)
        // These fields are retrieved from the AttendanceRecord which is updated in real-time
        holder.binding.tvEventName.setText(record.getEventName());
        holder.binding.tvVenueName.setText(record.getVenueName());

        // 2. Format Timestamp safely
        if (record.getCheckedInAt() != null) {
            holder.binding.tvDateTime.setText(
                    DateTimeUtils.formatTimestamp(record.getCheckedInAt().toDate().getTime())
            );
        }

        // 3. Status Image (Small verification indicator)
        holder.binding.ivStatus.setImageResource("verified".equalsIgnoreCase(record.getStatus()) ?
                R.drawable.ic_verified : R.drawable.ic_flagged);

        // 4. Decode Base64 Photo (Selfie)
        String photoData = record.getPhotoUrl();
        if (photoData != null && photoData.contains("base64,")) {
            try {
                // Strip the header "data:image/jpeg;base64," if present
                String base64Image = photoData.substring(photoData.indexOf(",") + 1);
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedByte != null) {
                    holder.binding.ivRecordPhoto.setImageBitmap(decodedByte);
                } else {
                    usePlaceholder(holder);
                }
            } catch (Exception e) {
                usePlaceholder(holder);
            }
        } else {
            usePlaceholder(holder);
        }
    }

    /**
     * Helper to set a fallback icon if the selfie is missing.
     */
    private void usePlaceholder(ViewHolder holder) {
        holder.binding.ivRecordPhoto.setImageResource(android.R.drawable.ic_menu_camera);
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHistoryBinding binding;
        ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
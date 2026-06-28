package com.example.present.ui.admin.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.present.data.model.Event;
import com.example.present.databinding.BottomSheetEditEventBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditEventBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetEditEventBinding binding;
    private final Event event;
    private final Runnable onUpdateListener;

    public EditEventBottomSheet(Event event, Runnable onUpdateListener) {
        this.event = event;
        this.onUpdateListener = onUpdateListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.etEditEventName.setText(event.eventName);
        binding.etEditVenueName.setText(event.venueName);

        binding.btnSaveEdit.setOnClickListener(v -> {
            String newTitle = binding.etEditEventName.getText().toString();
            String newVenue = binding.etEditVenueName.getText().toString();

            FirebaseFirestore.getInstance().collection("events").document(event.eventId)
                    .update("title", newTitle, "venueName", newVenue)
                    .addOnSuccessListener(aVoid -> {
                        if (onUpdateListener != null) onUpdateListener.run();
                        dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
        });

        binding.btnCancelEdit.setOnClickListener(v -> dismiss());
    }
}

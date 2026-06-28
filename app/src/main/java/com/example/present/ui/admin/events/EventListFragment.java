package com.example.present.ui.admin.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.present.QRDisplayActivity;
import com.example.present.R;
import com.example.present.data.model.Event;
import com.example.present.databinding.FragmentEventListBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EventListFragment extends Fragment {
    private FragmentEventListBinding binding;
    private EventListViewModel viewModel;
    private EventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(EventListViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup RecyclerView
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(new ArrayList<>(), new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                Bundle args = new Bundle();
                args.putString("eventId", event.eventId);
                args.putString("eventName", event.eventName);
                Navigation.findNavController(requireView()).navigate(R.id.action_list_to_attendees, args);
            }

            @Override
            public void onMoreClick(Event event, View anchor) {
                showPopupMenu(event, anchor);
            }
        });
        binding.rvEvents.setAdapter(adapter);

        // 2. Observe changes
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                adapter.updateData(events);

                // Toggle empty state visibility
                if (binding.tvEmptyEvents != null) {
                    binding.tvEmptyEvents.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });

        // 3. Trigger data fetch
        viewModel.loadEvents();

        // Note: The FAB click listener has been removed to prevent navigation conflicts.
        // Users should use the Bottom Navigation Bar to "Create" events.
    }

    private void showPopupMenu(Event event, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_event_overflow, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_view_qr) {
                Intent intent = new Intent(getActivity(), QRDisplayActivity.class);
                intent.putExtra("qrPayload", event.qrPayload);
                intent.putExtra("eventName", event.eventName);
                intent.putExtra("eventDate", event.date);
                startActivity(intent);
            } else if (id == R.id.action_edit) {
                new EditEventBottomSheet(event, () -> viewModel.loadEvents()).show(getChildFragmentManager(), "EDIT_EVENT");
            } else if (id == R.id.action_delete) {
                confirmDelete(event);
            }
            return true;
        });
        popup.show();
    }

    private void confirmDelete(Event event) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Event")
                .setMessage(getString(R.string.delete_confirm))
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection("events").document(event.eventId)
                            .delete()
                            .addOnSuccessListener(aVoid -> viewModel.loadEvents())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
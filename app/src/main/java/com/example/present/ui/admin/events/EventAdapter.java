package com.example.present.ui.admin.events;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.present.data.model.Event;
import com.example.present.databinding.ItemEventBinding;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    // 1. Remove 'final' so the list can be updated
    private List<Event> eventList;
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onMoreClick(Event event, android.view.View view);
    }

    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    // 2. Add this missing method to fix the "cannot find symbol" error
    public void updateData(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.binding.tvEventTitle.setText(event.eventName);
        holder.binding.tvEventVenue.setText(event.venueName);
        holder.binding.tvEventDate.setText(event.date);

        // Handle clicks
        holder.binding.getRoot().setOnClickListener(v -> listener.onEventClick(event));
        holder.binding.btnEventMore.setOnClickListener(v -> listener.onMoreClick(event, v));
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemEventBinding binding;
        ViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
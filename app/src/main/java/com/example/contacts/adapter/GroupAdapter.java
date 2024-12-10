package com.example.contacts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<String> groups;
    private Map<String, String> groupColors;
    private Map<String, Integer> groupCounts;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(String groupName);
    }

    public GroupAdapter(List<String> groups, Map<String, String> groupColors, 
                       Map<String, Integer> groupCounts, OnGroupClickListener listener) {
        this.groups = groups;
        this.groupColors = groupColors;
        this.groupCounts = groupCounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        String groupName = groups.get(position);
        holder.bind(groupName);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateData(List<String> newGroups, Map<String, Integer> newGroupCounts) {
        this.groups = newGroups;
        this.groupCounts = newGroupCounts;
        notifyDataSetChanged();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView textGroupName;
        private final TextView textContactCount;

        GroupViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textGroupName = itemView.findViewById(R.id.textGroupName);
            textContactCount = itemView.findViewById(R.id.textContactCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onGroupClick(groups.get(position));
                }
            });
        }

        void bind(String groupName) {
            textGroupName.setText(groupName);
            int count = groupCounts.getOrDefault(groupName, 0);
            textContactCount.setText(count + " 位联系人");

            String color = groupColors.get(groupName);
            if (color != null) {
                try {
                    cardView.setStrokeColor(android.graphics.Color.parseColor(color));
                } catch (IllegalArgumentException e) {
                    cardView.setStrokeColor(android.graphics.Color.GRAY);
                }
            }
        }
    }
} 

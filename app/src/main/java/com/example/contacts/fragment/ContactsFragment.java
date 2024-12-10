package com.example.contacts.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.GroupManagementActivity;
import com.example.contacts.R;
import com.example.contacts.adapter.ContactsAdapter;
import com.example.contacts.database.ContactDbHelper;

public class ContactsFragment extends Fragment {
    private ContactDbHelper dbHelper;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new ContactDbHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter(getContext());
        recyclerView.setAdapter(adapter);

        // Set group management click event
        view.findViewById(R.id.cardManageGroups).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GroupManagementActivity.class);
            startActivity(intent);
        });

        // Update group count display
        updateGroupCount();

        return view;
    }

    private void updateGroupCount() {
        if (getView() != null) {
            TextView textGroupCount = getView().findViewById(R.id.textGroupCount);
            int groupCount = dbHelper.getAllGroups().size();
            textGroupCount.setText(String.format("%d 个分组", groupCount));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update group count when returning from group management
        updateGroupCount();
        // Refresh contact list
        if (adapter != null) {
            adapter.refreshData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 

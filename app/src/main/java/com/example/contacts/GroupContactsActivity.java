package com.example.contacts;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.ContactsAdapter;
import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.Contact;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupContactsActivity extends AppCompatActivity {
    private ContactDbHelper dbHelper;
    private ContactsAdapter adapter;
    private TextView toolbarTitle;
    private TextView contactCount;
    private TextView emptyView;
    private RecyclerView recyclerView;
    private String groupName;
    private List<Contact> allContacts;
    private String currentQuery = "";
    private SortOrder currentSortOrder = SortOrder.NAME_ASC;

    private enum SortOrder {
        NAME_ASC,
        NAME_DESC,
        COMPANY,
        RECENT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_contacts);

        // Get group name from intent
        groupName = getIntent().getStringExtra("group_name");
        if (groupName == null) {
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Initialize database helper
        dbHelper = new ContactDbHelper(this);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(this);
        recyclerView.setAdapter(adapter);

        // Load contacts
        loadContacts();
    }

    private void initializeViews() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbarTitle);
        contactCount = findViewById(R.id.contactCount);
        emptyView = findViewById(R.id.emptyView);
        recyclerView = findViewById(R.id.recyclerView);

        // Set group name and color
        toolbarTitle.setText(groupName);
        String groupColor = dbHelper.getGroupColor(groupName);
        toolbar.setBackgroundColor(Color.parseColor(groupColor));
    }

    private void loadContacts() {
        adapter.filterByGroup(groupName);
        updateContactCount(adapter.getItemCount());
        updateEmptyView(adapter.getItemCount() == 0);
    }

    private void updateContactCount(int count) {
        String countText = count + " contact" + (count != 1 ? "s" : "");
        if (!currentQuery.isEmpty()) {
            countText += " found";
        }
        contactCount.setText(countText);
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(currentQuery.isEmpty() ? 
                    "No contacts in this group" : 
                    "No contacts found");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_contacts, menu);

        // Setup search
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                adapter.filterByKeyword(newText);
                updateContactCount(adapter.getItemCount());
                updateEmptyView(adapter.getItemCount() == 0);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 

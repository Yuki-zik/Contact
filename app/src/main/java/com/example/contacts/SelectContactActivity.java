package com.example.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.ContactSelectAdapter;
import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.Contact;
import com.example.contacts.widget.SideLetterBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectContactActivity extends AppCompatActivity {
    private ContactDbHelper dbHelper;
    private ContactSelectAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SideLetterBar letterBar;
    private TextView letterHint;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        // Initialize database
        dbHelper = new ContactDbHelper(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("选择联系人");

        // Initialize views
        initializeViews();

        // Load contacts
        loadContacts();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        letterBar = findViewById(R.id.letterBar);
        letterHint = findViewById(R.id.letterHint);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new ContactSelectAdapter(contact -> {
            boolean forMessaging = getIntent().getBooleanExtra("for_messaging", false);
            if (forMessaging) {
                // If for messaging, open message detail screen
                Intent intent = new Intent(this, MessageDetailActivity.class);
                intent.putExtra("phone_number", contact.getPhone());
                intent.putExtra("contact_name", contact.getName());
                startActivity(intent);
                finish();
            } else {
                // If for general contact selection, return result
                Intent resultIntent = new Intent();
                resultIntent.putExtra("phone_number", contact.getPhone());
                resultIntent.putExtra("contact_name", contact.getName());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        recyclerView.setAdapter(adapter);

        // Setup letter bar
        letterBar.setOnLetterChangedListener(new SideLetterBar.OnLetterChangedListener() {
            @Override
            public void onLetterChanged(String letter) {
                letterHint.setText(letter);
                letterHint.setVisibility(View.VISIBLE);
                scrollToLetter(letter);
            }

            @Override
            public void onLetterGone() {
                letterHint.setVisibility(View.GONE);
            }
        });

        // Setup search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterContacts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterContacts(newText);
                return true;
            }
        });
    }

    private void loadContacts() {
        List<Contact> contacts = dbHelper.getAllContacts();
        // Sort by pinyin
        Collections.sort(contacts, (c1, c2) -> 
            c1.getPinyin().compareToIgnoreCase(c2.getPinyin()));
        adapter.setContacts(contacts);
    }

    private void scrollToLetter(String letter) {
        List<Contact> contacts = dbHelper.getAllContacts();
        for (int i = 0; i < contacts.size(); i++) {
            String firstLetter = contacts.get(i).getPinyin().substring(0, 1).toUpperCase();
            if (firstLetter.equals(letter)) {
                layoutManager.scrollToPositionWithOffset(i, 0);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 

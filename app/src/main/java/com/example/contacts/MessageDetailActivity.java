package com.example.contacts;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.MessageDetailAdapter;
import com.example.contacts.model.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MessageDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageDetailAdapter adapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private String phoneNumber;
    private String contactName;
    private TextView titleTextView;
    private static final int REQUEST_SELECT_CONTACT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        // Get phone number and contact name
        phoneNumber = getIntent().getStringExtra("phone_number");
        contactName = getIntent().getStringExtra("contact_name");

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();

        if (phoneNumber != null) {
            // Load existing conversation
            loadConversation();
            titleTextView.setText(contactName != null ? contactName : phoneNumber);
        } else {
            // New message mode
            titleTextView.setText("新建消息");
            messageInput.setEnabled(false);
            sendButton.setEnabled(false);
            
            // Show contact selection button
            FloatingActionButton fabSelectContact = findViewById(R.id.fabSelectContact);
            fabSelectContact.setVisibility(View.VISIBLE);
            fabSelectContact.setOnClickListener(v -> selectContact());
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.messageDetailRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        titleTextView = findViewById(R.id.titleTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageDetailAdapter();
        recyclerView.setAdapter(adapter);

        // Set send button click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Make title clickable to change contact
        View titleContainer = findViewById(R.id.titleContainer);
        if (titleContainer != null) {
            titleContainer.setOnClickListener(v -> selectContact());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_change_contact) {
            selectContact();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadConversation() {
        List<Message> messagesList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        
        // Query all messages with this number
        String selection = "address = ?";
        String[] selectionArgs = new String[]{phoneNumber};
        
        Cursor cursor = contentResolver.query(
                Uri.parse("content://sms"),
                new String[]{"_id", "address", "body", "date", "type", "read"},
                selection,
                selectionArgs,
                "date ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setId(cursor.getLong(0));
                message.setPhoneNumber(cursor.getString(1));
                message.setContent(cursor.getString(2));
                message.setTimestamp(cursor.getLong(3));
                message.setIncoming(cursor.getInt(4) == Telephony.Sms.MESSAGE_TYPE_INBOX);
                message.setRead(cursor.getInt(5) == 1);

                messagesList.add(message);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.setMessages(messagesList);
        
        // Scroll to latest message
        if (!messagesList.isEmpty()) {
            recyclerView.scrollToPosition(messagesList.size() - 1);
        }
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        try {
            // Create SMS intent
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
            smsIntent.putExtra("sms_body", content);
            
            // Clear input
            messageInput.setText("");
            
            // Create new message object
            Message message = new Message();
            message.setPhoneNumber(phoneNumber);
            message.setContent(content);
            message.setTimestamp(System.currentTimeMillis());
            message.setIncoming(false);
            message.setRead(true);
            
            // Update UI
            List<Message> currentMessages = new ArrayList<>(adapter.getMessages());
            currentMessages.add(message);
            adapter.setMessages(currentMessages);
            recyclerView.scrollToPosition(currentMessages.size() - 1);
            
            // Send SMS
            startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectContact() {
        Intent intent = new Intent(this, SelectContactActivity.class);
        intent.putExtra("for_messaging", true);
        startActivityForResult(intent, REQUEST_SELECT_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK && data != null) {
            String newPhoneNumber = data.getStringExtra("phone_number");
            String newContactName = data.getStringExtra("contact_name");
            
            if (newPhoneNumber != null) {
                // Update contact info
                phoneNumber = newPhoneNumber;
                contactName = newContactName;
                
                // Update UI
                titleTextView.setText(contactName != null ? contactName : phoneNumber);
                messageInput.setEnabled(true);
                sendButton.setEnabled(true);
                findViewById(R.id.fabSelectContact).setVisibility(View.GONE);
                
                // Load new conversation
                loadConversation();
            }
        }
    }
} 

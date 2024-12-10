package com.example.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.MessageAdapter;
import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.Contact;
import com.example.contacts.model.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private static final String TAG = "MessagesActivity";
    private ContactDbHelper dbHelper;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private FloatingActionButton fabSelectContact;
    private TextView emptyView;
    private long contactId = -1;
    private Contact contact;

    private final ActivityResultLauncher<Intent> selectContactLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                contactId = result.getData().getLongExtra("contact_id", -1);
                if (contactId != -1) {
                    contact = dbHelper.getContact(contactId);
                    updateTitle();
                    loadMessages();
                    fabSelectContact.setVisibility(View.GONE);
                    messageInput.setEnabled(true);
                    sendButton.setEnabled(true);
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // 初始化数据库
        dbHelper = new ContactDbHelper(this);

        // 获取联系人ID（如果有）
        contactId = getIntent().getLongExtra("contact_id", -1);
        if (contactId != -1) {
            contact = dbHelper.getContact(contactId);
        }

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateTitle();

        // 初始化视图
        recyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        fabSelectContact = findViewById(R.id.fabSelectContact);
        emptyView = findViewById(R.id.emptyView);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(new ArrayList<>(), dbHelper, message -> {
            // 标记消息为已读
            dbHelper.markMessageAsRead(message.getId());
            adapter.notifyDataSetChanged();
        });
        recyclerView.setAdapter(adapter);

        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());

        // 设置选择联系人按钮
        fabSelectContact.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectContactActivity.class);
            selectContactLauncher.launch(intent);
        });

        // 如果已有联系人，隐藏选择联系人按钮
        if (contactId != -1) {
            fabSelectContact.setVisibility(View.GONE);
            messageInput.setEnabled(true);
            sendButton.setEnabled(true);
        } else {
            messageInput.setEnabled(false);
            sendButton.setEnabled(false);
            emptyView.setText("请选择联系人");
            emptyView.setVisibility(View.VISIBLE);
        }

        // 加载消息
        loadMessages();
    }

    private void updateTitle() {
        if (contact != null) {
            getSupportActionBar().setTitle(contact.getName());
        } else {
            getSupportActionBar().setTitle("新消息");
        }
    }

    private void loadMessages() {
        if (contactId != -1) {
            List<Message> messages = dbHelper.getMessageHistory(contactId);
            adapter.updateMessages(messages);

            // 更新空视图状态
            if (messages.isEmpty()) {
                emptyView.setText("暂无消息");
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.scrollToPosition(messages.size() - 1);
            }

            // 标记所有消息为已读
            for (Message message : messages) {
                if (!message.isRead()) {
                    dbHelper.markMessageAsRead(message.getId());
                }
            }
        }
    }

    private void sendMessage() {
        if (contactId == -1) {
            Toast.makeText(this, "请先选择联系人", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        try {
            // 保存消息到数据库
            long messageId = dbHelper.insertMessage(contactId, content, false);
            if (messageId != -1) {
                // 清空输入框
                messageInput.setText("");
                // 刷新消息列表
                loadMessages();
                // 发送短信
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("sms:" + contact.getPhone()));
                intent.putExtra("sms_body", content);
                startActivity(intent);
            } else {
                Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        loadMessages();
    }
}

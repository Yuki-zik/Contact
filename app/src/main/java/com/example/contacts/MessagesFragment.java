package com.example.contacts;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.MessageAdapter;
import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.Message;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {
    private ContactDbHelper dbHelper;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton fabNewMessage;
    private MaterialTextView emptyView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadSystemMessages();
                } else {
                    Toast.makeText(getContext(), "需要短信权限来显示消息", Toast.LENGTH_SHORT).show();
                    updateEmptyView(true);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // 初始化数据库
        dbHelper = new ContactDbHelper(getContext());

        // 初始化视图
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        fabNewMessage = view.findViewById(R.id.fabNewMessage);
        emptyView = view.findViewById(R.id.emptyView);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置FAB
        fabNewMessage.setOnClickListener(v -> createNewMessage());

        // 初始化适配器
        adapter = new MessageAdapter(new ArrayList<>(), dbHelper, message -> {
            // 处理消息点击事件
            openMessageDetail(message);
        });
        recyclerView.setAdapter(adapter);

        // 检查权限并加载消息
        checkPermissionAndLoadMessages();

        return view;
    }

    private void createNewMessage() {
        Intent intent = new Intent(requireContext(), SelectContactActivity.class);
        intent.putExtra("for_messaging", true);
        startActivity(intent);
    }

    private void checkPermissionAndLoadMessages() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            loadSystemMessages();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_SMS);
        }
    }

    private void loadSystemMessages() {
        try {
            Map<String, MessageGroup> messageGroups = new HashMap<>();
            ContentResolver contentResolver = requireContext().getContentResolver();
            Cursor cursor = contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    new String[]{
                            Telephony.Sms.ADDRESS,
                            Telephony.Sms.BODY,
                            Telephony.Sms.DATE,
                            Telephony.Sms.TYPE,
                            Telephony.Sms.READ
                    },
                    null,
                    null,
                    Telephony.Sms.DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String messageBody = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE));
                    int read = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.READ));

                    boolean isIncoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX;
                    boolean isRead = read == 1;

                    MessageGroup group = messageGroups.get(phoneNumber);
                    if (group == null) {
                        group = new MessageGroup();
                        group.phoneNumber = phoneNumber;
                        group.lastMessage = messageBody;
                        group.timestamp = timestamp;
                        group.unreadCount = 0;
                        group.totalCount = 0;
                        messageGroups.put(phoneNumber, group);
                    }

                    if (isIncoming && !isRead) {
                        group.unreadCount++;
                    }
                    group.totalCount++;

                } while (cursor.moveToNext());
                cursor.close();
            }

            List<Message> messagesList = new ArrayList<>();
            for (MessageGroup group : messageGroups.values()) {
                Message message = new Message();
                message.setPhoneNumber(group.phoneNumber);
                message.setContent(group.lastMessage);
                message.setTimestamp(group.timestamp);
                message.setUnreadCount(group.unreadCount);
                message.setTotalCount(group.totalCount);
                messagesList.add(message);
            }

            Collections.sort(messagesList, (m1, m2) -> 
                Long.compare(m2.getTimestamp(), m1.getTimestamp()));

            adapter.updateMessages(messagesList);
            updateEmptyView(messagesList.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "加载消息时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            updateEmptyView(true);
        }
    }

    private void updateEmptyView(boolean isEmpty) {
        if (emptyView != null && recyclerView != null) {
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private static class MessageGroup {
        String phoneNumber;
        String lastMessage;
        long timestamp;
        int unreadCount;
        int totalCount;
    }

    private void openMessageDetail(Message message) {
        Intent intent = new Intent(requireContext(), MessageDetailActivity.class);
        intent.putExtra("phone_number", message.getPhoneNumber());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissionAndLoadMessages();
    }
} 

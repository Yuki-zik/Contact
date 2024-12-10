package com.example.contacts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.R;
import com.example.contacts.model.Contact;
import com.example.contacts.model.Message;
import com.example.contacts.database.ContactDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private final ContactDbHelper dbHelper;
    private final OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }

    public MessageAdapter(List<Message> messages, ContactDbHelper dbHelper, OnMessageClickListener listener) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        
        // 设置联系人名称（如果有的话）
        Contact contact = null;
        try {
            if (message.getContactId() > 0) {
                contact = dbHelper.getContact(message.getContactId());
            }
        } catch (Exception e) {
            // 忽略错误
        }
        
        holder.contactName.setText(contact != null ? contact.getName() : message.getPhoneNumber());
        holder.messageContent.setText(message.getContent());
        holder.messageTime.setText(formatTime(message.getTimestamp()));

        // 显示未读消息数
        if (message.getUnreadCount() > 0) {
            holder.unreadCount.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(message.getUnreadCount()));
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        // 显示总消息数
        holder.totalCount.setText(String.format("%d条消息", message.getTotalCount()));

        // 设置点击事件
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onMessageClick(message));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages != null ? newMessages : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String formatTime(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        TextView messageContent;
        TextView messageTime;
        TextView unreadCount;
        TextView totalCount;

        MessageViewHolder(View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contactName);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageTime = itemView.findViewById(R.id.messageTime);
            unreadCount = itemView.findViewById(R.id.unreadCount);
            totalCount = itemView.findViewById(R.id.totalCount);
        }
    }
} 

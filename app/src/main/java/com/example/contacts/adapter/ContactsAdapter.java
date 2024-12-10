package com.example.contacts.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.ContactDetailActivity;
import com.example.contacts.R;
import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.Contact;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private final Context context;
    private List<Contact> contacts;
    private final ContactDbHelper dbHelper;

    public ContactsAdapter(Context context) {
        this.context = context;
        this.contacts = new ArrayList<>();
        this.dbHelper = new ContactDbHelper(context);
        refreshData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.textName.setText(contact.getName());
        holder.textPhone.setText(contact.getPhone());
        holder.textGroup.setText(contact.getGroup());

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra("contact_id", contact.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void refreshData() {
        contacts = dbHelper.getAllContacts();
        notifyDataSetChanged();
    }

    public void filterByGroup(String group) {
        if (group == null || group.equals("全部")) {
            contacts = dbHelper.getAllContacts();
        } else {
            contacts = dbHelper.getContactsByGroup(group);
        }
        notifyDataSetChanged();
    }

    public void filterByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            contacts = dbHelper.getAllContacts();
        } else {
            contacts = dbHelper.searchContacts(keyword);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textName;
        TextView textPhone;
        TextView textGroup;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textName = itemView.findViewById(R.id.textName);
            textPhone = itemView.findViewById(R.id.textPhone);
            textGroup = itemView.findViewById(R.id.textGroup);
        }
    }
} 

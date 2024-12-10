package com.example.contacts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.R;
import com.example.contacts.model.Contact;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ContactSelectAdapter extends RecyclerView.Adapter<ContactSelectAdapter.ViewHolder> {
    private List<Contact> contacts;
    private List<Contact> filteredContacts;
    private final OnContactSelectedListener listener;

    public interface OnContactSelectedListener {
        void onContactSelected(Contact contact);
    }

    public ContactSelectAdapter(OnContactSelectedListener listener) {
        this.listener = listener;
        this.contacts = new ArrayList<>();
        this.filteredContacts = new ArrayList<>();
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        this.filteredContacts = new ArrayList<>(contacts);
        notifyDataSetChanged();
    }

    public void filterContacts(String query) {
        filteredContacts.clear();
        if (query == null || query.isEmpty()) {
            filteredContacts.addAll(contacts);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Contact contact : contacts) {
                if (contact.getName().toLowerCase().contains(lowerQuery) ||
                    contact.getPhone().contains(lowerQuery)) {
                    filteredContacts.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = filteredContacts.get(position);
        holder.textName.setText(contact.getName());
        holder.textPhone.setText(contact.getPhone());
        holder.cardView.setOnClickListener(v -> listener.onContactSelected(contact));
    }

    @Override
    public int getItemCount() {
        return filteredContacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textName;
        TextView textPhone;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textName = itemView.findViewById(R.id.textName);
            textPhone = itemView.findViewById(R.id.textPhone);
        }
    }
} 

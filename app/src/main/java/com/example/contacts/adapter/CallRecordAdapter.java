package com.example.contacts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.R;
import com.example.contacts.model.CallRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallRecordAdapter extends RecyclerView.Adapter<CallRecordAdapter.ViewHolder> {
    private List<CallRecord> callRecords;
    private SimpleDateFormat dateFormat;
    private OnCallButtonClickListener listener;

    public interface OnCallButtonClickListener {
        void onCallButtonClick(String phoneNumber);
    }

    public CallRecordAdapter(List<CallRecord> callRecords, OnCallButtonClickListener listener) {
        this.callRecords = callRecords;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallRecord record = callRecords.get(position);
        
        // 设置通话类型图标
        int callTypeIcon;
        switch (record.getCallType()) {
            case 0: // 来电
                callTypeIcon = R.drawable.ic_call_received;
                break;
            case 1: // 去电
                callTypeIcon = R.drawable.ic_call_made;
                break;
            case 2: // 未接
                callTypeIcon = R.drawable.ic_call_missed;
                break;
            default:
                callTypeIcon = R.drawable.ic_call;
                break;
        }
        holder.callTypeIcon.setImageResource(callTypeIcon);

        // 设置通话时间
        holder.callTime.setText(dateFormat.format(new Date(record.getCallTime())));

        // 设置通话时长
        if (record.getCallType() == 2) { // 未接电话
            holder.callDuration.setText("未接通");
        } else {
            holder.callDuration.setText(record.getFormattedDuration());
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallButtonClick(record.getPhoneNumber());
            }
        });
    }

    @Override
    public int getItemCount() {
        return callRecords.size();
    }

    public void updateData(List<CallRecord> newRecords) {
        this.callRecords = newRecords;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView callTypeIcon;
        TextView callTime;
        TextView callDuration;

        ViewHolder(View itemView) {
            super(itemView);
            callTypeIcon = itemView.findViewById(R.id.callTypeIcon);
            callTime = itemView.findViewById(R.id.callTime);
            callDuration = itemView.findViewById(R.id.callDuration);
        }
    }
} 

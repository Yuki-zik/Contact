package com.example.contacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CallLogActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CALL_LOG = 100;
    private RecyclerView callLogRecyclerView;
    private CallLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        callLogRecyclerView = findViewById(R.id.callLogRecyclerView);
        callLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CallLogAdapter();
        callLogRecyclerView.setAdapter(adapter);

        if (checkCallLogPermission()) {
            loadCallLog();
        }
    }

    private boolean checkCallLogPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    PERMISSIONS_REQUEST_READ_CALL_LOG);
            return false;
        }
        return true;
    }

    private void loadCallLog() {
        String[] projection = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        String sortOrder = CallLog.Calls.DATE + " DESC";

        try (Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            List<CallLogEntry> entries = new ArrayList<>();
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    CallLogEntry entry = new CallLogEntry(
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)),
                            cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)),
                            cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)),
                            cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                    );
                    entries.add(entry);
                } while (cursor.moveToNext());
            }
            
            adapter.setCallLogs(entries);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCallLog();
            } else {
                Toast.makeText(this, "Permission denied to read call log", Toast.LENGTH_SHORT).show();
                finish();
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

    private static class CallLogEntry {
        String number;
        int type;
        long date;
        long duration;

        CallLogEntry(String number, int type, long date, long duration) {
            this.number = number;
            this.type = type;
            this.date = date;
            this.duration = duration;
        }
    }

    private class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {
        private List<CallLogEntry> callLogs = new ArrayList<>();
        private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        void setCallLogs(List<CallLogEntry> callLogs) {
            this.callLogs = callLogs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.call_log_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CallLogEntry entry = callLogs.get(position);
            
            holder.numberView.setText(entry.number);
            holder.dateView.setText(dateFormat.format(new Date(entry.date)));
            
            // Format duration
            long minutes = TimeUnit.SECONDS.toMinutes(entry.duration);
            long seconds = entry.duration - TimeUnit.MINUTES.toSeconds(minutes);
            holder.durationView.setText(String.format(Locale.getDefault(),
                    "%d:%02d", minutes, seconds));

            // Set call type icon
            int iconRes;
            switch (entry.type) {
                case CallLog.Calls.INCOMING_TYPE:
                    iconRes = R.drawable.ic_call_received;
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    iconRes = R.drawable.ic_call_made;
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    iconRes = R.drawable.ic_call_missed;
                    break;
                default:
                    iconRes = R.drawable.ic_call;
                    break;
            }
            holder.typeIcon.setImageResource(iconRes);
        }

        @Override
        public int getItemCount() {
            return callLogs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView typeIcon;
            final TextView numberView;
            final TextView dateView;
            final TextView durationView;

            ViewHolder(View view) {
                super(view);
                typeIcon = view.findViewById(R.id.callTypeIcon);
                numberView = view.findViewById(R.id.callNumber);
                dateView = view.findViewById(R.id.callDate);
                durationView = view.findViewById(R.id.callDuration);
            }
        }
    }
} 

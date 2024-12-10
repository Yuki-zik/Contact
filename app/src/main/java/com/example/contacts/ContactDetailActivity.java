package com.example.contacts;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.CallRecordAdapter;
import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.CallRecord;
import com.example.contacts.model.Contact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class ContactDetailActivity extends AppCompatActivity {
    private static final String TAG = "ContactDetailActivity";
    private static final int RINGTONE_PICKER_REQUEST = 1;
    
    private long contactId;
    private ContactDbHelper dbHelper;
    private boolean isEditMode = false;
    private Contact currentContact;
    private Uri selectedRingtoneUri;

    private TextInputEditText editName;
    private TextInputEditText editPhone;
    private TextInputEditText editEmail;
    private TextInputEditText editCompany;
    private AutoCompleteTextView spinnerGroup;
    private MaterialTextView selectedRingtoneText;
    private MaterialTextView callHistoryEmpty;
    private RecyclerView callHistoryRecyclerView;
    private MaterialButton btnSave;
    private View fabEdit;
    private View ringtoneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // 获取联系人ID
        contactId = getIntent().getLongExtra("contact_id", -1);
        if (contactId == -1) {
            Toast.makeText(this, "无法加载联系人", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化数据库
        dbHelper = new ContactDbHelper(this);

        // 设置ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // 初始化视图
        initViews();
        
        // 加载联系人数据
        loadContactData();
        
        // 加载通话记录
        loadCallHistory();
    }

    private void initViews() {
        // 初始化基本视图
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        editCompany = findViewById(R.id.editCompany);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        selectedRingtoneText = findViewById(R.id.selectedRingtone);
        callHistoryRecyclerView = findViewById(R.id.callHistoryRecyclerView);
        callHistoryEmpty = findViewById(R.id.callHistoryEmpty);
        btnSave = findViewById(R.id.btnSave);
        fabEdit = findViewById(R.id.fabEdit);
        ringtoneLayout = findViewById(R.id.ringtoneLayout);
        
        // 设置铃声选择器点击事件
        ringtoneLayout.setOnClickListener(v -> {
            if (isEditMode) {
                openRingtonePicker();
            }
        });

        // 设置操作按钮点击事件
        findViewById(R.id.btnCall).setOnClickListener(v -> makeCall());
        findViewById(R.id.btnMessage).setOnClickListener(v -> sendMessage());
        fabEdit.setOnClickListener(v -> toggleEditMode());
        btnSave.setOnClickListener(v -> saveContact());

        // 设置通话记录列表
        callHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 加载群组数据
        loadGroups();

        // 初始设置为不可编辑
        setFieldsEditable(false);
    }

    private void openRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择联系人铃声");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

        // 设置当前选中的铃声
        if (selectedRingtoneUri != null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);
        }

        startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                selectedRingtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                updateRingtoneDisplay();
            }
        }
    }

    private void updateRingtoneDisplay() {
        if (selectedRingtoneUri == null) {
            selectedRingtoneText.setText("默认铃声");
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri);
            String ringtoneName = ringtone.getTitle(this);
            selectedRingtoneText.setText(ringtoneName);
        }
    }

    private void loadContactData() {
        try {
            currentContact = dbHelper.getContact(contactId);
            if (currentContact != null) {
                // 设置标题
                getSupportActionBar().setTitle(currentContact.getName());
                
                // 填充数据
                editName.setText(currentContact.getName());
                editPhone.setText(currentContact.getPhone());
                editEmail.setText(currentContact.getEmail());
                editCompany.setText(currentContact.getCompany());
                spinnerGroup.setText(currentContact.getGroup(), false);
                
                // 设置铃声
                String ringtoneUriString = currentContact.getRingtone();
                if (ringtoneUriString != null && !ringtoneUriString.isEmpty()) {
                    selectedRingtoneUri = Uri.parse(ringtoneUriString);
                }
                updateRingtoneDisplay();

                Log.d(TAG, "Contact data loaded successfully");
            } else {
                Log.e(TAG, "Contact not found with ID: " + contactId);
                Toast.makeText(this, "联系人不存在", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading contact data: " + e.getMessage(), e);
            Toast.makeText(this, "加载联系人数据失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        setFieldsEditable(isEditMode);
        btnSave.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        fabEdit.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        ringtoneLayout.setClickable(isEditMode);
        ringtoneLayout.setAlpha(isEditMode ? 1.0f : 0.5f);
        invalidateOptionsMenu();
    }

    private void setFieldsEditable(boolean editable) {
        editName.setEnabled(editable);
        editPhone.setEnabled(editable);
        editEmail.setEnabled(editable);
        editCompany.setEnabled(editable);
        spinnerGroup.setEnabled(editable);
    }

    private void saveContact() {
        try {
            // 获取输入值
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String company = editCompany.getText().toString().trim();
            String group = spinnerGroup.getText().toString().trim();

            // 验证必填字段
            if (name.isEmpty()) {
                editName.setError("请输入姓名");
                editName.requestFocus();
                return;
            }

            if (phone.isEmpty()) {
                editPhone.setError("请输入电话号码");
                editPhone.requestFocus();
                return;
            }

            // 更新联系人对象
            currentContact.setName(name);
            currentContact.setPhone(phone);
            currentContact.setEmail(email);
            currentContact.setCompany(company);
            currentContact.setGroup(group.isEmpty() ? "其他" : group);
            currentContact.setRingtone(selectedRingtoneUri != null ? selectedRingtoneUri.toString() : "");

            // 保存到数据库
            long result = dbHelper.updateContact(currentContact);
            if (result > 0) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                toggleEditMode();
                loadContactData(); // 重新加载数据
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving contact: " + e.getMessage(), e);
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGroups() {
        List<String> groups = dbHelper.getAllGroups();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            groups
        );
        spinnerGroup.setAdapter(adapter);
    }

    private void loadCallHistory() {
        // 从数据库加载通话记录
        List<CallRecord> callRecords = dbHelper.getCallHistoryForContact(contactId);
        
        // 创建并设置适配器
        CallRecordAdapter adapter = new CallRecordAdapter(callRecords, phoneNumber -> {
            // 处理点击拨打电话按钮的事件
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
        callHistoryRecyclerView.setAdapter(adapter);
        
        // 如果没有通话记录，显示提示信息
        if (callRecords.isEmpty()) {
            callHistoryEmpty.setVisibility(View.VISIBLE);
            callHistoryRecyclerView.setVisibility(View.GONE);
        } else {
            callHistoryEmpty.setVisibility(View.GONE);
            callHistoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void makeCall() {
        String phoneNumber = editPhone.getText().toString();
        if (!phoneNumber.isEmpty()) {
            // 记录通话
            long currentTime = System.currentTimeMillis();
            CallRecord record = new CallRecord(contactId, 1, currentTime, 0, phoneNumber);
            dbHelper.insertCallRecord(record);
            
            // 刷新通话记录显示
            loadCallHistory();
            
            // 拨打电话
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    private void sendMessage() {
        String phoneNumber = editPhone.getText().toString();
        if (!phoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("sms:" + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            toggleEditMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        editItem.setVisible(!isEditMode);
        deleteItem.setVisible(!isEditMode);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            toggleEditMode();
            return true;
        } else if (id == R.id.action_delete) {
            deleteContact();
            return true;
        } else if (id == R.id.group_family) {
            updateContactGroup("家人");
            return true;
        } else if (id == R.id.group_friends) {
            updateContactGroup("朋友");
            return true;
        } else if (id == R.id.group_work) {
            updateContactGroup("工作");
            return true;
        } else if (id == R.id.group_other) {
            updateContactGroup("其他");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteContact() {
        try {
            dbHelper.deleteContact(contactId);
            Toast.makeText(this, "联系人已删除", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting contact: " + e.getMessage(), e);
            Toast.makeText(this, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateContactGroup(String newGroup) {
        try {
            currentContact.setGroup(newGroup);
            long result = dbHelper.updateContact(currentContact);
            if (result > 0) {
                Toast.makeText(this, "分组已更新", Toast.LENGTH_SHORT).show();
                loadContactData(); // 重新加载数据以显示更新
                setResult(RESULT_OK);
            } else {
                Toast.makeText(this, "更新分组失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating group: " + e.getMessage(), e);
            Toast.makeText(this, "更新分组失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 

package com.example.contacts;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.Contact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = "AddContactActivity";
    private TextInputEditText editName;
    private TextInputEditText editPhone;
    private TextInputEditText editCompany;
    private TextInputEditText editEmail;
    private AutoCompleteTextView spinnerGroup;
    private MaterialButton btnSave;
    private ContactDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception:", throwable);
        });
        
        Log.e(TAG, "===== Activity onCreate START =====");
        try {
            super.onCreate(savedInstanceState);
            Log.e(TAG, "super.onCreate completed");
            
            // 检查布局文件是否存在
            int layoutId = getResources().getIdentifier("activity_add_contact", "layout", getPackageName());
            Log.e(TAG, "Layout resource found: " + (layoutId != 0));
            
            setContentView(R.layout.activity_add_contact);
            Log.e(TAG, "setContentView completed");
            
            // 检查布局文件是否正确加载
            View rootView = findViewById(android.R.id.content);
            Log.e(TAG, "Root view found: " + (rootView != null));
            
            if (rootView == null) {
                throw new IllegalStateException("Root view is null after setContentView");
            }
            
            // 检查必需的视图ID是否在布局中定义
            int editNameId = getResources().getIdentifier("editName", "id", getPackageName());
            int editPhoneId = getResources().getIdentifier("editPhone", "id", getPackageName());
            int spinnerGroupId = getResources().getIdentifier("spinnerGroup", "id", getPackageName());
            int btnSaveId = getResources().getIdentifier("btnSave", "id", getPackageName());
            
            Log.e(TAG, String.format("Resource IDs found - editName: %b, editPhone: %b, spinnerGroup: %b, btnSave: %b",
                editNameId != 0, editPhoneId != 0, spinnerGroupId != 0, btnSaveId != 0));
            
            // 初始化数据库前检查上下文
            Log.e(TAG, "Context available: " + (this != null));
            dbHelper = new ContactDbHelper(this);
            Log.e(TAG, "Database helper initialized: " + (dbHelper != null));

            // 设置工具栏
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("添加联系人");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
                Log.d(TAG, "Toolbar setup completed");
            } else {
                Log.e(TAG, "Toolbar not found in layout");
            }

            // 初始化视图和逻辑
            initViews();
            setupListeners();
            Log.d(TAG, "Views and listeners initialized");
        } catch (Exception e) {
            Log.e(TAG, "===== CRASH in onCreate =====");
            Log.e(TAG, "Error type: " + e.getClass().getName());
            Log.e(TAG, "Error message: " + e.getMessage());
            Log.e(TAG, "Stack trace:", e);
            
            // 检查是否是资源相关的错误
            if (e instanceof android.content.res.Resources.NotFoundException) {
                Log.e(TAG, "Resource not found error. Checking layout file...");
                try {
                    getResources().getLayout(R.layout.activity_add_contact);
                } catch (Exception e2) {
                    Log.e(TAG, "Layout file check failed:", e2);
                }
            }
            
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        Log.e(TAG, "===== Activity onCreate END =====");
    }

    private void initViews() {
        Log.d(TAG, "initViews started");
        try {
            // 记录每个视图的查找结果
            editName = findViewById(R.id.editName);
            Log.d(TAG, "editName found: " + (editName != null));
            
            editPhone = findViewById(R.id.editPhone);
            Log.d(TAG, "editPhone found: " + (editPhone != null));
            
            editCompany = findViewById(R.id.editCompany);
            Log.d(TAG, "editCompany found: " + (editCompany != null));
            
            editEmail = findViewById(R.id.editEmail);
            Log.d(TAG, "editEmail found: " + (editEmail != null));
            
            spinnerGroup = findViewById(R.id.spinnerGroup);
            Log.d(TAG, "spinnerGroup found: " + (spinnerGroup != null));
            
            btnSave = findViewById(R.id.btnSave);
            Log.d(TAG, "btnSave found: " + (btnSave != null));

            // 检查所有必需的视图是否都成功初始化
            if (editName == null || editPhone == null || spinnerGroup == null || btnSave == null) {
                String missingViews = "";
                if (editName == null) missingViews += "editName ";
                if (editPhone == null) missingViews += "editPhone ";
                if (spinnerGroup == null) missingViews += "spinnerGroup ";
                if (btnSave == null) missingViews += "btnSave ";
                
                throw new IllegalStateException("Missing essential views: " + missingViews.trim());
            }

            loadGroups();
            Log.d(TAG, "initViews completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "初始化视图失败: ", e);
            String errorMsg = "错误类型: " + e.getClass().getName() + "\n" +
                             "错误信息: " + e.getMessage() + "\n" +
                             "出错位置: " + (e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "未知");
            Log.e(TAG, errorMsg);
            Toast.makeText(this, "界面初始化失败: " + errorMsg, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners started");
        try {
            btnSave.setOnClickListener(v -> {
                Log.d(TAG, "Save button clicked");
                saveContact();
            });
            Log.d(TAG, "Listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in setupListeners: " + e.getMessage(), e);
        }
    }

    private void loadGroups() {
        Log.d(TAG, "loadGroups started");
        try {
            if (dbHelper == null) {
                throw new IllegalStateException("Database helper is null");
            }
            
            // 获取所有群组
            List<String> groups = dbHelper.getAllGroups();
            Log.d(TAG, "Groups from database: " + (groups != null ? groups.toString() : "null"));
            
            if (groups == null || groups.isEmpty()) {
                groups = new ArrayList<>();
                groups.add("其他");
                Log.d(TAG, "Using default group");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                groups
            );
            spinnerGroup.setAdapter(adapter);
            
            // 设置默认分组为"其他"
            spinnerGroup.setText("其他", false);
            
            Log.d(TAG, "Groups loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "加载群组失败: ", e);
            String errorMsg = "错误类型: " + e.getClass().getName() + "\n" +
                             "错误信息: " + e.getMessage() + "\n" +
                             "出错位置: " + (e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "未知");
            Log.e(TAG, errorMsg);
            
            // 使用默认群组并继续
            try {
                List<String> defaultGroups = Collections.singletonList("其他");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    defaultGroups
                );
                spinnerGroup.setAdapter(adapter);
                spinnerGroup.setText("其他", false);
                Toast.makeText(this, "加载分组失败，使用默认分组\n" + errorMsg, Toast.LENGTH_LONG).show();
            } catch (Exception e2) {
                Log.e(TAG, "设置默认群组也失败: ", e2);
            }
        }
    }

    private void saveContact() {
        Log.d(TAG, "saveContact started");
        try {
            if (dbHelper == null) {
                Toast.makeText(this, "数据库未初始化", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database helper is null");
                return;
            }

            // 获取输入值
            String name = editName.getText() != null ? editName.getText().toString().trim() : "";
            String phone = editPhone.getText() != null ? editPhone.getText().toString().trim() : "";
            String company = editCompany.getText() != null ? editCompany.getText().toString().trim() : "";
            String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
            String group = spinnerGroup.getText() != null ? spinnerGroup.getText().toString().trim() : "";

            Log.d(TAG, "Input values - Name: " + name + ", Phone: " + phone + 
                      ", Company: " + company + ", Email: " + email + ", Group: " + group);

            // 验证必填字段
            if (name.isEmpty()) {
                editName.setError("请输入姓名");
                editName.requestFocus();
                Log.d(TAG, "Name validation failed");
                return;
            }

            if (phone.isEmpty()) {
                editPhone.setError("请输入电话号码");
                editPhone.requestFocus();
                Log.d(TAG, "Phone validation failed");
                return;
            }

            // 验证手机号格式
            if (!isValidPhoneNumber(phone)) {
                editPhone.setError("请输入有效的电话号码");
                editPhone.requestFocus();
                Log.d(TAG, "Phone number format validation failed");
                return;
            }

            // 验证邮箱格式
            if (!email.isEmpty() && !isValidEmail(email)) {
                editEmail.setError("请输入有效的邮箱地址");
                editEmail.requestFocus();
                Log.d(TAG, "Email format validation failed");
                return;
            }

            // 创建联系人对象
            Contact contact = new Contact();
            contact.setName(name);
            contact.setPhone(phone);
            contact.setCompany(company);
            contact.setEmail(email);
            contact.setGroup(group.isEmpty() ? "其他" : group);
            contact.setRingtone("default");

            Log.d(TAG, "Contact object created");

            // 保存到数据库
            long newContactId = dbHelper.insertContact(contact);
            Log.d(TAG, "Database insert result: " + newContactId);

            if (newContactId != -1) {
                Toast.makeText(this, "联系人添加成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                Log.d(TAG, "Contact saved successfully");
                finish();
            } else {
                Log.e(TAG, "Failed to insert contact into database");
                Toast.makeText(this, "联系人添加失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving contact: " + e.getMessage(), e);
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        // 允许数字、加号、空格和连字符
        String cleaned = phone.replaceAll("[\\s-]", "");
        return cleaned.matches("^[+]?\\d{11}$") || // 中国手机号
               cleaned.matches("^[+]?\\d{7,8}$") || // 座机号码
               cleaned.matches("^[+]?\\d{10,15}$"); // 其他格式
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 

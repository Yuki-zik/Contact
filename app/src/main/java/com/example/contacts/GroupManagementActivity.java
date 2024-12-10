package com.example.contacts;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contacts.adapter.GroupAdapter;
import com.example.contacts.database.ContactDbHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManagementActivity extends AppCompatActivity {
    private ContactDbHelper dbHelper;
    private GroupAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        // 初始化数据库
        dbHelper = new ContactDbHelper(this);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("分组管理");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 设置FAB点击事件
        findViewById(R.id.fabAddGroup).setOnClickListener(v -> showAddGroupDialog());

        // 加载分组数据
        loadGroups();
    }

    private void loadGroups() {
        List<String> groups = dbHelper.getAllGroups();
        Map<String, String> groupColors = new HashMap<>();
        Map<String, Integer> groupCounts = new HashMap<>();

        for (String group : groups) {
            groupColors.put(group, dbHelper.getGroupColor(group));
            groupCounts.put(group, dbHelper.getContactCountInGroup(group));
        }

        if (adapter == null) {
            adapter = new GroupAdapter(groups, groupColors, groupCounts, this::showGroupOptionsDialog);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(groups, groupCounts);
        }
    }

    private void showAddGroupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_group, null);
        EditText editGroupName = dialogView.findViewById(R.id.editGroupName);

        new MaterialAlertDialogBuilder(this)
                .setTitle("添加新分组")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String groupName = editGroupName.getText().toString().trim();
                    if (!groupName.isEmpty()) {
                        if (dbHelper.isGroupNameExists(groupName)) {
                            Toast.makeText(this, "分组名称已存在", Toast.LENGTH_SHORT).show();
                        } else {
                            long result = dbHelper.addGroup(groupName, "#9E9E9E");
                            if (result != -1) {
                                Toast.makeText(this, "分组添加成功", Toast.LENGTH_SHORT).show();
                                loadGroups();
                            } else {
                                Toast.makeText(this, "分组添加失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showGroupOptionsDialog(String groupName) {
        if ("其他".equals(groupName)) {
            Toast.makeText(this, "默认分组不能修改", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"编辑名称", "删除分组"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(groupName)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditGroupDialog(groupName);
                    } else {
                        showDeleteGroupConfirmDialog(groupName);
                    }
                })
                .show();
    }

    private void showEditGroupDialog(String oldName) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_group, null);
        EditText editGroupName = dialogView.findViewById(R.id.editGroupName);
        editGroupName.setText(oldName);
        editGroupName.setSelection(oldName.length());

        new MaterialAlertDialogBuilder(this)
                .setTitle("编辑分组名称")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newName = editGroupName.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(oldName)) {
                        if (dbHelper.isGroupNameExists(newName)) {
                            Toast.makeText(this, "分组名称已存在", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean success = dbHelper.updateGroupName(oldName, newName);
                            if (success) {
                                Toast.makeText(this, "分组已重命名", Toast.LENGTH_SHORT).show();
                                loadGroups();
                            } else {
                                Toast.makeText(this, "重命名失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteGroupConfirmDialog(String groupName) {
        if ("其他".equals(groupName)) {
            Toast.makeText(this, "默认分组不能删除", Toast.LENGTH_SHORT).show();
            return;
        }

        int contactCount = dbHelper.getContactCountInGroup(groupName);
        String message = String.format("确定要删除分组 \"%s\" 吗？\n该分组中的 %d 位联系人将被移动到默认分组。",
                groupName, contactCount);

        new MaterialAlertDialogBuilder(this)
                .setTitle("删除分组")
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> {
                    if (dbHelper.deleteGroup(groupName)) {
                        Toast.makeText(this, "分组已删除", Toast.LENGTH_SHORT).show();
                        loadGroups(); // 刷新列表
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 

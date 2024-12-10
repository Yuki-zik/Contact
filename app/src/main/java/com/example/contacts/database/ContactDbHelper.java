package com.example.contacts.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.contacts.model.Contact;
import com.example.contacts.model.CallRecord;
import com.example.contacts.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "ContactDbHelper";
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 3;

    // Tables
    public static final String TABLE_CONTACTS = "contacts";
    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_CALL_HISTORY = "call_history";
    
    // Contacts columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_COMPANY = "company";
    public static final String COLUMN_GROUP = "contact_group";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_RINGTONE = "ringtone";

    // Messages columns
    public static final String COLUMN_MESSAGE_ID = "_id";
    public static final String COLUMN_CONTACT_ID = "contact_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_IS_INCOMING = "is_incoming";
    public static final String COLUMN_IS_READ = "is_read";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";

    // Groups columns
    public static final String COLUMN_GROUP_ID = "_id";
    public static final String COLUMN_GROUP_NAME = "name";
    public static final String COLUMN_GROUP_COLOR = "color";

    // Call History columns
    public static final String COLUMN_CALL_ID = "_id";
    public static final String COLUMN_CALL_CONTACT_ID = "contact_id";
    public static final String COLUMN_CALL_TYPE = "call_type";  // 0: incoming, 1: outgoing, 2: missed
    public static final String COLUMN_CALL_DURATION = "duration";
    public static final String COLUMN_CALL_TIMESTAMP = "timestamp";
    public static final String COLUMN_CALL_PHONE = "phone_number";

    private static final String SQL_CREATE_CONTACTS =
            "CREATE TABLE " + TABLE_CONTACTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_PHONE + " TEXT NOT NULL, " +
                    COLUMN_COMPANY + " TEXT, " +
                    COLUMN_GROUP + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_RINGTONE + " TEXT" +
                    ")";

    private static final String SQL_CREATE_MESSAGES =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CONTACT_ID + " INTEGER, " +
                    COLUMN_CONTENT + " TEXT NOT NULL, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                    COLUMN_IS_INCOMING + " INTEGER NOT NULL, " +
                    COLUMN_IS_READ + " INTEGER NOT NULL, " +
                    COLUMN_PHONE_NUMBER + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_CONTACT_ID + ") REFERENCES " +
                    TABLE_CONTACTS + "(" + COLUMN_ID + ")" +
                    ")";

    private static final String SQL_CREATE_MESSAGES_INDEX =
            "CREATE INDEX idx_messages_contact_id ON " + TABLE_MESSAGES +
            "(" + COLUMN_CONTACT_ID + ")";

    private static final String SQL_CREATE_MESSAGES_TIMESTAMP_INDEX =
            "CREATE INDEX idx_messages_timestamp ON " + TABLE_MESSAGES +
            "(" + COLUMN_TIMESTAMP + " DESC)";

    private static final String SQL_CREATE_GROUPS =
            "CREATE TABLE " + TABLE_GROUPS + " (" +
                    COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GROUP_NAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_GROUP_COLOR + " TEXT NOT NULL" +
                    ")";

    private static final String SQL_CREATE_CALL_HISTORY =
            "CREATE TABLE " + TABLE_CALL_HISTORY + " (" +
                    COLUMN_CALL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CALL_CONTACT_ID + " INTEGER, " +
                    COLUMN_CALL_TYPE + " INTEGER NOT NULL, " +
                    COLUMN_CALL_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_CALL_TIMESTAMP + " INTEGER NOT NULL, " +
                    COLUMN_CALL_PHONE + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_CALL_CONTACT_ID + ") REFERENCES " +
                    TABLE_CONTACTS + "(" + COLUMN_ID + ")" +
                    ")";

    private static final String SQL_CREATE_CALL_HISTORY_INDEX =
            "CREATE INDEX idx_call_history_contact_id ON " + TABLE_CALL_HISTORY +
            "(" + COLUMN_CALL_CONTACT_ID + ")";

    private static final String SQL_CREATE_CALL_HISTORY_TIMESTAMP_INDEX =
            "CREATE INDEX idx_call_history_timestamp ON " + TABLE_CALL_HISTORY +
            "(" + COLUMN_CALL_TIMESTAMP + " DESC)";

    public ContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            try {
                // 创建表
                db.execSQL(SQL_CREATE_CONTACTS);
                db.execSQL(SQL_CREATE_MESSAGES);
                db.execSQL(SQL_CREATE_GROUPS);
                db.execSQL(SQL_CREATE_CALL_HISTORY);
                
                // 创建索引
                db.execSQL(SQL_CREATE_MESSAGES_INDEX);
                db.execSQL(SQL_CREATE_MESSAGES_TIMESTAMP_INDEX);
                db.execSQL(SQL_CREATE_CALL_HISTORY_INDEX);
                db.execSQL(SQL_CREATE_CALL_HISTORY_TIMESTAMP_INDEX);
                
                // 插入默认分组
                insertDefaultGroups(db);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error creating database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_MESSAGES);
            db.execSQL(SQL_CREATE_GROUPS);
            db.execSQL(SQL_CREATE_CALL_HISTORY);
            
            // Create indexes
            db.execSQL(SQL_CREATE_MESSAGES_INDEX);
            db.execSQL(SQL_CREATE_MESSAGES_TIMESTAMP_INDEX);
            db.execSQL(SQL_CREATE_CALL_HISTORY_INDEX);
            db.execSQL(SQL_CREATE_CALL_HISTORY_TIMESTAMP_INDEX);
            
            insertDefaultGroups(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 方案1：保留数据
        Log.w(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion);
        // 不执行任何操作，保持数据库结构不
        
        // 方案2：如果一定要降级，则删除旧数据库重建
        /*
        Log.w(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        onUpgrade(db, oldVersion, newVersion);
        */
    }

    private void insertDefaultGroups(SQLiteDatabase db) {
        try {
            String[][] defaultGroups = {
                {"家人", "#4CAF50"},  // Green
                {"朋友", "#2196F3"}, // Blue
                {"工作", "#FFC107"},    // Yellow
                {"其他", "#9E9E9E"}    // Gray
            };

            for (String[] group : defaultGroups) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_GROUP_NAME, group[0]);
                values.put(COLUMN_GROUP_COLOR, group[1]);
                db.insertWithOnConflict(TABLE_GROUPS, null, values, 
                    SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error inserting default groups: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Contact CRUD operations
    public long insertContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        long newRowId = -1;
        
        // 验证必填字段
        if (contact.getName() == null || contact.getName().trim().isEmpty()) {
            Log.e("ContactDbHelper", "Contact name cannot be empty");
            return -1;
        }
        if (contact.getPhone() == null || contact.getPhone().trim().isEmpty()) {
            Log.e("ContactDbHelper", "Contact phone cannot be empty");
            return -1;
        }

        // 准备数据
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName().trim());
        values.put(COLUMN_PHONE, contact.getPhone().trim());
        values.put(COLUMN_COMPANY, contact.getCompany() != null ? contact.getCompany().trim() : "");
        values.put(COLUMN_EMAIL, contact.getEmail() != null ? contact.getEmail().trim() : "");
        values.put(COLUMN_RINGTONE, contact.getRingtone() != null ? contact.getRingtone() : "default");

        // 确保群组存在
        String group = contact.getGroup();
        if (group == null || group.trim().isEmpty()) {
            group = "其他";
        }
        ensureGroupExists(db, group);
        values.put(COLUMN_GROUP, group);

        // 执行插入操作
        db.beginTransaction();
        try {
            newRowId = db.insert(TABLE_CONTACTS, null, values);
            if (newRowId != -1) {
                contact.setId(newRowId);
                db.setTransactionSuccessful();
                Log.d("ContactDbHelper", "Successfully inserted contact: " + contact.getName());
            } else {
                Log.e("ContactDbHelper", "Failed to insert contact: " + contact.getName());
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error inserting contact: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        
        return newRowId;
    }

    private void ensureGroupExists(SQLiteDatabase db, String groupName) {
        Cursor cursor = db.query(TABLE_GROUPS, new String[]{COLUMN_GROUP_NAME},
                COLUMN_GROUP_NAME + "=?", new String[]{groupName},
                null, null, null);
                
        if (cursor != null) {
            try {
                if (!cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_GROUP_NAME, groupName);
                    values.put(COLUMN_GROUP_COLOR, "#9E9E9E"); // Default gray color
                    db.insertWithOnConflict(TABLE_GROUPS, null, values, 
                        SQLiteDatabase.CONFLICT_IGNORE);
                }
            } finally {
                cursor.close();
            }
        }
    }

    public int updateContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE, contact.getPhone());
        values.put(COLUMN_COMPANY, contact.getCompany());
        values.put(COLUMN_GROUP, contact.getGroup());
        values.put(COLUMN_EMAIL, contact.getEmail());
        values.put(COLUMN_RINGTONE, contact.getRingtone());
        return db.update(TABLE_CONTACTS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(contact.getId())});
    }

    public void deleteContact(long contactId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CONTACTS, COLUMN_ID + "=?",
                new String[]{String.valueOf(contactId)});
    }

    public Contact getContact(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Contact contact = null;
        
        try {
            Cursor cursor = db.query(TABLE_CONTACTS, null,
                    COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                contact = cursorToContact(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error getting contact: " + e.getMessage());
            e.printStackTrace();
        }
        
        return contact;
    }

    private Contact cursorToContact(Cursor cursor) {
        try {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            String company = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String ringtone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE));
            String group = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP));

            Contact contact = new Contact(name, phone, company, email, ringtone, group);
            contact.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            return contact;
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error converting cursor to contact: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String[] projection = {
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_PHONE,
            COLUMN_COMPANY,
            COLUMN_GROUP,
            COLUMN_EMAIL,
            COLUMN_RINGTONE
        };

        try (Cursor cursor = db.query(
                TABLE_CONTACTS,
                projection,
                null,
                null,
                null,
                null,
                COLUMN_NAME + " COLLATE NOCASE ASC")) {

            while (cursor.moveToNext()) {
                Contact contact = new Contact();
                contact.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                contact.setCompany(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY)));
                contact.setGroup(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP)));
                contact.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                contact.setRingtone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE)));
                contacts.add(contact);
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error getting all contacts: " + e.getMessage(), e);
        }

        return contacts;
    }

    public List<Contact> getContactsByGroup(String group) {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null,
                COLUMN_GROUP + "=?", new String[]{group},
                null, null, COLUMN_NAME + " ASC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                contacts.add(cursorToContact(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return contacts;
    }

    // Group operations
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS, new String[]{COLUMN_GROUP_NAME},
                null, null, null, null, COLUMN_GROUP_NAME + " ASC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                groups.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return groups;
    }

    public String getGroupColor(String groupName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS, new String[]{COLUMN_GROUP_COLOR},
                COLUMN_GROUP_NAME + "=?", new String[]{groupName},
                null, null, null);
        
        String color = "#9E9E9E"; // Default gray
        if (cursor != null && cursor.moveToFirst()) {
            color = cursor.getString(0);
            cursor.close();
        }
        return color;
    }

    public void updateGroupColor(String groupName, String color) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_COLOR, color);
        db.update(TABLE_GROUPS, values,
                COLUMN_GROUP_NAME + "=?", new String[]{groupName});
    }

    public int getContactCountInGroup(String groupName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{"COUNT(*)"},
                COLUMN_GROUP + "=?", new String[]{groupName},
                null, null, null);
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // Call History operations
    public void addCallRecord(long contactId, String phoneNumber, int callType, long duration) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CALL_CONTACT_ID, contactId);
        values.put(COLUMN_CALL_TYPE, callType);
        values.put(COLUMN_CALL_DURATION, duration);
        values.put(COLUMN_CALL_TIMESTAMP, System.currentTimeMillis());
        values.put(COLUMN_CALL_PHONE, phoneNumber);
        db.insert(TABLE_CALL_HISTORY, null, values);
    }

    public List<CallRecord> getCallHistory() {
        List<CallRecord> callRecords = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_CALL_HISTORY + 
                      " ORDER BY " + COLUMN_CALL_TIMESTAMP + " DESC";
        
        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                CallRecord record = new CallRecord(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CALL_CONTACT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALL_TYPE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CALL_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALL_DURATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CALL_PHONE))
                );
                record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CALL_ID)));
                callRecords.add(record);
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error getting call history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return callRecords;
    }

    public void deleteCallHistory(long contactId) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COLUMN_CALL_CONTACT_ID + "=?";
        String[] whereArgs = {String.valueOf(contactId)};
        db.delete(TABLE_CALL_HISTORY, whereClause, whereArgs);
    }

    // 消息相关操作
    public long insertMessage(long contactId, String content, boolean isIncoming) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTACT_ID, contactId);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put(COLUMN_IS_INCOMING, isIncoming ? 1 : 0);
        values.put(COLUMN_IS_READ, 0);
        values.put(COLUMN_PHONE_NUMBER, getContactPhone(contactId));
        return db.insert(TABLE_MESSAGES, null, values);
    }

    public List<Message> getMessageHistory(long contactId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MESSAGES, null,
                COLUMN_CONTACT_ID + "=?", new String[]{String.valueOf(contactId)},
                null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_MESSAGE_ID)));
                message.setContactId(cursor.getLong(cursor.getColumnIndex(COLUMN_CONTACT_ID)));
                message.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));
                message.setTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
                message.setIncoming(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_INCOMING)) == 1);
                message.setRead(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_READ)) == 1);
                messages.add(message);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return messages;
    }

    public void markMessageAsRead(long messageId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_READ, 1);
        db.update(TABLE_MESSAGES, values,
                COLUMN_MESSAGE_ID + "=?", new String[]{String.valueOf(messageId)});
    }

    // 群组管理增强
    public void moveContactToGroup(long contactId, String newGroup) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP, newGroup);
        db.update(TABLE_CONTACTS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(contactId)});
    }

    public void createGroup(String groupName, String color) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, groupName);
        values.put(COLUMN_GROUP_COLOR, color);
        db.insertWithOnConflict(TABLE_GROUPS, null, values, 
            SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean deleteGroup(String groupName) {
        // 不允许删除"其他"分组
        if ("其他".equals(groupName)) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // 1. 将该分组的联系人移动到"其他"分组
            ContentValues values = new ContentValues();
            values.put(COLUMN_GROUP, "其他");
            int contactsUpdated = db.update(TABLE_CONTACTS,
                    values,
                    COLUMN_GROUP + "=?",
                    new String[]{groupName});

            // 2. 删除分组
            int groupsDeleted = db.delete(TABLE_GROUPS,
                    COLUMN_GROUP_NAME + "=?",
                    new String[]{groupName});

            if (groupsDeleted > 0) {
                db.setTransactionSuccessful();
                success = true;
                Log.d(TAG, String.format("Deleted group %s and moved %d contacts to default group",
                        groupName, contactsUpdated));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting group: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return success;
    }

    // 联系人搜索功能
    public List<Contact> searchContacts(String query) {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String selection = COLUMN_NAME + " LIKE ? OR " +
                          COLUMN_PHONE + " LIKE ? OR " +
                          COLUMN_EMAIL + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%",
                                            "%" + query + "%",
                                            "%" + query + "%"};
        
        Cursor cursor = db.query(TABLE_CONTACTS, null, selection, selectionArgs,
                null, null, COLUMN_NAME + " ASC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                contacts.add(cursorToContact(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return contacts;
    }

    // 铃声管理
    public void updateContactRingtone(long contactId, String ringtone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RINGTONE, ringtone);
        db.update(TABLE_CONTACTS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(contactId)});
    }

    // 联人统计
    public Map<String, Integer> getGroupStatistics() {
        Map<String, Integer> statistics = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CONTACTS,
                new String[]{COLUMN_GROUP, "COUNT(*) as count"},
                null, null, COLUMN_GROUP, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String group = cursor.getString(0);
                int count = cursor.getInt(1);
                statistics.put(group, count);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return statistics;
    }

    // 辅助方法
    private String getContactPhone(long contactId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS,
                new String[]{COLUMN_PHONE},
                COLUMN_ID + "=?", new String[]{String.valueOf(contactId)},
                null, null, null);
        
        String phone = "";
        if (cursor != null && cursor.moveToFirst()) {
            phone = cursor.getString(0);
            cursor.close();
        }
        return phone;
    }

    // 批量操作
    public void deleteContacts(Set<Long> contactIds) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Long id : contactIds) {
                db.delete(TABLE_CONTACTS, COLUMN_ID + "=?",
                        new String[]{String.valueOf(id)});
                // 删除相关的通话记录和消息
                db.delete(TABLE_CALL_HISTORY, COLUMN_CALL_CONTACT_ID + "=?",
                        new String[]{String.valueOf(id)});
                db.delete(TABLE_MESSAGES, COLUMN_CONTACT_ID + "=?",
                        new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void moveContactsToGroup(Set<Long> contactIds, String newGroup) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GROUP, newGroup);
            for (Long id : contactIds) {
                db.update(TABLE_CONTACTS, values,
                        COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // 未读消息统计
    public int getUnreadMessageCount(long contactId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES,
                new String[]{"COUNT(*)"},
                COLUMN_CONTACT_ID + "=? AND " + COLUMN_IS_READ + "=0",
                new String[]{String.valueOf(contactId)},
                null, null, null);
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public void debugDatabaseContent() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, null, null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                StringBuilder row = new StringBuilder();
                for (String columnName : cursor.getColumnNames()) {
                    row.append(columnName).append(": ")
                       .append(cursor.getString(cursor.getColumnIndex(columnName)))
                       .append(", ");
                }
                Log.d("Database", row.toString());
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_MESSAGES + 
                      " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)));
                message.setContactId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_ID)));
                message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                message.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                message.setIncoming(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_INCOMING)) == 1);
                message.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_READ)) == 1);
                message.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER)));
                
                messages.add(message);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return messages;
    }

    public long insertCallRecord(CallRecord record) {
        SQLiteDatabase db = getWritableDatabase();
        long newRowId = -1;
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_CALL_CONTACT_ID, record.getContactId());
        values.put(COLUMN_CALL_TYPE, record.getCallType());
        values.put(COLUMN_CALL_DURATION, record.getDuration());
        values.put(COLUMN_CALL_TIMESTAMP, record.getCallTime());
        values.put(COLUMN_CALL_PHONE, record.getPhoneNumber());
        
        db.beginTransaction();
        try {
            newRowId = db.insert(TABLE_CALL_HISTORY, null, values);
            if (newRowId != -1) {
                db.setTransactionSuccessful();
                Log.d("ContactDbHelper", "Successfully inserted call record");
            } else {
                Log.e("ContactDbHelper", "Failed to insert call record");
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error inserting call record: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        
        return newRowId;
    }

    public List<CallRecord> getCallHistoryForContact(long contactId) {
        List<CallRecord> callRecords = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_CALL_HISTORY +
                " WHERE " + COLUMN_CALL_CONTACT_ID + " = ?" +
                " ORDER BY " + COLUMN_CALL_TIMESTAMP + " DESC";
                
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(contactId)})) {
            while (cursor.moveToNext()) {
                CallRecord record = new CallRecord(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CALL_CONTACT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALL_TYPE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CALL_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALL_DURATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CALL_PHONE))
                );
                record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CALL_ID)));
                callRecords.add(record);
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error getting call history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return callRecords;
    }

    public void updateCallDuration(long callId, int duration) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CALL_DURATION, duration);
        
        db.update(TABLE_CALL_HISTORY, values,
                COLUMN_CALL_ID + "=?", new String[]{String.valueOf(callId)});
    }

    public void deleteCallRecord(long callId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            int deletedRows = db.delete(TABLE_CALL_HISTORY,
                    COLUMN_CALL_ID + " = ?",
                    new String[]{String.valueOf(callId)});
            if (deletedRows > 0) {
                db.setTransactionSuccessful();
                Log.d("ContactDbHelper", "Successfully deleted call record");
            } else {
                Log.e("ContactDbHelper", "Failed to delete call record");
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error deleting call record: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void clearCallHistory(long contactId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            int deletedRows = db.delete(TABLE_CALL_HISTORY,
                    COLUMN_CALL_CONTACT_ID + " = ?",
                    new String[]{String.valueOf(contactId)});
            if (deletedRows > 0) {
                db.setTransactionSuccessful();
                Log.d("ContactDbHelper", "Successfully cleared call history");
            } else {
                Log.e("ContactDbHelper", "Failed to clear call history");
            }
        } catch (Exception e) {
            Log.e("ContactDbHelper", "Error clearing call history: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public int getContactCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CONTACTS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // 添加新的分组
    public long addGroup(String groupName, String color) {
        SQLiteDatabase db = getWritableDatabase();
        long newRowId = -1;

        try {
            // 检查分组是否已存在
            Cursor cursor = db.query(TABLE_GROUPS,
                    new String[]{COLUMN_GROUP_NAME},
                    COLUMN_GROUP_NAME + "=?",
                    new String[]{groupName},
                    null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                return -1; // 分组已存在
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_GROUP_NAME, groupName);
            values.put(COLUMN_GROUP_COLOR, color != null ? color : "#9E9E9E");

            newRowId = db.insert(TABLE_GROUPS, null, values);
            Log.d(TAG, "Added new group: " + groupName + " with id: " + newRowId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding group: " + e.getMessage());
        }

        return newRowId;
    }

    // 修改分组名称
    public boolean updateGroupName(String oldName, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // 1. 更新分组表中的名称
            ContentValues groupValues = new ContentValues();
            groupValues.put(COLUMN_GROUP_NAME, newName);
            int groupsUpdated = db.update(TABLE_GROUPS,
                    groupValues,
                    COLUMN_GROUP_NAME + "=?",
                    new String[]{oldName});

            // 2. 更新所有使用该分组的联系人
            ContentValues contactValues = new ContentValues();
            contactValues.put(COLUMN_GROUP, newName);
            int contactsUpdated = db.update(TABLE_CONTACTS,
                    contactValues,
                    COLUMN_GROUP + "=?",
                    new String[]{oldName});

            db.setTransactionSuccessful();
            success = true;
            Log.d(TAG, String.format("Updated group name from %s to %s. Updated %d contacts",
                    oldName, newName, contactsUpdated));
        } catch (Exception e) {
            Log.e(TAG, "Error updating group name: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return success;
    }

    // 获取分组详细信息
    public Map<String, String> getGroupDetails(String groupName) {
        SQLiteDatabase db = getReadableDatabase();
        Map<String, String> details = new HashMap<>();

        try {
            Cursor cursor = db.query(TABLE_GROUPS,
                    new String[]{COLUMN_GROUP_NAME, COLUMN_GROUP_COLOR},
                    COLUMN_GROUP_NAME + "=?",
                    new String[]{groupName},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                details.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)));
                details.put("color", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_COLOR)));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting group details: " + e.getMessage());
        }

        return details;
    }

    // 检查分组名称是否已存在
    public boolean isGroupNameExists(String groupName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS,
                new String[]{COLUMN_GROUP_NAME},
                COLUMN_GROUP_NAME + "=?",
                new String[]{groupName},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }
} 

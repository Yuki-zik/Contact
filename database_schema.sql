-- 联系人表
CREATE TABLE contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    company TEXT,
    email TEXT,
    ringtone TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 群组表
CREATE TABLE groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

-- 联系人群组关联表
CREATE TABLE contact_groups (
    contact_id INTEGER,
    group_id INTEGER,
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    FOREIGN KEY (group_id) REFERENCES groups(id)
);

-- 通话记录表
CREATE TABLE call_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contact_id INTEGER,
    call_type TEXT, -- 来电/去电/未接
    call_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration INTEGER, -- 通话时长(秒)
    FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

-- 短信记录表
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contact_id INTEGER,
    content TEXT,
    type TEXT, -- 发送/接收
    send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contacts(id)
); 

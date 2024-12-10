package com.example.contacts.model;

public class CallRecord {
    private long id;
    private long contactId;
    private String phoneNumber;
    private long callTime;
    private int callType; // 0: 来电, 1: 去电, 2: 未接
    private int duration; // 通话时长（秒）

    public CallRecord(long contactId, int callType, long callTime, int duration, String phoneNumber) {
        this.contactId = contactId;
        this.callType = callType;
        this.callTime = callTime;
        this.duration = duration;
        this.phoneNumber = phoneNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getTimestamp() {
        return callTime;
    }

    public String getFormattedDuration() {
        if (duration < 60) {
            return duration + "秒";
        } else if (duration < 3600) {
            return (duration / 60) + "分" + (duration % 60) + "秒";
        } else {
            int hours = duration / 3600;
            int minutes = (duration % 3600) / 60;
            int secs = duration % 60;
            return hours + "时" + minutes + "分" + secs + "秒";
        }
    }
} 

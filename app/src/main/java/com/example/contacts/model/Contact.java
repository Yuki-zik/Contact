package com.example.contacts.model;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Contact {
    private long id;
    private String name;
    private String phone;
    private String email;
    private String group;
    private String pinyin;
    private String company;
    private String ringtone;
    private String photoUri;

    public Contact() {
        updatePinyin();
    }

    public Contact(String name, String phone, String company, String email, String ringtone, String group) {
        this.name = name;
        this.phone = phone;
        this.company = company;
        this.email = email;
        this.ringtone = ringtone;
        this.group = group;
        updatePinyin();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        updatePinyin();
    }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRingtone() { return ringtone; }
    public void setRingtone(String ringtone) { this.ringtone = ringtone; }

    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public String getPinyin() {
        if (pinyin == null) {
            updatePinyin();
        }
        return pinyin;
    }

    private void updatePinyin() {
        if (name == null || name.isEmpty()) {
            pinyin = "#";
            return;
        }

        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

            StringBuilder pinyinBuilder = new StringBuilder();
            char[] chars = name.toCharArray();

            for (char c : chars) {
                if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyinBuilder.append(pinyinArray[0]);
                    }
                } else {
                    pinyinBuilder.append(Character.toLowerCase(c));
                }
            }

            pinyin = pinyinBuilder.toString();
            if (pinyin.isEmpty()) {
                pinyin = "#";
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            pinyin = "#";
        }
    }
} 

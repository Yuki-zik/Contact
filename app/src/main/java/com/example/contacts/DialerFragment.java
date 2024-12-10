package com.example.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.CallRecord;
import com.example.contacts.model.Contact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class DialerFragment extends Fragment {
    private static final String TAG = "DialerFragment";
    private static final String PREFS_NAME = "DialerPrefs";
    private static final String PREF_SOUND_ENABLED = "sound_enabled";

    private MaterialTextView phoneNumberDisplay;
    private MaterialButton backspaceButton;
    private MaterialButton callLogButton;
    private MaterialCheckBox soundEnabledCheckBox;
    private StringBuilder phoneNumber = new StringBuilder();
    private Vibrator vibrator;
    private ToneGenerator toneGenerator;
    private SharedPreferences prefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean(PREF_SOUND_ENABLED, true);
        if (soundEnabled) {
            initToneGenerator();
        }
    }

    private void initToneGenerator() {
        if (toneGenerator == null) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
        }
    }

    private void releaseToneGenerator() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialer, container, false);
        
        // 初始化振动器
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        
        // 初始化显示区域
        phoneNumberDisplay = view.findViewById(R.id.phoneNumberDisplay);
        if (phoneNumberDisplay != null) {
            phoneNumberDisplay.setText(""); // 确保初始状态为空
        }
        
        // 初始化按键
        initDialPad(view);
        
        // 初始化删除按钮
        backspaceButton = view.findViewById(R.id.backspaceButton);
        if (backspaceButton != null) {
            backspaceButton.setOnClickListener(v -> {
                deleteNumber();
                addVibration();
            });
            backspaceButton.setOnLongClickListener(v -> {
                clearNumber();
                addVibration();
                return true;
            });
        }

        // 初始化拨号按钮
        ExtendedFloatingActionButton callButton = view.findViewById(R.id.callButton);
        if (callButton != null) {
            callButton.setOnClickListener(v -> makeCall());
        }

        // 初始化通话记录按钮
        callLogButton = view.findViewById(R.id.callLogButton);
        if (callLogButton != null) {
            callLogButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CallLogActivity.class);
                startActivity(intent);
            });
        }

        // 初始化声音设置
        soundEnabledCheckBox = view.findViewById(R.id.soundEnabledCheckBox);
        if (soundEnabledCheckBox != null) {
            soundEnabledCheckBox.setChecked(prefs.getBoolean(PREF_SOUND_ENABLED, true));
            soundEnabledCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(PREF_SOUND_ENABLED, isChecked).apply();
                if (isChecked) {
                    initToneGenerator();
                } else {
                    releaseToneGenerator();
                }
            });
        }

        return view;
    }

    private void initDialPad(View view) {
        // 数字按钮ID数组
        int[] buttonIds = {
            R.id.button0, R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6, R.id.button7,
            R.id.button8, R.id.button9, R.id.buttonStar, R.id.buttonHash
        };

        // 为每个按钮设置点击监听器
        for (int id : buttonIds) {
            MaterialButton button = view.findViewById(id);
            if (button != null) {
                button.setOnClickListener(v -> {
                    String digit = ((MaterialButton) v).getText().toString();
                    addNumber(digit);
                });
            }
        }

        // 为0按钮添加长按监听器（输入+号）
        MaterialButton button0 = view.findViewById(R.id.button0);
        if (button0 != null) {
            button0.setOnLongClickListener(v -> {
                if (phoneNumber.length() == 0) {
                    addNumber("+");
                    return true;
                }
                return false;
            });
        }
    }

    private void addNumber(String num) {
        if (phoneNumber.length() < 20) {  // 限制最大长度
            phoneNumber.append(num);
            updateDisplay();
            addVibration();
            playDialSound(num);
        }
    }

    private void deleteNumber() {
        if (phoneNumber.length() > 0) {
            phoneNumber.deleteCharAt(phoneNumber.length() - 1);
            updateDisplay();
        }
    }

    private void clearNumber() {
        phoneNumber.setLength(0);
        updateDisplay();
    }

    private void updateDisplay() {
        if (phoneNumberDisplay != null) {
            String number = formatPhoneNumber(phoneNumber.toString());
            phoneNumberDisplay.setText(number);
            
            // 根据是否有输入显示/隐藏删除按钮
            if (backspaceButton != null) {
                backspaceButton.setVisibility(number.isEmpty() ? View.GONE : View.VISIBLE);
            }

            // 根据是否有输入调整显示区域的背景
            phoneNumberDisplay.setAlpha(number.isEmpty() ? 0.5f : 1.0f);
        }
    }

    private void addVibration() {
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(20);
            }
        }
    }

    private void playDialSound(String digit) {
        if (toneGenerator != null && soundEnabledCheckBox != null && soundEnabledCheckBox.isChecked()) {
            int tone;
            switch (digit) {
                case "1": tone = ToneGenerator.TONE_DTMF_1; break;
                case "2": tone = ToneGenerator.TONE_DTMF_2; break;
                case "3": tone = ToneGenerator.TONE_DTMF_3; break;
                case "4": tone = ToneGenerator.TONE_DTMF_4; break;
                case "5": tone = ToneGenerator.TONE_DTMF_5; break;
                case "6": tone = ToneGenerator.TONE_DTMF_6; break;
                case "7": tone = ToneGenerator.TONE_DTMF_7; break;
                case "8": tone = ToneGenerator.TONE_DTMF_8; break;
                case "9": tone = ToneGenerator.TONE_DTMF_9; break;
                case "0": tone = ToneGenerator.TONE_DTMF_0; break;
                case "*": tone = ToneGenerator.TONE_DTMF_S; break;
                case "#": tone = ToneGenerator.TONE_DTMF_P; break;
                default: tone = ToneGenerator.TONE_DTMF_1; break;
            }
            toneGenerator.startTone(tone, 150);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseToneGenerator();
    }

    private String formatPhoneNumber(String number) {
        if (number.isEmpty()) {
            return "";
        }

        // 处理国际号码格式
        if (number.startsWith("+")) {
            if (number.length() <= 3) {
                return number;
            }
            // 国际号码格式：+86 138 8888 8888
            StringBuilder formatted = new StringBuilder(number.substring(0, 3));
            for (int i = 3; i < number.length(); i += 3) {
                formatted.append(" ");
                formatted.append(number.substring(i, Math.min(i + 3, number.length())));
            }
            return formatted.toString();
        } else {
            // 国内号码格式：138 8888 8888
            if (number.length() <= 3) {
                return number;
            }
            StringBuilder formatted = new StringBuilder(number.substring(0, 3));
            for (int i = 3; i < number.length(); i += 4) {
                formatted.append(" ");
                formatted.append(number.substring(i, Math.min(i + 4, number.length())));
            }
            return formatted.toString();
        }
    }

    private void makeCall() {
        String number = phoneNumber.toString().replaceAll("\\s+", "");
        if (number.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查号码格式
        if (!isValidPhoneNumber(number)) {
            Toast.makeText(requireContext(), "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查权限
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }

        // 拨打电话
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        try {
            startActivity(intent);
            // 保存通话记录
            saveCallRecord(number);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to make call: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPhoneNumber(String number) {
        // 移除所有空格
        String cleaned = number.replaceAll("\\s+", "");
        // 检查国际号码格式
        if (cleaned.startsWith("+")) {
            return cleaned.substring(1).matches("\\d{10,15}");
        }
        // 检查国内号码格式
        return cleaned.matches("\\d{11}");
    }

    private void saveCallRecord(String number) {
        try {
            ContactDbHelper dbHelper = new ContactDbHelper(requireContext());
            // 查找联系人ID
            long contactId = -1;
            List<Contact> contacts = dbHelper.searchContacts(number);
            if (!contacts.isEmpty()) {
                contactId = contacts.get(0).getId();
            }
            
            // 创建通话记录
            CallRecord record = new CallRecord(
                contactId,
                1, // 1表示呼出
                System.currentTimeMillis(),
                0, // 通话时长初始为0
                number
            );
            
            // 保存到数据库
            dbHelper.insertCallRecord(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

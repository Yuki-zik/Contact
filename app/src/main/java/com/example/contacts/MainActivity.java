package com.example.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.contacts.database.ContactDbHelper;
import com.example.contacts.model.Contact;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ContactDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new ContactDbHelper(this);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // 设置ViewPager2
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2); // 预加载所有页面
        viewPager.setUserInputEnabled(true); // 允许滑动切换
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 可以在这里处理页面切换事件
            }
        });

        // 设置TabLayout和ViewPager2的联动
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.tab_contacts);
                            break;
                        case 1:
                            tab.setText(R.string.tab_dialpad);
                            break;
                        case 2:
                            tab.setText(R.string.tab_messages);
                            break;
                    }
                }).attach();

        // 检查是否需要注入示例数据
        if (dbHelper.getContactCount() == 0) {
            injectSampleContacts();
        }
    }

    private void injectSampleContacts() {
        // 家人分组示例联系人
        Contact[] familyContacts = {
            new Contact("爸爸", "13800138001", "某公司", "father@example.com", "default", "家人"),
            new Contact("妈妈", "13800138002", "某学校", "mother@example.com", "default", "家人"),
            new Contact("姐姐", "13800138003", "某医院", "sister@example.com", "default", "家人")
        };

        // 朋友分组示例联系人
        Contact[] friendContacts = {
            new Contact("张三", "13900139001", "科技公司", "zhangsan@example.com", "default", "朋友"),
            new Contact("李四", "13900139002", "设计工作室", "lisi@example.com", "default", "朋友"),
            new Contact("王五", "13900139003", "咖啡店", "wangwu@example.com", "default", "朋友")
        };

        // 工作分组示例联系人
        Contact[] workContacts = {
            new Contact("项目经理", "13700137001", "ABC科技", "manager@company.com", "default", "工作"),
            new Contact("技术主管", "13700137002", "ABC科技", "tech@company.com", "default", "工作"),
            new Contact("人事主管", "13700137003", "ABC科技", "hr@company.com", "default", "工作")
        };

        // 其他分组示例联系人
        Contact[] otherContacts = {
            new Contact("快递小哥", "13600136001", "顺丰快递", "", "default", "其他"),
            new Contact("物业管理", "13600136002", "幸福小区", "", "default", "其他"),
            new Contact("外卖商家", "13600136003", "美味餐厅", "", "default", "其他")
        };

        // 插入所有示例联系人
        Contact[][] allContacts = {familyContacts, friendContacts, workContacts, otherContacts};
        for (Contact[] contacts : allContacts) {
            for (Contact contact : contacts) {
                dbHelper.insertContact(contact);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage_groups) {
            // 启动分组管理界面
            Intent intent = new Intent(this, GroupManagementActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 

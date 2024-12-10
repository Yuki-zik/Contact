package com.example.contacts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.contacts.fragment.ContactsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 3;
    private Fragment[] fragments;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments = new Fragment[NUM_PAGES];
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (fragments[position] == null) {
            switch (position) {
                case 0:
                    fragments[position] = new ContactsFragment();
                    break;
                case 1:
                    fragments[position] = new DialerFragment();
                    break;
                case 2:
                    fragments[position] = new MessagesFragment();
                    break;
                default:
                    fragments[position] = new ContactsFragment();
                    break;
            }
        }
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < NUM_PAGES;
    }
} 

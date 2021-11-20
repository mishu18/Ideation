package com.example.android.ideation.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.android.ideation.fragements.BooksChatRoomsFragment;
import com.example.android.ideation.fragements.PostsChatRoomsFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = new PostsChatRoomsFragment();
        } else if (position == 1) {
            fragment = new BooksChatRoomsFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0) {
            title = "Posts";
        } else if (position == 1) {
            title = "Books";
        }
        return title;
    }
}
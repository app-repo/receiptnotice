package com.weihuagu.receiptnotice.view;


import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomeFragmentsAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> fragmentslist = new ArrayList<>();

    public HomeFragmentsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragmentslist.add(new HelloFragment());
        fragmentslist.add(new LogListFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentslist.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentslist.size();
    }
}

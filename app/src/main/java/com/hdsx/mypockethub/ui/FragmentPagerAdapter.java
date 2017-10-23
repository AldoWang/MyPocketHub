package com.hdsx.mypockethub.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

public abstract class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter
        implements FragmentProvider {

    private FragmentManager fragmentManager;
    private AppCompatActivity activity;
    private Fragment selected;

    public FragmentPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
        fragmentManager = activity.getSupportFragmentManager();
        this.activity = activity;
    }

    public FragmentPagerAdapter(Fragment fragment) {
        super(fragment.getChildFragmentManager());
        fragmentManager = fragment.getChildFragmentManager();
        activity = (AppCompatActivity) fragment.getActivity();
    }

    @Override
    public Fragment getSelected() {
        return selected;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        boolean changed;
        if (object instanceof Fragment) {
            changed = object != selected;
            selected = (Fragment) object;
        } else {
            changed = object != null;
            selected = null;
        }

        if (changed)
            activity.invalidateOptionsMenu();
    }

}

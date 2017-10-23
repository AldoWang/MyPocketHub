package com.hdsx.mypockethub.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

public abstract class FragmentStatePagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter
        implements FragmentProvider {

    private Fragment selected;
    private AppCompatActivity activity;

    public FragmentStatePagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
        this.activity = activity;
    }

    @Override
    public Fragment getSelected() {
        return selected;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        boolean changed = false;
        if (object instanceof Fragment) {
            changed = selected != object;
            selected = (Fragment) object;
        } else {
            changed = object != null;
            selected = null;
        }

        if (changed) {
            activity.invalidateOptionsMenu();
        }
    }

}

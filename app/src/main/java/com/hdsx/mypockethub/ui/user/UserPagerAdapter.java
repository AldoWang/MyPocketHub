package com.hdsx.mypockethub.ui.user;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ui.FragmentPagerAdapter;

public class UserPagerAdapter extends FragmentPagerAdapter {

    private Resources resources;

    public UserPagerAdapter(AppCompatActivity activity) {
        super(activity);
        resources = activity.getResources();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new UserCreatedNewsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.tab_news);
            case 1:
                return resources.getString(R.string.tab_repositories);
            case 2:
                return resources.getString(R.string.tab_followers);
            case 3:
                return resources.getString(R.string.tab_following);
            default:
                return null;
        }
    }

}

package com.hdsx.mypockethub.ui.user;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ui.FragmentPagerAdapter;
import com.hdsx.mypockethub.ui.repo.RepositoryListFragment;
import com.meisolsson.githubsdk.model.User;


public class HomePagerAdapter extends FragmentPagerAdapter {

    private boolean defaultUser;
    private User org;
    private Resources resources;

    public HomePagerAdapter(Fragment fragment, boolean defaultUser, User org) {
        super(fragment);
        this.defaultUser = defaultUser;
        this.org = org;
        resources = fragment.getResources();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = defaultUser ? new UserReceivedNewsFragment() : new OrganizationNewsFragment();
                break;
            case 1:
                fragment = new RepositoryListFragment();
                break;
            case 2:
                fragment = defaultUser ? new MyFollowersFragment() : new MembersFragment();
                break;
            case 3:
                fragment = new MyFollowingFragment();
                break;
        }

        if (fragment != null) {
            Bundle args = new Bundle();
            args.putParcelable("org", org);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.tab_news);
            case 1:
                return resources.getString(R.string.tab_repositories);
            case 2:
                return resources.getString(defaultUser ? R.string.tab_followers_self
                        : R.string.tab_members);
            case 3:
                return resources.getString(R.string.tab_following_self);
            default:
                return null;
        }

    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return defaultUser ? 4 : 3;
    }

}

package com.hdsx.mypockethub.ui.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.hdsx.mypockethub.accounts.AccountUtils;
import com.hdsx.mypockethub.ui.TabPagerFragment;
import com.meisolsson.githubsdk.model.User;

import static android.content.Context.MODE_PRIVATE;


public class HomePageFragment extends TabPagerFragment<HomePagerAdapter> {

    private SharedPreferences sp;
    private static final String PREF_ORG_ID = "orgId";
    private User org;
    private boolean isDefault;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sp = getActivity().getPreferences(MODE_PRIVATE);
        setOrg((User) getArguments().getParcelable("org"));
    }

    private void setOrg(User org) {
        sp.edit().putInt(PREF_ORG_ID, org.id()).apply();
        this.org = org;
        isDefault = AccountUtils.isUser(getActivity(), org);
        configureTabPager();
    }

    @Override
    protected HomePagerAdapter createAdapter() {
        return new HomePagerAdapter(this, isDefault, org);
    }

}

package com.hdsx.mypockethub.ui.repo;

import android.accounts.Account;
import android.app.Activity;

import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.accounts.AuthenticatedUserLoader;
import com.hdsx.mypockethub.core.user.UserComparator;
import com.hdsx.mypockethub.persistence.AccountDataManager;
import com.hdsx.mypockethub.util.ToastUtils;
import com.meisolsson.githubsdk.model.User;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;


public class OrganizationLoader extends AuthenticatedUserLoader<List<User>> {

    @Inject
    AccountDataManager accountDataManager;

    public OrganizationLoader(Activity activity) {
        super(activity);
        App.getAppComponent().inject(this);
    }

    @Override
    protected List<User> loadData(Account account) {
        List<User> orgs;
        try {
            orgs = accountDataManager.getOrgs(false);
        } catch (IOException e) {
            ToastUtils.show(R.string.error_orgs_load);
            return Collections.emptyList();
        }
        Collections.sort(orgs, new UserComparator(account));
        return orgs;
    }

    @Override
    protected List<User> getAccountFailureData() {
        return Collections.emptyList();
    }

}

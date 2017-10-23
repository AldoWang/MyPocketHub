package com.hdsx.mypockethub.persistence;

import android.content.Context;

import com.meisolsson.githubsdk.model.User;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class AccountDataManager {

    @Inject
    Context context;

    @Inject
    DatabaseCache dbCache;

    @Inject
    Organizations userAndOrgsResource;

    @Inject
    public AccountDataManager() {
    }

    public List<User> getOrgs(boolean forceReload) throws IOException {
        return forceReload ? dbCache.requestAndStore(userAndOrgsResource)
                : dbCache.loadOrRequest(userAndOrgsResource);
    }

}

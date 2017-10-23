package com.hdsx.mypockethub;

import android.content.Context;

import com.hdsx.mypockethub.core.commit.CommitStore;
import com.hdsx.mypockethub.core.issue.IssueStore;
import com.hdsx.mypockethub.persistence.CacheHelper;
import com.hdsx.mypockethub.util.AvatarLoader;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    public Context getContext() {
        return app.getBaseContext();
    }

    @Singleton
    @Provides
    AvatarLoader getAvatarLoader() {
        return new AvatarLoader(App.getContext());
    }

    @Singleton
    @Provides
    CommitStore getCommitStore() {
        return new CommitStore(App.getContext());
    }

    @Singleton
    @Provides
    IssueStore getIssueStore() {
        return new IssueStore(App.getContext());
    }

    @Singleton
    @Provides
    CacheHelper getCacheHelper() {
        return new CacheHelper(App.getContext());
    }

}

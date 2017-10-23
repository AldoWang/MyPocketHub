package com.hdsx.mypockethub;


import com.hdsx.mypockethub.ui.MainActivity;
import com.hdsx.mypockethub.ui.commit.CommitListFragment;
import com.hdsx.mypockethub.ui.issue.IssueFragment;
import com.hdsx.mypockethub.ui.issue.IssuesFragment;
import com.hdsx.mypockethub.ui.issue.IssuesViewActivity;
import com.hdsx.mypockethub.ui.repo.OrganizationLoader;
import com.hdsx.mypockethub.ui.repo.RepositoryViewActivity;
import com.hdsx.mypockethub.ui.user.NewsFragment;
import com.hdsx.mypockethub.ui.user.UserViewActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(NewsFragment fragment);

    void inject(RepositoryViewActivity activity);

    void inject(CommitListFragment fragment);

    void inject(IssuesFragment fragment);

    void inject(OrganizationLoader loader);

    void inject(UserViewActivity activity);

    void inject(IssuesViewActivity activity);

    void inject(IssueFragment fragment);

}

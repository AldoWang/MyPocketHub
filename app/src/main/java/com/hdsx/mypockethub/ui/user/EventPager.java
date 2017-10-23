package com.hdsx.mypockethub.ui.user;

import com.hdsx.mypockethub.core.ResourcePager;
import com.meisolsson.githubsdk.model.GitHubEvent;


public abstract class EventPager extends ResourcePager<GitHubEvent> {

    @Override
    protected Object getId(GitHubEvent resource) {
        return resource.id();
    }

    @Override
    protected GitHubEvent register(GitHubEvent resource) {
        return NewsListAdapter.isValid(resource) ? resource : null;
    }
}

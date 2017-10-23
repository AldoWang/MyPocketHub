package com.hdsx.mypockethub.core.issue;

import com.hdsx.mypockethub.core.ResourcePager;
import com.meisolsson.githubsdk.model.Issue;


public abstract class IssuePager extends ResourcePager<Issue> {

    private IssueStore store;

    public IssuePager(IssueStore store) {
        this.store = store;
    }

    @Override
    protected Object getId(Issue resource) {
        return resource.id();
    }

    @Override
    protected Issue register(Issue resource) {
        return store.addIssue(resource);
    }

}

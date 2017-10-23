package com.hdsx.mypockethub.ui.commit;

import com.hdsx.mypockethub.core.ResourcePager;
import com.hdsx.mypockethub.core.commit.CommitStore;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;

public abstract class CommitPager extends ResourcePager<Commit> {

    private Repository repository;
    private CommitStore store;

    public CommitPager(Repository repository, CommitStore store) {
        this.repository = repository;
        this.store = store;
    }

    @Override
    protected Object getId(Commit resource) {
        return resource.sha();
    }

    @Override
    protected Commit register(Commit commit) {
        return store.addCommit(repository, commit);
    }

}

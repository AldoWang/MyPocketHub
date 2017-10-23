package com.hdsx.mypockethub.core.commit;

import android.content.Context;

import com.hdsx.mypockethub.core.ItemStore;
import com.hdsx.mypockethub.util.InfoUtils;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class CommitStore extends ItemStore {

    private Map<String, ItemReferences<Commit>> commits = new HashMap<>();

    private Context context;

    @Inject()
    public CommitStore(Context context) {
        this.context = context;
    }

    public Commit addCommit(Repository repository, Commit commit) {
        Commit current = getCommit(repository, commit.sha());
        if (current == commit) {
            return commit;
        }

        String repoId = InfoUtils.createRepoId(repository);
        ItemReferences<Commit> repoCommits = commits.get(repoId);
        if (repoCommits == null) {
            repoCommits = new ItemReferences<>();
            commits.put(repoId, repoCommits);
        }
        repoCommits.put(commit.sha(), commit);
        return commit;
    }

    private Commit getCommit(Repository repository, String id) {
        ItemReferences<Commit> repoCommits = commits.get(InfoUtils.createRepoId(repository));
        return repoCommits != null ? repoCommits.get(id) : null;
    }

}

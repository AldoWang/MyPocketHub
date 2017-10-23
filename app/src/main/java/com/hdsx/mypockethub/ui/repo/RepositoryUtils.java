package com.hdsx.mypockethub.ui.repo;

import com.meisolsson.githubsdk.model.Repository;

public class RepositoryUtils {

    public static boolean isComplete(Repository repository) {
        return (repository.isPrivate() != null && repository.isPrivate())
                || (repository.isFork() != null && repository.isFork())
                || (repository.forksCount() != null && repository.forksCount() > 0)
                || (repository.watchersCount() != null && repository.watchersCount() > 0)
                || (repository.hasIssues() != null && repository.hasIssues());
    }

}

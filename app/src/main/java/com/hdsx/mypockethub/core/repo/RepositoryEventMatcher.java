package com.hdsx.mypockethub.core.repo;

import android.text.TextUtils;

import com.hdsx.mypockethub.util.ConvertUtils;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.payload.ForkPayload;

import static com.meisolsson.githubsdk.model.GitHubEventType.CreateEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.ForkEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.PublicEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.WatchEvent;

public class RepositoryEventMatcher {

    public Repository getRepository(GitHubEvent event) {
        if (event == null || event.payload() == null)
            return null;

        GitHubEventType type = event.type();
        if (ForkEvent == type) {
            Repository repository = ((ForkPayload) event.payload()).forkee();
            if (repository != null && !TextUtils.isEmpty(repository.name()) && repository.owner() != null
                    && !TextUtils.isEmpty(repository.owner().login())) {
                return repository;
            }
        }

        if (WatchEvent.equals(type) || CreateEvent.equals(type) || PublicEvent.equals(type))
            return ConvertUtils.eventRepoToRepo(event.repo());
        return null;
    }

}

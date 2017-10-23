package com.hdsx.mypockethub.core.issue;

import android.content.Context;

import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.ItemStore;
import com.hdsx.mypockethub.util.InfoUtils;
import com.hdsx.mypockethub.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.issues.IssueService;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import retrofit2.Response;


public class IssueStore extends ItemStore {

    private Map<String, ItemReferences<Issue>> repos = new HashMap<>();

    private Context context;
    private IssueService service;

    public IssueStore(Context context) {
        this.context = context;
        service = ServiceGenerator.createService(context, IssueService.class);
    }

    public Single<Issue> refreshIssue(final Repository repository, int issueNumber) {
        return service.getIssue(repository.owner().login(), repository.name(), issueNumber)
                .map(new Function<Response<Issue>, Issue>() {
                    @Override
                    public Issue apply(@NonNull Response<Issue> response) throws Exception {
                        return addIssueOrThrow(repository, response, R.string.error_issue_load);
                    }
                });
    }

    private Issue addIssueOrThrow(Repository repository, Response<Issue> response, int resId) {
        if (response.isSuccessful()) {
            return addIssue(repository, response.body());
        } else {
            ToastUtils.show(resId);
            return Issue.builder().build();
        }
    }

    public Issue addIssue(Issue issue) {
        Repository repo = null;
        if (issue != null) {
            repo = issue.repository();
            if (repo == null) {
                repo = repoFromUrl(issue.htmlUrl());
            }
        }
        return addIssue(repo, issue);
    }

    public Issue addIssue(Repository repository, Issue issue) {
        Issue current = getIssue(repository, issue.number());
        if (current == issue) {
            return issue;
        }

        String repoId = InfoUtils.createRepoId(repository);
        ItemReferences<Issue> issues = repos.get(repoId);
        if (issues == null) {
            issues = new ItemReferences<>();
            repos.put(repoId, issues);
        }
        issues.put(issue.number(), issue);
        return issue;
    }

    public Issue getIssue(Repository repository, int number) {
        ItemReferences<Issue> repoIssues = repos.get(InfoUtils.createRepoId(repository));
        return repoIssues != null ? repoIssues.get(number) : null;
    }

    private Repository repoFromUrl(String url) {
        if (url == null || url.length() == 0) {
            return null;
        }

        String owner = null;
        String name = null;
        for (String segment : url.split("/")) {
            if (segment.length() > 0) {
                if (owner == null) {
                    owner = segment;
                } else if (name == null) {
                    name = segment;
                } else {
                    break;
                }
            }
        }

        if (owner != null && owner.length() > 0 && name != null && name.length() > 0) {
            return InfoUtils.createRepoFromData(owner, name);
        } else {
            return null;
        }
    }

}

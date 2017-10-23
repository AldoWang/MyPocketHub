package com.hdsx.mypockethub.core.issue;

import android.content.Context;

import com.hdsx.mypockethub.core.PageIterator.GitHubRequest;
import com.hdsx.mypockethub.util.RxPageUtil;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueEvent;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.PullRequest;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.issues.IssueCommentService;
import com.meisolsson.githubsdk.service.issues.IssueEventService;
import com.meisolsson.githubsdk.service.pull_request.PullRequestService;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import retrofit2.Response;

public class RefreshIssueTask {

    IssueStore store;

    private Repository repository;
    private int issueNumber;
    private Context context;

    public RefreshIssueTask(Context context, Repository repository, int issueNumber, IssueStore issueStore) {
        this.context = context;
        this.repository = repository;
        this.issueNumber = issueNumber;
        this.store = issueStore;
    }

    public Single<FullIssue> refresh() {
        return store.refreshIssue(repository, issueNumber)
                .flatMap(new Function<Issue, SingleSource<Issue>>() {
                    @Override
                    public SingleSource<Issue> apply(@NonNull final Issue issue) throws Exception {
                        if (issue.pullRequest() != null) {
                            return getPullRequest(repository.owner().login(), repository.name(), issue.number())
                                    .map(new Function<PullRequest, Issue>() {
                                        @Override
                                        public Issue apply(@NonNull PullRequest pullRequest) throws Exception {
                                            return issue.toBuilder()
                                                    .pullRequest(pullRequest)
                                                    .build();
                                        }
                                    });
                        }
                        return Single.just(issue);
                    }
                })
                .flatMap(new Function<Issue, SingleSource<FullIssue>>() {
                    @Override
                    public SingleSource<FullIssue> apply(@NonNull Issue issue) throws Exception {
                        return getAllComments(repository.owner().login(), repository.name(), issue)
                                .zipWith(Single.just(issue), new BiFunction<List<GitHubComment>, Issue, FullIssue>() {
                                    @Override
                                    public FullIssue apply(@NonNull List<GitHubComment> comments, @NonNull Issue issue) throws Exception {
                                        return new FullIssue(issue, comments, null);
                                    }
                                });
                    }
                })
                .zipWith(getAllEvents(repository.owner().login(), repository.name(), issueNumber)
                        , new BiFunction<FullIssue, List<IssueEvent>, FullIssue>() {
                            @Override
                            public FullIssue apply(@NonNull FullIssue fullIssue, @NonNull List<IssueEvent> issueEvents) throws Exception {
                                return new FullIssue(fullIssue.getIssue(), fullIssue.getComments(), issueEvents);
                            }
                        })
                .map(new Function<FullIssue, FullIssue>() {
                    @Override
                    public FullIssue apply(@NonNull FullIssue fullIssue) throws Exception {
                        return fullIssue;
                    }
                });
    }

    private SingleSource<List<IssueEvent>> getAllEvents(final String owner, final String name, final int issueNumber) {
        final IssueEventService service = ServiceGenerator.createService(context, IssueEventService.class);
        return RxPageUtil
                .getAllPages(new GitHubRequest<Response<Page<IssueEvent>>>() {
                    @Override
                    public Single<Response<Page<IssueEvent>>> execute(int page) {
                        return service.getIssueEvents(owner, name, issueNumber, page);
                    }
                }, 1)
                .flatMap(new Function<Page<IssueEvent>, ObservableSource<IssueEvent>>() {
                    @Override
                    public ObservableSource<IssueEvent> apply(@NonNull Page<IssueEvent> page) throws Exception {
                        return Observable.fromIterable(page.items());
                    }
                })
                .toList();
    }

    private Single<PullRequest> getPullRequest(final String owner, final String name, final int issueNumber) {
        return ServiceGenerator.createService(context, PullRequestService.class)
                .getPullRequest(owner, name, issueNumber)
                .map(new Function<Response<PullRequest>, PullRequest>() {
                    @Override
                    public PullRequest apply(@NonNull Response<PullRequest> response) throws Exception {
                        return response.body();
                    }
                });
    }

    private Single<List<GitHubComment>> getAllComments(final String owner, final String name, final Issue issue) {
        if (issue.comments() <= 0) {
            Single.just(Collections.emptyList());
        }

        final IssueCommentService service = ServiceGenerator.createService(context, IssueCommentService.class);
        return RxPageUtil
                .getAllPages(new GitHubRequest<Response<Page<GitHubComment>>>() {
                    @Override
                    public Single<Response<Page<GitHubComment>>> execute(int page) {
                        return service.getIssueComments(owner, name, issue.number(), page);
                    }
                }, 1)
                .flatMap(new Function<Page<GitHubComment>, ObservableSource<GitHubComment>>() {
                    @Override
                    public ObservableSource<GitHubComment> apply(@NonNull Page<GitHubComment> page) throws Exception {
                        return Observable.fromIterable(page.items());
                    }
                })
                .toList();
    }

}

package com.hdsx.mypockethub.ui.issue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.Intents.Builder;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.issue.IssueStore;
import com.hdsx.mypockethub.core.issue.IssueUtils;
import com.hdsx.mypockethub.ui.FragmentProvider;
import com.hdsx.mypockethub.ui.PagerActivity;
import com.hdsx.mypockethub.ui.ViewPager;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.hdsx.mypockethub.util.InfoUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.hdsx.mypockethub.Intents.EXTRA_ISSUE_NUMBERS;
import static com.hdsx.mypockethub.Intents.EXTRA_POSITION;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORIES;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY;

public class IssuesViewActivity extends PagerActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.vp_pages)
    ViewPager viewPager;

    private static final String EXTRA_PULL_REQUESTS = "pullRequests";
    private int[] issueNumbers;
    private boolean[] pullRequests;
    private List<Repository> repoIds;
    private Repository repo;
    private boolean canWrite;

    private final AtomicReference<User> user = new AtomicReference<>();

    @Inject
    AvatarLoader avatars;

    @Inject
    IssueStore store;

    private IssuesPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        ButterKnife.bind(this);

        issueNumbers = getIntArrayExtra(EXTRA_ISSUE_NUMBERS);
        pullRequests = getBooleanArrayExtra(EXTRA_PULL_REQUESTS);
        repoIds = getIntent().getParcelableArrayListExtra(EXTRA_REPOSITORIES);
        repo = getParcelableExtra(EXTRA_REPOSITORY);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        App.getAppComponent().inject(this);

        if (repo != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(InfoUtils.createRepoId(repo));
            user.set(repo.owner());
            avatars.bind(actionBar, user);
        }

        if (repo == null) {
            Repository temp = repo != null ? repo : repoIds.get(0);
            ServiceGenerator.createService(this, RepositoryService.class)
                    .getRepository(temp.owner().login(), temp.name())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<Repository>>() {
                        @Override
                        public void accept(@NonNull Response<Repository> response) throws Exception {
                            repositoryLoaded(response.body());
                        }
                    });

        } else {
            repositoryLoaded(repo);
        }
    }

    private void repositoryLoaded(Repository repo) {
        if (issueNumbers.length == 1 && (user.get() == null || user.get().avatarUrl() == null)) {
            avatars.bind(getSupportActionBar(), repo.owner());
        }

        canWrite = repo.permissions() != null && (repo.permissions().admin() || repo.permissions().push());

        invalidateOptionsMenu();
        configurePager();
    }

    private void configurePager() {
        int initialPosition = getIntExtra(EXTRA_POSITION);

        if (repo != null) {
            adapter = new IssuesPagerAdapter(this, repo, issueNumbers, canWrite);
        } else {
            adapter = new IssuesPagerAdapter(this, repoIds, issueNumbers, store, canWrite);
        }
        viewPager.setAdapter(adapter);

        viewPager.setOnPageChangeListener(this);
//        viewPager.scheduleSetItem(initialPosition, this);
        onPageSelected(initialPosition);
    }

    @Override
    protected FragmentProvider getProvider() {
        return null;
    }

    public static Intent createIntent(Issue issue, Repository repository) {
        return createIntent(Collections.singletonList(issue), repository, 0);
    }

    public static Intent createIntent(final Issue issue) {
        return createIntent(Collections.singletonList(issue), 0);
    }

    public static Intent createIntent(Collection<? extends Issue> issues, int position) {
        final int count = issues.size();
        int[] numbers = new int[count];
        boolean[] pullRequests = new boolean[count];
        ArrayList<Repository> repos = new ArrayList<>(count);
        int index = 0;
        for (Issue issue : issues) {
            numbers[index] = issue.number();
            pullRequests[index] = IssueUtils.isPullRequest(issue);
            index++;

            Repository repoId = null;
            Repository issueRepo = issue.repository();
            if (issueRepo != null) {
                User owner = issueRepo.owner();
                if (owner != null) {
                    repoId = InfoUtils.createRepoFromData(owner.login(), issueRepo.name());
                }
            }
            if (repoId == null) {
                repoId = InfoUtils.createRepoFromUrl(issue.htmlUrl());
            }
            repos.add(repoId);
        }

        Builder builder = new Builder("issues.VIEW");
        builder.add(EXTRA_ISSUE_NUMBERS, numbers);
        builder.add(EXTRA_REPOSITORIES, repos);
        builder.add(EXTRA_POSITION, position);
        builder.add(EXTRA_PULL_REQUESTS, pullRequests);
        return builder.toIntent();
    }

    public static Intent createIntent(List<Issue> issues, Repository repository, int position) {
        int[] numbers = new int[issues.size()];
        boolean[] pullRequests = new boolean[issues.size()];
        int index = 0;
        for (Issue issue : issues) {
            numbers[index] = issue.number();
            pullRequests[index] = IssueUtils.isPullRequest(issue);
            index++;
        }
        return new Builder("issues.VIEW")
                .add(EXTRA_ISSUE_NUMBERS, numbers)
                .add(EXTRA_REPOSITORY, repository)
                .add(EXTRA_POSITION, position)
                .add(EXTRA_PULL_REQUESTS, pullRequests).toIntent();
    }

}

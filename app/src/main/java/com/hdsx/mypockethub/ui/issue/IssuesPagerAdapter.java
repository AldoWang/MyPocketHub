package com.hdsx.mypockethub.ui.issue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

import com.hdsx.mypockethub.core.issue.IssueStore;
import com.hdsx.mypockethub.ui.FragmentStatePagerAdapter;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;

import java.util.List;

import static com.hdsx.mypockethub.Intents.EXTRA_CAN_WRITE_REPO;
import static com.hdsx.mypockethub.Intents.EXTRA_ISSUE_NUMBER;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY_NAME;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY_OWNER;
import static com.hdsx.mypockethub.Intents.EXTRA_USER;

public class IssuesPagerAdapter extends FragmentStatePagerAdapter {

    private final Repository repo;

    private final List<Repository> repos;

    private final int[] issues;

    private final SparseArray<IssueFragment> fragments = new SparseArray<>();

    private final IssueStore store;

    private boolean canWrite;

    public IssuesPagerAdapter(AppCompatActivity activity, Repository repository, int[] issueNumbers
            , boolean canWrite) {
        super(activity);
        repos = null;
        repo = repository;
        issues = issueNumbers;
        store = null;
        this.canWrite = canWrite;
    }

    public IssuesPagerAdapter(AppCompatActivity activity, List<Repository> repoIds, int[] issueNumbers
            , IssueStore issueStore, boolean canWrite) {
        super(activity);
        repos = repoIds;
        repo = null;
        issues = issueNumbers;
        store = issueStore;
        this.canWrite = canWrite;
    }

    @Override
    public Fragment getItem(int position) {
        IssueFragment fragment = new IssueFragment();
        Bundle args = new Bundle();
        if (repo != null) {
            args.putString(EXTRA_REPOSITORY_NAME, repo.name());
            User owner = repo.owner();
            args.putString(EXTRA_REPOSITORY_OWNER, owner.login());
            args.putParcelable(EXTRA_USER, owner);
        } else {
            Repository repo = repos.get(position);
            args.putString(EXTRA_REPOSITORY_NAME, repo.name());
            args.putString(EXTRA_REPOSITORY_OWNER, repo.owner().login());
            Issue issue = store.getIssue(repo, issues[position]);
            if (issue != null && issue.user() != null) {
                Repository fullRepo = issue.repository();
                if (fullRepo != null && fullRepo.owner() != null) {
                    args.putParcelable(EXTRA_USER, fullRepo.owner());
                }
            }
        }
        args.putInt(EXTRA_ISSUE_NUMBER, issues[position]);
        args.putBoolean(EXTRA_CAN_WRITE_REPO, canWrite);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return issues.length;
    }

}

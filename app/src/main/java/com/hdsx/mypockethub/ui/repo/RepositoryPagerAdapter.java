package com.hdsx.mypockethub.ui.repo;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ui.FragmentPagerAdapter;
import com.hdsx.mypockethub.ui.code.RepositoryCodeFragment;
import com.hdsx.mypockethub.ui.commit.CommitListFragment;
import com.hdsx.mypockethub.ui.issue.IssuesFragment;

public class RepositoryPagerAdapter extends FragmentPagerAdapter {

    private boolean hasIssues;
    private boolean hasReadme;
    private final Resources resources;
    private RepositoryCodeFragment codeFragment;
    private CommitListFragment commitsFragment;

    public RepositoryPagerAdapter(AppCompatActivity activity, boolean hasIssues, boolean hasReadme) {
        super(activity);
        resources = activity.getResources();
        this.hasIssues = hasIssues;
        this.hasReadme = hasReadme;
    }

    @Override
    public Fragment getItem(int position) {
        position = hasReadme ? position : position + 1;

        switch (position) {
            case 0:
                return new RepositoryReadmeFragment();
            case 1:
                return new RepositoryNewsFragment();
            case 2:
                codeFragment = new RepositoryCodeFragment();
                return codeFragment;
            case 3:
                commitsFragment = new CommitListFragment();
                return commitsFragment;
            case 4:
                return new IssuesFragment();
            default:
                return null;
        }
    }

    public void onDialogResult(int position, int requestCode, int resultCode, Bundle arguments) {
        if (position == getItemCode() && codeFragment != null) {
            codeFragment.onDialogResult(requestCode, resultCode, arguments);
        } else if (position == getItemCommits() && commitsFragment != null) {
            commitsFragment.onDialogResult(requestCode, resultCode, arguments);
        }
    }

    private int getItemCommits() {
        return hasReadme ? 3 : 2;
    }

    public boolean onBackPressed() {
        return codeFragment != null && codeFragment.onBackPressed();
    }

    public int getItemCode() {
        return hasReadme ? 2 : 1;
    }

    @Override
    public int getCount() {
        int count = hasIssues ? 5 : 4;
        count = hasReadme ? count : count - 1;
        return count;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        position = hasReadme ? position : position + 1;
        switch (position) {
            case 0:
                return resources.getString(R.string.tab_readme);
            case 1:
                return resources.getString(R.string.tab_news);
            case 2:
                return resources.getString(R.string.tab_code);
            case 3:
                return resources.getString(R.string.tab_commits);
            case 4:
                return resources.getString(R.string.tab_issues);
            default:
                return null;
        }
    }

}

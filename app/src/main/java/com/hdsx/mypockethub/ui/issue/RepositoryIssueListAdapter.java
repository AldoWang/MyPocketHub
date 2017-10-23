package com.hdsx.mypockethub.ui.issue;

import android.view.LayoutInflater;
import android.view.View;

import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.issue.IssueUtils;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Issue;

public class RepositoryIssueListAdapter extends IssueListAdapter<Issue> {

    private int numberPaintFlags;

    public RepositoryIssueListAdapter(LayoutInflater inflater, Issue[] items, AvatarLoader avatars) {
        super(inflater, R.layout.repo_issue_item, items, avatars);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.tv_issue_number, R.id.tv_issue_title, R.id.iv_avatar,
                R.id.tv_issue_creation, R.id.tv_issue_comments,
                R.id.tv_pull_request_icon, R.id.v_label0, R.id.v_label1, R.id.v_label2,
                R.id.v_label3, R.id.v_label4, R.id.v_label5, R.id.v_label6, R.id.v_label7};
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);
        numberPaintFlags = textView(view, 0).getPaintFlags();
        return view;
    }

    @Override
    protected void update(int i, Issue issue) {
        updateNumber(issue.number(), issue.state(), numberPaintFlags, 0);
        avatars.bind(imageView(2), issue.user());
        setGone(5, !IssueUtils.isPullRequest(issue));
        setText(1, issue.title());
        updateReporter(issue.user().login(), issue.createdAt(), 3);
        setNumber(4, issue.comments());
        updateLabels(issue.labels(), 6);
    }

    @Override
    protected int getNumber(Issue issue) {
        return issue.number();
    }

}

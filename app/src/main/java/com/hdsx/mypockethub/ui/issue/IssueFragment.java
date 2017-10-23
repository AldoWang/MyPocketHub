package com.hdsx.mypockethub.ui.issue;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.issue.FullIssue;
import com.hdsx.mypockethub.core.issue.IssueStore;
import com.hdsx.mypockethub.core.issue.RefreshIssueTask;
import com.hdsx.mypockethub.ui.DialogFragment;
import com.hdsx.mypockethub.ui.HeaderFooterListAdapter;
import com.hdsx.mypockethub.ui.SelectableLinkMovementMethod;
import com.hdsx.mypockethub.ui.comment.CommentListAdapter;
import com.hdsx.mypockethub.util.HttpImageGetter;
import com.hdsx.mypockethub.util.InfoUtils;
import com.hdsx.mypockethub.util.ToastUtils;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueEvent;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hdsx.mypockethub.Intents.EXTRA_CAN_WRITE_REPO;
import static com.hdsx.mypockethub.Intents.EXTRA_ISSUE_NUMBER;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY_NAME;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY_OWNER;
import static com.hdsx.mypockethub.Intents.EXTRA_USER;


public class IssueFragment extends DialogFragment {

    @BindView(android.R.id.list)
    ListView listView;

    @BindView(R.id.pb_loading)
    ProgressBar progress;

    private View headerView;

    private View loadingView;

    private View footerView;

    private TextView stateText;

    private TextView titleText;

    private TextView bodyText;

    private TextView authorText;

    private TextView createdDateText;

    private ImageView creatorAvatar;

    private ViewGroup commitsView;

    private TextView assigneeText;

    private ImageView assigneeAvatar;

    private TextView labelsArea;

    private View milestoneArea;

    private View milestoneProgressArea;

    private TextView milestoneText;

    private MenuItem stateItem;
    private Repository repositoryId;
    private boolean canWrite;
    private User user;
    private int issueNumber;

    @Inject
    IssueStore store;

    @Inject
    HttpImageGetter imageGetter;

    private HeaderFooterListAdapter<CommentListAdapter> adapter;
    private Issue issue;
    private List<Object> items;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        repositoryId = InfoUtils.createRepoFromData(args.getString(EXTRA_REPOSITORY_OWNER)
                , args.getString(EXTRA_REPOSITORY_NAME));
        issueNumber = args.getInt(EXTRA_ISSUE_NUMBER);
        user = args.getParcelable(EXTRA_USER);
        canWrite = args.getBoolean(EXTRA_CAN_WRITE_REPO, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        headerView = inflater.inflate(R.layout.issue_header, null);
        stateText = (TextView) headerView.findViewById(R.id.tv_state);
        titleText = (TextView) headerView.findViewById(R.id.tv_issue_title);
        authorText = (TextView) headerView.findViewById(R.id.tv_issue_author);
        createdDateText = (TextView) headerView.findViewById(R.id.tv_issue_creation_date);
        creatorAvatar = (ImageView) headerView.findViewById(R.id.iv_avatar);
        commitsView = (ViewGroup) headerView.findViewById(R.id.ll_issue_commits);
        assigneeText = (TextView) headerView.findViewById(R.id.tv_assignee_name);
        assigneeAvatar = (ImageView) headerView.findViewById(R.id.iv_assignee_avatar);
        labelsArea = (TextView) headerView.findViewById(R.id.tv_labels);
        milestoneArea = headerView.findViewById(R.id.ll_milestone);
        milestoneText = (TextView) headerView.findViewById(R.id.tv_milestone);
        milestoneProgressArea = headerView.findViewById(R.id.v_closed);
        bodyText = (TextView) headerView.findViewById(R.id.tv_issue_body);
        bodyText.setMovementMethod(SelectableLinkMovementMethod.getInstance());

        loadingView = inflater.inflate(R.layout.loading_item, null);
        footerView = inflater.inflate(R.layout.footer_separator, null);

        adapter = new HeaderFooterListAdapter<>(listView, new CommentListAdapter(inflater, imageGetter));
        listView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        App.getAppComponent().inject(this);
        adapter.addHeader(headerView);
        adapter.addFooter(footerView);

        Issue issue = store.getIssue(repositoryId, issueNumber);

        TextView loadingText = (TextView) loadingView.findViewById(R.id.tv_loading);
        loadingText.setText(R.string.loading_comments);

        if (issue == null || (issue.comments() > 0 && items == null)) {
            adapter.addHeader(loadingView);
        }

        refreshIssue();

        progress.setVisibility(GONE);
        listView.setVisibility(VISIBLE);

    }

    private void refreshIssue() {
        new RefreshIssueTask(getContext(), repositoryId, issueNumber, store)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Predicate<FullIssue>() {
                    @Override
                    public boolean test(@NonNull FullIssue fullIssue) throws Exception {
                        return isUsable();
                    }
                })
                .subscribe(new Consumer<FullIssue>() {
                    @Override
                    public void accept(@NonNull FullIssue fullIssue) throws Exception {
                        issue = fullIssue.getIssue();
                        items = new ArrayList<>();
                        items.addAll(fullIssue.getComments());
                        items.addAll(fullIssue.getEvents());
                        updateList(fullIssue.getIssue(), items);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        ToastUtils.show(R.string.error_issue_load);
                        progress.setVisibility(GONE);
                    }
                });
    }

    private void updateList(Issue issue, List<Object> items) {
        Collections.sort(items, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Date l = getDate(o1);
                Date r = getDate(o2);
                if (l == null && r != null) {
                    return 1;
                } else if (l != null && r == null) {
                    return -1;
                } else if (l == null && r == null) {
                    return 0;
                } else {
                    return l.compareTo(r);
                }
            }

            private Date getDate(Object obj) {
                if (obj instanceof GitHubComment) {
                    return ((GitHubComment) obj).createdAt();
                } else if (obj instanceof IssueEvent) {
                    return ((IssueEvent) obj).createdAt();
                }
                return null;
            }
        });

        adapter.getWrappedAdapter().setItems(items);
        adapter.removeHeader(loadingView);
        adapter.getWrappedAdapter().notifyDataSetChanged();

        updateHeader(issue);
    }

    private void updateHeader(Issue issue) {

    }

}

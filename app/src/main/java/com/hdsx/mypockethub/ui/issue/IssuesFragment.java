package com.hdsx.mypockethub.ui.issue;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.PageIterator;
import com.hdsx.mypockethub.core.PageIterator.GitHubRequest;
import com.hdsx.mypockethub.core.ResourcePager;
import com.hdsx.mypockethub.core.issue.IssueFilter;
import com.hdsx.mypockethub.core.issue.IssuePager;
import com.hdsx.mypockethub.core.issue.IssueStore;
import com.hdsx.mypockethub.persistence.AccountDataManager;
import com.hdsx.mypockethub.ui.PagedItemFragment;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.issues.IssueService;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import retrofit2.Response;

import static android.content.Context.SEARCH_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hdsx.mypockethub.Intents.EXTRA_ISSUE_FILTER;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY;


public class IssuesFragment extends PagedItemFragment<Issue> {

    private Repository repository;
    private IssueFilter filter;

    @Inject
    AvatarLoader avatars;

    @Inject
    IssueStore issueStore;

    @Inject
    AccountDataManager cache;

    private View filterHeader;
    private TextView state;
    private TextView labels;
    private TextView milestone;
    private View assigneeArea;
    private TextView assignee;
    private ImageView assigneeAvatar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        repository = getParcelableExtra(EXTRA_REPOSITORY);
        filter = getParcelableExtra(EXTRA_ISSUE_FILTER);
        App.getAppComponent().inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (filter == null)
            filter = new IssueFilter(repository);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        filterHeader = getActivity().getLayoutInflater().inflate(R.layout.issues_filter_header, null);
        state = (TextView) filterHeader.findViewById(R.id.tv_filter_state);
        labels = (TextView) filterHeader.findViewById(R.id.tv_filter_labels);
        milestone = (TextView) filterHeader.findViewById(R.id.tv_filter_milestone);
        assigneeArea = filterHeader.findViewById(R.id.ll_assignee);
        assignee = (TextView) filterHeader.findViewById(R.id.tv_filter_assignee);
        assigneeAvatar = (ImageView) filterHeader.findViewById(R.id.iv_assignee_avatar);
        updateFilterSummary();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        getListAdapter().addHeader(filterHeader, filter, true);
    }

    @Override
    protected ResourcePager<Issue> createPager() {
        return new IssuePager(issueStore) {
            @Override
            protected PageIterator<Issue> createIterator(int page, int size) {
                return new PageIterator<>(new GitHubRequest<Response<Page<Issue>>>() {
                    @Override
                    public Single<Response<Page<Issue>>> execute(int page1) {
                        return ServiceGenerator.createService(getActivity(), IssueService.class)
                                .getRepositoryIssues(repository.owner().login(), repository.name()
                                        , filter.toFilterMap(), page1);
                    }
                }, page);
            }
        };
    }

    private void updateFilterSummary() {
        if (filter.isOpen()) {
            state.setText(R.string.open_issues);
        } else {
            state.setText(R.string.closed_issues);
        }

        Collection<Label> filterLabels = filter.getLabels();
        if (filterLabels != null && !filterLabels.isEmpty()) {
            LabelDrawableSpan.setText(labels, filterLabels);
            labels.setVisibility(VISIBLE);
        } else {
            labels.setVisibility(GONE);
        }

        Milestone filterMilestone = filter.getMilestone();
        if (filterMilestone != null) {
            milestone.setText(filterMilestone.title());
            milestone.setVisibility(VISIBLE);
        } else {
            milestone.setVisibility(GONE);
        }

        User user = filter.getAssignee();
        if (user != null) {
            avatars.bind(assigneeAvatar, user);
            assignee.setText(user.login());
            assigneeArea.setVisibility(VISIBLE);
        } else {
            assigneeArea.setVisibility(GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_issues, menu);
        MenuItem searchItem = menu.findItem(R.id.m_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        Bundle args = new Bundle();
        args.putParcelable(EXTRA_REPOSITORY, repository);
        searchView.setAppSearchData(args);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable()) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.m_bookmark:
                return true;
            case R.id.m_filter:
                return true;
            case R.id.create_issue:
                return true;
            case R.id.m_refresh:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected SingleTypeAdapter<Issue> createAdapter(List<Issue> items) {
        return new RepositoryIssueListAdapter(getActivity().getLayoutInflater()
                , items.toArray(new Issue[items.size()]), avatars);
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_issues;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_issues_load;
    }

}

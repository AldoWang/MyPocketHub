package com.hdsx.mypockethub.ui.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.issue.IssueEventMatcher;
import com.hdsx.mypockethub.core.repo.RepositoryEventMatcher;
import com.hdsx.mypockethub.ui.PagedItemFragment;
import com.hdsx.mypockethub.ui.commit.CommitCompareViewActivity;
import com.hdsx.mypockethub.ui.commit.CommitViewActivity;
import com.hdsx.mypockethub.ui.issue.IssuesViewActivity;
import com.hdsx.mypockethub.ui.repo.RepositoryViewActivity;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.hdsx.mypockethub.util.ConvertUtils;
import com.hdsx.mypockethub.util.InfoUtils;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Release;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.git.GitComment;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload;
import com.meisolsson.githubsdk.model.payload.PushPayload;
import com.meisolsson.githubsdk.model.payload.ReleasePayload;

import java.util.List;

import javax.inject.Inject;

import static android.content.Intent.ACTION_VIEW;
import static com.meisolsson.githubsdk.model.GitHubEventType.CommitCommentEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.DownloadEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.PushEvent;

public abstract class NewsFragment extends PagedItemFragment<GitHubEvent> {

    @Inject
    AvatarLoader avatars;

    protected final IssueEventMatcher issueMatcher = new IssueEventMatcher();

    protected RepositoryEventMatcher repoWatcher = new RepositoryEventMatcher();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(R.string.no_news);
    }

    @Override
    protected SingleTypeAdapter<GitHubEvent> createAdapter(List<GitHubEvent> items) {
        App.getAppComponent().inject(this);
        return new NewsListAdapter(getActivity().getLayoutInflater(), avatars);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        GitHubEvent event = items.get(position);
        if (DownloadEvent.equals(event.type())) {
            openDownload(event);
            return;
        }

        if (PushEvent.equals(event.type())) {
            openPush(event);
            return;
        }

        Issue issue = issueMatcher.getIssue(event);
        if (issue != null) {
            Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
            viewIssue(issue, repo);
            return;
        }

        if (CommitCommentEvent.equals(event.type())) {
            openCommitComment(event);
            return;
        }

        Repository repo = repoWatcher.getRepository(event);
        if (repo != null) {
            viewRepository(repo);
            return;
        }

    }

    private void viewIssue(Issue issue, Repository repo) {
        if (repo != null) {
            startActivity(IssuesViewActivity.createIntent(issue, repo));
        } else {
            startActivity(IssuesViewActivity.createIntent(issue));
        }
    }

    @Override
    protected boolean onListItemLongClick(ListView listView, View view, int position, long id) {
        if (!isUsable()) {
            return false;
        }

        GitHubEvent event = (GitHubEvent) listView.getItemAtPosition(position);
        final Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
        final User user = event.actor();

        if (repo != null && user != null) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                    .title(R.string.navigate_to)
                    .cancelable(true);

            final MaterialDialog[] dialogHolder = new MaterialDialog[1];

            View contentView = getActivity().getLayoutInflater().inflate(R.layout.navi_dialog, null);
            avatars.bind(((ImageView) contentView.findViewById(R.id.iv_user_avatar)), user);
            avatars.bind(((ImageView) contentView.findViewById(R.id.iv_repo_avatar)), repo.owner());

            ((TextView) contentView.findViewById(R.id.tv_login)).setText(user.login());
            ((TextView) contentView.findViewById(R.id.tv_repo_name)).setText(InfoUtils.createRepoId(repo));
            contentView.findViewById(R.id.ll_user_area).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogHolder[0].dismiss();
                    viewUser(user);
                }
            });
            contentView.findViewById(R.id.ll_repo_area).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogHolder[0].dismiss();
                    viewRepository(repo);
                }
            });

            builder.customView(contentView, false);
            MaterialDialog dialog = builder.build();
            dialogHolder[0] = dialog;
            dialog.show();
            return true;
        }
        return false;
    }

    protected boolean viewUser(User user) {
        return false;
    }

    private void viewRepository(Repository repository) {
        startActivity(RepositoryViewActivity.createIntent(repository));
    }

    private void openCommitComment(GitHubEvent event) {
        Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
        if (repo == null) {
            return;
        }

        if (repo.name().contains("/")) {
            String[] repoId = repo.name().split("/");
            repo = InfoUtils.createRepoFromData(repoId[0], repoId[1]);
        }

        CommitCommentPayload payload = ((CommitCommentPayload) event.payload());
        GitComment comment = payload.comment();
        if (comment == null) {
            return;
        }

        String sha = comment.commitId();
        if (!TextUtils.isEmpty(sha)) {
            startActivity(CommitViewActivity.createIntent(repo, sha));
        }
    }

    private void openPush(GitHubEvent event) {
        Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
        if (repo == null) {
            return;
        }

        PushPayload payload = ((PushPayload) event.payload());
        List<GitCommit> commits = payload.commits();
        if (commits.isEmpty()) {
            return;
        }

        if (commits.size() > 1) {
            String base = payload.before();
            String head = payload.head();
            if (!TextUtils.isEmpty(base) && !TextUtils.isEmpty(head)) {
                startActivity(CommitCompareViewActivity.createIntent(repo, base, head));
            }
        } else {
            GitCommit commit = commits.get(0);
            String sha = commit != null ? commit.sha() : null;
            if (!TextUtils.isEmpty(sha)) {
                startActivity(CommitViewActivity.createIntent(repo, sha));
            }
        }
    }

    private void openDownload(GitHubEvent event) {
        Release release = ((ReleasePayload) event.payload()).release();
        if (release == null)
            return;

        String url = release.htmlUrl();
        if (TextUtils.isEmpty(url))
            return;

        Intent intent = new Intent(ACTION_VIEW, Uri.parse(url));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        startActivity(intent);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_news_load;
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_news;
    }

}

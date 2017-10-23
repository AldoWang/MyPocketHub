package com.hdsx.mypockethub.ui.user;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.payload.CreatePayload;
import com.meisolsson.githubsdk.model.payload.GistPayload;
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload;
import com.meisolsson.githubsdk.model.payload.IssuesPayload;

import static com.meisolsson.githubsdk.model.GitHubEventType.CommitCommentEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.CreateEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.DeleteEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.DownloadEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.FollowEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.ForkEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.GistEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.GollumEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.IssueCommentEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.IssuesEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.MemberEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.PublicEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.PullRequestEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.PullRequestReviewCommentEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.PushEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.TeamAddEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.WatchEvent;

/**
 * Adapter for a list of news events
 */
public class NewsListAdapter extends SingleTypeAdapter<GitHubEvent> {

    private final IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(this);

    public static boolean isValid(final GitHubEvent event) {
        if (event == null || event.payload() == null) {
            return false;
        }

        final GitHubEventType type = event.type();

        return CommitCommentEvent.equals(type) //
                || (CreateEvent.equals(type) //
                && ((CreatePayload) event.payload()).refType() != null) //
                || DeleteEvent.equals(type) //
                || DownloadEvent.equals(type) //
                || FollowEvent.equals(type) //
                || ForkEvent.equals(type) //
                || (GistEvent.equals(type)
                && ((GistPayload) event.payload()).gist() != null)
                || GollumEvent.equals(type) //
                || (IssueCommentEvent.equals(type) //
                && ((IssueCommentPayload) event.payload()).issue() != null) //
                || (IssuesEvent.equals(type) //
                && ((IssuesPayload) event.payload()).issue() != null) //
                || MemberEvent.equals(type) //
                || PublicEvent.equals(type) //
                || PullRequestEvent.equals(type) //
                || PullRequestReviewCommentEvent.equals(type) //
                || PushEvent.equals(type) //
                || TeamAddEvent.equals(type) //
                || WatchEvent.equals(type);
    }

    private final AvatarLoader avatars;

    public NewsListAdapter(LayoutInflater inflater, GitHubEvent[] elements, AvatarLoader avatars) {
        super(inflater, R.layout.news_item);

        this.avatars = avatars;
        setItems(elements);
    }

    public NewsListAdapter(LayoutInflater inflater, AvatarLoader avatars) {
        this(inflater, null, avatars);
    }

    @Override
    public long getItemId(final int position) {
        final String id = String.valueOf(getItem(position).id());
        return !TextUtils.isEmpty(id) ? id.hashCode() : super.getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.iv_avatar, R.id.tv_event, R.id.tv_event_details, R.id.tv_event_icon
                , R.id.tv_event_date};
    }

    @Override
    protected void update(int position, GitHubEvent event) {
        iconAndViewTextManager.update(position, event);
    }

    public AvatarLoader getAvatars() {
        return avatars;
    }

    ImageView imageViewAgent(int childViewIndex) {
        return this.imageView(childViewIndex);
    }

    TextView setTextAgent(int childViewIndex, CharSequence text) {
        return this.setText(childViewIndex, text);
    }

    View setGoneAgent(int childViewIndex, boolean gone) {
        return this.setGone(childViewIndex, gone);
    }
}

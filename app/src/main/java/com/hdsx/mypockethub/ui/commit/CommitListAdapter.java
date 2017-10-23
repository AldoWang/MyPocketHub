package com.hdsx.mypockethub.ui.commit;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.commit.CommitUtils;
import com.hdsx.mypockethub.ui.StyledText;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Commit;

import java.util.Collection;


public class CommitListAdapter extends SingleTypeAdapter<Commit> {

    private AvatarLoader avatars;

    public CommitListAdapter(int viewId, LayoutInflater inflater, Collection<Commit> elements
            , AvatarLoader avatars) {
        super(inflater, viewId);
        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(int position) {
        String sha = getItem(position).sha();
        if (!TextUtils.isEmpty(sha)) {
            return sha.hashCode();
        } else {
            return super.getItemId(position);
        }
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.tv_commit_id, R.id.tv_commit_author, R.id.iv_avatar,
                R.id.tv_commit_message, R.id.tv_commit_comments};
    }

    @Override
    protected void update(int i, Commit commit) {
        setText(0, CommitUtils.abbreviate(commit.sha()));
        StyledText authorText = new StyledText();
        authorText.bold(CommitUtils.getAuthor(commit));
        authorText.append(' ');
        authorText.append(CommitUtils.getAuthorDate(commit));
        setText(1, authorText);

        CommitUtils.bindAuthor(commit, avatars, imageView(2));
        setText(3, commit.commit().message());
        setText(4, CommitUtils.getCommentCount(commit));
    }

}

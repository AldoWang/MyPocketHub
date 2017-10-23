package com.hdsx.mypockethub.ui.comment;

import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.util.HttpImageGetter;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.IssueEvent;

import java.util.Collection;


public class CommentListAdapter extends MultiTypeAdapter {

    private HttpImageGetter imageGetter;

    public CommentListAdapter(LayoutInflater inflater, HttpImageGetter imageGetter) {
        super(inflater);
        this.imageGetter = imageGetter;
    }

    @Override
    protected int getChildLayoutId(int type) {
        if (type == 0)
            return R.layout.comment_item;
        else
            return R.layout.comment_event_item;
    }

    @Override
    protected int[] getChildViewIds(int type) {
        if (type == 0) {
            return new int[]{R.id.tv_comment_body, R.id.tv_comment_author,
                    R.id.tv_comment_date, R.id.iv_avatar, R.id.iv_more};
        } else {
            return new int[]{R.id.tv_event_icon, R.id.tv_event, R.id.iv_avatar};
        }
    }

    @Override
    protected void update(int position, Object item, int type) {
        if (type == 0) {
            updateComment((GitHubComment) item);
        } else {
            updateEvent((IssueEvent) item);
        }
    }

    private void updateEvent(IssueEvent item) {

    }

    private void updateComment(GitHubComment comment) {
//        imageGetter.bind(textView(0), comment.body(), comment.id());
    }

    public MultiTypeAdapter setItems(Collection<Object> items) {
        if (items == null)
            return this;

        return setItems(items.toArray());
    }

    public MultiTypeAdapter setItems(Object[] items) {
        if (items == null)
            return this;

        for (Object item : items) {
            if (item instanceof GitHubComment) {
                addItem(0, item);
            } else if (item instanceof IssueEvent) {
                addItem(1, item);
            }
        }

        notifyDataSetChanged();
        return this;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

}

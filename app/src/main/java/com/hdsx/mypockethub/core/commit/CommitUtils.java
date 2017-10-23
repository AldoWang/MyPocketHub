package com.hdsx.mypockethub.core.commit;

import android.text.TextUtils;
import android.widget.ImageView;

import com.hdsx.mypockethub.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.git.GitUser;

import java.text.NumberFormat;
import java.util.Date;

public class CommitUtils {

    private static final int LENGTH = 10;

    private static final NumberFormat FORMAT = NumberFormat.getIntegerInstance();

    public static String getName(String path) {
        if (TextUtils.isEmpty(path))
            return path;

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash + 1 < path.length())
            return path.substring(lastSlash + 1);
        else
            return path;
    }

    public static String abbreviate(String id) {
        if (!TextUtils.isEmpty(id) && id.length() > LENGTH) {
            return id.substring(0, LENGTH);
        } else {
            return id;
        }
    }

    public static String getAuthor(Commit commit) {
        User author = commit.author();
        if (author != null)
            return author.login();

        GitCommit rawCommit = commit.commit();
        if (rawCommit == null)
            return null;

        GitUser commitAuthor = rawCommit.author();
        return commitAuthor != null ? commitAuthor.name() : null;
    }

    public static Date getAuthorDate(final Commit commit) {
        GitCommit rawCommit = commit.commit();
        if (rawCommit == null) {
            return null;
        }

        GitUser commitAuthor = rawCommit.author();
        return commitAuthor != null && commitAuthor.date() != null ? commitAuthor.date() : null;
    }

    public static ImageView bindAuthor(final Commit commit, final AvatarLoader avatars, final ImageView view) {
        User author = commit.author();
        if (author != null) {
            avatars.bind(view, author);
        }

        return view;
    }

    public static String getCommentCount(final Commit commit) {
        final GitCommit rawCommit = commit.commit();
        if (rawCommit != null) {
            return FORMAT.format(rawCommit.commentCount());
        } else {
            return "0";
        }
    }

}

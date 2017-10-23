package com.hdsx.mypockethub.ui.commit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hdsx.mypockethub.Intents.Builder;
import com.hdsx.mypockethub.R;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;

import java.util.Collection;

import static com.hdsx.mypockethub.Intents.EXTRA_BASES;
import static com.hdsx.mypockethub.Intents.EXTRA_POSITION;

public class CommitViewActivity extends AppCompatActivity {

    public static Intent createIntent(Repository repository, String id) {
        return createIntent(repository, 0, id);
    }

    public static Intent createIntent(final Repository repository, final int position, final String... ids) {
        Builder builder = new Builder("commits.VIEW");
        builder.add(EXTRA_POSITION, position);
        builder.add(EXTRA_BASES, ids);
        builder.repo(repository);
        return builder.toIntent();
    }

    public static Intent createIntent(final Repository repository, final int position, final Collection<Commit> commits) {
        String[] ids = new String[commits.size()];
        int index = 0;
        for (Commit commit : commits) {
            ids[index++] = commit.sha();
        }
        return createIntent(repository, position, ids);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_view);
    }
}

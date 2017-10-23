package com.hdsx.mypockethub.ui.commit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hdsx.mypockethub.Intents;
import com.hdsx.mypockethub.R;
import com.meisolsson.githubsdk.model.Repository;

import static com.hdsx.mypockethub.Intents.EXTRA_BASE;
import static com.hdsx.mypockethub.Intents.EXTRA_HEAD;

public class CommitCompareViewActivity extends AppCompatActivity {

    public static Intent createIntent(Repository repository, String base, String head) {
        Intents.Builder builder = new Intents.Builder("commits.compare.VIEW");
        builder.add(EXTRA_BASE, base);
        builder.add(EXTRA_HEAD, head);
        builder.repo(repository);
        return builder.toIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_compare_view);
    }

}

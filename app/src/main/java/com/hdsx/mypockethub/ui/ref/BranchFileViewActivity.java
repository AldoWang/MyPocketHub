package com.hdsx.mypockethub.ui.ref;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hdsx.mypockethub.Intents;
import com.hdsx.mypockethub.R;

public class BranchFileViewActivity extends AppCompatActivity {

    public static Intent createIntent() {
        return new Intents.Builder("branch.file.VIEW").toIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_file_view);
    }

}

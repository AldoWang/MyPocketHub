package com.hdsx.mypockethub.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;


public abstract class BaseActivity extends AppCompatActivity implements DialogResultListener {

    protected <V extends Parcelable> V getParcelableExtra(String name) {
        return getIntent().getParcelableExtra(name);
    }

    protected int getIntExtra(String name) {
        return getIntent().getIntExtra(name, -1);
    }

    protected int[] getIntArrayExtra(String name) {
        return getIntent().getIntArrayExtra(name);
    }

    protected boolean[] getBooleanArrayExtra(String name) {
        return getIntent().getBooleanArrayExtra(name);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {

    }

}

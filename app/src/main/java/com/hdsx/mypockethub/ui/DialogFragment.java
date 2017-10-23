package com.hdsx.mypockethub.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;


public abstract class DialogFragment extends Fragment implements DialogResultListener {

    protected boolean isUsable() {
        return getActivity() != null;
    }

    protected <V extends Parcelable> V getParcelableExtra(String name) {
        Activity activity = getActivity();
        if (activity != null) {
            return activity.getIntent().getParcelableExtra(name);
        } else {
            return null;
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {

    }

}

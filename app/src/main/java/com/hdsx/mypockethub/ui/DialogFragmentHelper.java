package com.hdsx.mypockethub.ui;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;

import static android.app.Activity.RESULT_CANCELED;

public abstract class DialogFragmentHelper extends DialogFragment implements OnClickListener {

    private static final String ARG_TITLE = "title";

    private static final String ARG_MESSAGE = "message";

    private static final String ARG_REQUEST_CODE = "requestCode";

    protected MaterialDialog.Builder createDialogBuilder() {
        return new MaterialDialog.Builder(getActivity())
                .title(getTitle())
                .content(getContent())
                .cancelable(true)
                .cancelListener(this);
    }

    private String getContent() {
        return getArguments().getString(ARG_MESSAGE);
    }

    private String getTitle() {
        return getArguments().getString(ARG_TITLE);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        onResult(RESULT_CANCELED);
    }

    protected void onResult(int resultCode) {
        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null) {
            Bundle args = getArguments();
            if (args != null) {
                activity.onDialogResult(args.getInt(ARG_REQUEST_CODE), resultCode, args);
            }
        }
    }

    protected static Bundle createArguments(String title, String msg, int requestCode) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, msg);
        args.putInt(ARG_REQUEST_CODE, requestCode);
        return args;
    }

    protected static void show(FragmentActivity activity, DialogFragmentHelper fragment, Bundle arguments, String tag) {
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment current = manager.findFragmentByTag(tag);
        if (current != null)
            transaction.remove(current);
        fragment.setArguments(arguments);
        fragment.show(manager, tag);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
    }

}

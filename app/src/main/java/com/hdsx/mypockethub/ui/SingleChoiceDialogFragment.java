package com.hdsx.mypockethub.ui;

import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

public abstract class SingleChoiceDialogFragment extends DialogFragmentHelper implements OnClickListener {

    public static final String ARG_SELECTED = "selected";

    protected static final String ARG_CHOICES = "choices";

    protected static final String ARG_SELECTED_CHOICE = "selectedChoice";

    protected static final String TAG = "single_choice_dialog";

    protected static void show(BaseActivity activity, int requestCode, String title
            , String msg, ArrayList<? extends Parcelable> choices, int selectedChoice, DialogFragmentHelper fragment) {
        Bundle arguments = createArguments(title, msg, requestCode);
        arguments.putParcelableArrayList(ARG_CHOICES, choices);
        arguments.putInt(ARG_SELECTED_CHOICE, selectedChoice);
        show(activity, fragment, arguments, TAG);
    }

}

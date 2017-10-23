package com.hdsx.mypockethub.ui.ref;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.ref.RefUtils;
import com.hdsx.mypockethub.ui.BaseActivity;
import com.hdsx.mypockethub.ui.SingleChoiceDialogFragment;
import com.meisolsson.githubsdk.model.git.GitReference;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;

public class RefDialogFragment extends SingleChoiceDialogFragment implements OnClickListener {

    public static void show(BaseActivity activity, int requestCode, final String title
            , final String message, ArrayList<GitReference> choices, final int selectedChoice) {
        show(activity, requestCode, title, message, choices, selectedChoice, new RefDialogFragment());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        final MaterialDialog.Builder builder = createDialogBuilder()
                .negativeText(R.string.cancel)
                .onNegative(new SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onClick1(dialog, BUTTON_NEGATIVE);
                    }
                });

        LayoutInflater inflater = activity.getLayoutInflater();

        ListView listView = (ListView) inflater.inflate(R.layout.dialog_list_view, null);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClick(getDialog(), position);
            }
        });

        List<GitReference> choices = getChoices();
        int selected = arguments.getInt(ARG_SELECTED_CHOICE);
        RefDialogAdapter adapter = new RefDialogAdapter(inflater
                , choices.toArray(new GitReference[choices.size()]), selected);
        listView.setAdapter(adapter);
        if (selected >= 0) {
            listView.setSelection(selected);
        }

        builder.customView(listView, false);
        return builder.build();
    }

    private List<GitReference> getChoices() {
        return getArguments().getParcelableArrayList(ARG_CHOICES);
    }

    public void onClick1(DialogInterface dialog, int which) {
        if (which == BUTTON_NEGATIVE)
            return;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        switch (which) {
            default:
                getArguments().putParcelable(ARG_SELECTED, getChoices().get(which));
                onResult(RESULT_OK);
                break;
        }
    }

    public static GitReference getSelected(Bundle arguments) {
        return arguments.getParcelable(ARG_SELECTED);
    }

    class RefDialogAdapter extends SingleTypeAdapter<GitReference> {

        private int selected;

        public RefDialogAdapter(LayoutInflater inflater, GitReference[] items, int selected) {
            super(inflater, R.layout.ref_item);
            this.selected = selected;
            setItems(items);
        }

        @Override
        protected int[] getChildViewIds() {
            return new int[]{R.id.tv_ref_icon, R.id.tv_ref, R.id.rb_selected};
        }

        @Override
        protected void update(int i, GitReference reference) {
            if (RefUtils.isTag(reference))
                setText(0, R.string.icon_tag);
            else
                setText(0, R.string.icon_fork);
            setText(1, RefUtils.getName(reference.ref()));
            setChecked(2, i == selected);
        }

    }

}

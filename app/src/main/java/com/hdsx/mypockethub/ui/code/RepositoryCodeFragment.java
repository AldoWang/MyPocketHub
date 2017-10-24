package com.hdsx.mypockethub.ui.code;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.code.FullTree;
import com.hdsx.mypockethub.core.code.FullTree.Entry;
import com.hdsx.mypockethub.core.code.FullTree.Folder;
import com.hdsx.mypockethub.core.code.RefreshTreeTask;
import com.hdsx.mypockethub.core.ref.RefUtils;
import com.hdsx.mypockethub.ui.BaseActivity;
import com.hdsx.mypockethub.ui.DialogFragment;
import com.hdsx.mypockethub.ui.HeaderFooterListAdapter;
import com.hdsx.mypockethub.ui.StyledText;
import com.hdsx.mypockethub.ui.ref.BranchFileViewActivity;
import com.hdsx.mypockethub.ui.ref.CodeTreeAdapter;
import com.hdsx.mypockethub.ui.ref.RefDialog;
import com.hdsx.mypockethub.ui.ref.RefDialogFragment;
import com.hdsx.mypockethub.ui.view.OcticonTextView;
import com.hdsx.mypockethub.util.ToastUtils;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitReference;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY;
import static com.hdsx.mypockethub.RequestCodes.REF_UPDATE;

public class RepositoryCodeFragment extends DialogFragment implements OnItemClickListener {

    @BindView(android.R.id.list)
    ListView listView;

    @BindView(R.id.pb_loading)
    ProgressBar progress;

    @BindView(R.id.tv_branch_icon)
    OcticonTextView branchIconView;

    @BindView(R.id.tv_branch)
    TextView branchView;

    @BindView(R.id.rl_branch)
    View branchFooterView;

    private static final String TAG = "RepositoryCodeFragment";

    private Repository repository;

    private Folder folder;

    private FullTree tree;
    private HeaderFooterListAdapter<CodeTreeAdapter> adapter;
    private View pathHeaderView;
    private TextView pathView;
    private boolean pathShowing;
    private RefDialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        repository = getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_repo_code, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        listView.setOnItemClickListener(this);

        Activity activity = getActivity();
        adapter = new HeaderFooterListAdapter<>(listView, new CodeTreeAdapter(activity));

        pathHeaderView = activity.getLayoutInflater().inflate(R.layout.path_item, null);
        pathView = (TextView) pathHeaderView.findViewById(R.id.tv_path);
        pathView.setMovementMethod(LinkMovementMethod.getInstance());

        if (pathShowing) {
            adapter.addHeader(pathHeaderView);
        }
        listView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (tree == null || folder == null) {
            refreshTree(null);
        } else {
            setFolder(tree, folder);
        }
    }

    private void refreshTree(final GitReference reference) {
        showLoading(true);
        new RefreshTreeTask(getContext(), repository, reference)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FullTree>() {
                    @Override
                    public void accept(FullTree tree) throws Exception {
                        if (folder == null || folder.parent == null) {
                            setFolder(tree, tree.root);
                        } else {
                            Folder current = folder;
                            LinkedList<Folder> stack = new LinkedList<>();
                            while (current.parent != null) {
                                stack.add(current);
                                current = current.parent;
                            }

                            Folder refreshed = tree.root;
                            while (!stack.isEmpty()) {
                                refreshed = refreshed.folders.get(stack.removeFirst().name);
                                if (refreshed == null)
                                    break;
                            }

                            if (refreshed != null) {
                                setFolder(tree, refreshed);
                            } else {
                                setFolder(tree, tree.root);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        Log.d(TAG, "Exception loading tree", e);
                        showLoading(false);
                        ToastUtils.show(R.string.error_code_load);
                    }
                });
    }

    private void setFolder(final FullTree tree, final Folder folder) {
        this.folder = folder;
        this.tree = tree;

        showLoading(false);
        branchView.setText(tree.branch);
        if (RefUtils.isTag(tree.reference)) {
            branchIconView.setText(R.string.icon_tag);
        } else {
            branchIconView.setText(R.string.icon_fork);
        }
        adapter.getWrappedAdapter().setIndented(folder.entry != null);

        if (folder.entry != null) {
            int textLightColor = getResources().getColor(R.color.text_light);
            StyledText text = new StyledText();
            final String[] segments = folder.entry.path().split("/");
            for (int i = 0; i < segments.length - 1; i++) {
                final int index = i;
                text.url(segments[i], new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Folder clicked = folder;
                        for (int j = index; j < segments.length - 1; j++) {
                            clicked = clicked.parent;
                            if (clicked == null) {
                                return;
                            }
                        }
                        setFolder(tree, clicked);
                    }
                }).append(' ').foreground('/', textLightColor).append(' ');
            }
            text.bold(segments[segments.length - 1]);
            pathView.setText(text);
            if (!pathShowing) {
                adapter.addHeader(pathHeaderView);
                pathShowing = true;
            }
        } else {
            if (pathShowing) {
                adapter.removeHeader(pathHeaderView);
                pathShowing = false;
            }
        }

        adapter.getWrappedAdapter().setItems(folder);
        listView.setSelection(0);
    }

    @OnClick(R.id.rl_branch)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_branch:
                switchBranches();
                break;
        }
    }

    private void switchBranches() {
        if (tree == null)
            return;

        if (dialog == null)
            dialog = new RefDialog((BaseActivity) getActivity(), REF_UPDATE, repository);
        dialog.show(tree.reference);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Entry entry = (Entry) parent.getItemAtPosition(position);
        if (entry instanceof Folder) {
            setFolder(tree, (Folder) entry);
        } else {
            startActivity(BranchFileViewActivity.createIntent());
        }
    }

    private void showLoading(boolean loading) {
        if (loading) {
            progress.setVisibility(VISIBLE);
            listView.setVisibility(GONE);
            branchFooterView.setVisibility(GONE);
        } else {
            progress.setVisibility(GONE);
            listView.setVisibility(VISIBLE);
            branchFooterView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_refresh:
                if (tree != null) {
                    GitReference ref = GitReference.builder()
                            .ref(tree.reference.ref())
                            .build();
                    refreshTree(ref);
                } else {
                    refreshTree(null);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        super.onDialogResult(requestCode, resultCode, arguments);
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REF_UPDATE:
                refreshTree(RefDialogFragment.getSelected(arguments));
                break;
        }
    }

    public boolean onBackPressed() {
        if (folder != null && folder.parent != null) {
            setFolder(tree, folder.parent);
            return true;
        }
        return false;
    }

}

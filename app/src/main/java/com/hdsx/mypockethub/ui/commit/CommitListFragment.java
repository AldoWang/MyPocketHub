package com.hdsx.mypockethub.ui.commit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ThrowableLoader;
import com.hdsx.mypockethub.core.PageIterator;
import com.hdsx.mypockethub.core.PageIterator.GitHubRequest;
import com.hdsx.mypockethub.core.ResourcePager;
import com.hdsx.mypockethub.core.commit.CommitStore;
import com.hdsx.mypockethub.core.ref.RefUtils;
import com.hdsx.mypockethub.ui.BaseActivity;
import com.hdsx.mypockethub.ui.ItemListFragment;
import com.hdsx.mypockethub.ui.PagedItemFragment;
import com.hdsx.mypockethub.ui.ref.RefDialog;
import com.hdsx.mypockethub.ui.ref.RefDialogFragment;
import com.hdsx.mypockethub.ui.view.OcticonTextView;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Single;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY;
import static com.hdsx.mypockethub.RequestCodes.REF_UPDATE;

public class CommitListFragment extends PagedItemFragment<Commit> {

    @BindView(R.id.tv_branch)
    TextView branchView;

    @BindView(R.id.rl_branch)
    View branchFooterView;

    @BindView(R.id.tv_branch_icon)
    OcticonTextView branchIconView;

    private Repository repository;

    private String ref;

    @Inject()
    AvatarLoader avatars;

    @Inject
    CommitStore commitStore;
    private RefDialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        repository = activity.getIntent().getParcelableExtra(EXTRA_REPOSITORY);
        App.getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commit_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(R.string.no_commits);
    }

    @Override
    protected ResourcePager<Commit> createPager() {
        return new CommitPager(repository, commitStore) {

            String last;

            @Override
            protected Commit register(Commit commit) {
                List<Commit> parents = commit.parents();
                if (parents != null && !parents.isEmpty())
                    last = parents.get(0).sha();
                else
                    last = null;
                return super.register(commit);
            }

            @Override
            protected PageIterator<Commit> createIterator(int page, int size) {
                return new PageIterator<>(new GitHubRequest<Response<Page<Commit>>>() {
                    @Override
                    public Single<Response<Page<Commit>>> execute(int page1) {
                        RepositoryCommitService service = ServiceGenerator.createService(getContext()
                                , RepositoryCommitService.class);
                        if (page1 > 1 || ref == null) {
                            return service.getCommits(repository.owner().login(), repository.name(), last, page1);
                        } else {
                            return service.getCommits(repository.owner().login(), repository.name(), ref, page1);
                        }
                    }
                }, page);
            }

            @Override
            public ResourcePager<Commit> clear() {
                last = null;
                return super.clear();
            }
        };
    }

    @Override
    public Loader<List<Commit>> onCreateLoader(int id, Bundle args) {
        final ThrowableLoader<List<Commit>> parentLoader = (ThrowableLoader<List<Commit>>) super.onCreateLoader(id, args);
        return new ThrowableLoader<List<Commit>>(getActivity(), items) {

            @Override
            public List<Commit> loadData() throws Exception {
                if (TextUtils.isEmpty(ref)) {
                    String defaultBranch = repository.defaultBranch();
                    if (TextUtils.isEmpty(defaultBranch)) {
                        defaultBranch = ServiceGenerator.createService(getContext(), RepositoryService.class)
                                .getRepository(repository.owner().login(), repository.name())
                                .blockingGet()
                                .body()
                                .defaultBranch();

                        if (TextUtils.isEmpty(defaultBranch))
                            defaultBranch = "master";
                    }
                    ref = defaultBranch;
                }
                return parentLoader.loadData();
            }
        };
    }

    @OnClick(R.id.rl_branch)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_branch:
                switchBranch();
                break;
        }
    }

    private void switchBranch() {
        if (ref == null)
            return;
        if (dialog == null)
            dialog = new RefDialog((BaseActivity) getActivity(), REF_UPDATE, repository);

        GitReference gitReference = GitReference.builder()
                .ref(ref)
                .build();

        dialog.show(gitReference);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        super.onDialogResult(requestCode, resultCode, arguments);
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REF_UPDATE:
                setRef(RefDialogFragment.getSelected(arguments));
                break;
        }
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
    }

    private void setRef(GitReference gitReference) {
        ref = gitReference.ref();
        updateRefLabel();
        refreshWithProgress();
    }

    private void updateRefLabel() {
        branchView.setText(RefUtils.getName(ref));
        if (RefUtils.isTag(ref)) {
            branchIconView.setText(R.string.icon_tag);
        } else {
            branchIconView.setText(R.string.icon_fork);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Commit>> loader, List<Commit> data) {
        super.onLoadFinished(loader, data);
        if (ref != null) {
            updateRefLabel();
        }
    }

    @Override
    protected ItemListFragment<Commit> setListShown(boolean shown, boolean animate) {
        branchFooterView.setVisibility(shown ? VISIBLE : GONE);
        return super.setListShown(shown, animate);
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_commits;
    }

    @Override
    protected SingleTypeAdapter<Commit> createAdapter(List<Commit> items) {
        return new CommitListAdapter(R.layout.commit_item, getActivity().getLayoutInflater(), items
                , avatars);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_commits_load;
    }

}

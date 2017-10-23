package com.hdsx.mypockethub.ui.repo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.Intents.Builder;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ui.TabPagerActivity;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.hdsx.mypockethub.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.activity.StarringService;
import com.meisolsson.githubsdk.service.repositories.RepositoryContentService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY;

public class RepositoryViewActivity extends TabPagerActivity<RepositoryPagerAdapter> {

    private Repository repository;
    private boolean hasReadme;

    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    @Inject
    AvatarLoader avatars;

    private boolean starredStatusChecked;
    private boolean isStarred;

    public static Intent createIntent(Repository repository) {
        return new Builder("repo.VIEW").repo(repository).toIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = getParcelableExtra(EXTRA_REPOSITORY);

        App.getAppComponent().inject(this);

        User owner = repository.owner();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repository.name());
        actionBar.setSubtitle(owner.login());
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (owner.avatarUrl() != null && RepositoryUtils.isComplete(repository)) {
            checkReadme();
        } else {
            avatars.bind(getSupportActionBar(), owner);
            setGone(true);
            pbLoading.setVisibility(VISIBLE);
            ServiceGenerator.createService(this, RepositoryService.class)
                    .getRepository(repository.owner().login(), repository.name())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<Repository>>() {
                        @Override
                        public void accept(Response<Repository> response) throws Exception {
                            repository = response.body();
                            checkReadme();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            ToastUtils.show(R.string.error_repo_load);
                            pbLoading.setVisibility(GONE);
                        }
                    });
        }

    }

    private void checkReadme() {
        pbLoading.setVisibility(VISIBLE);
        ServiceGenerator.createService(this, RepositoryContentService.class)
                .hasReadme(repository.owner().login(), repository.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        hasReadme = response.code() == 200;
                        configurePager();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hasReadme = false;
                        configurePager();
                    }
                });
    }

    private void configurePager() {
        avatars.bind(getSupportActionBar(), repository.owner());
        configureTabPager();
        pbLoading.setVisibility(GONE);
        setGone(false);
        checkStarredRepositoryStatus();
    }

    private void checkStarredRepositoryStatus() {
        starredStatusChecked = false;
        ServiceGenerator.createService(this, StarringService.class)
                .checkIfRepositoryIsStarred(repository.owner().login(), repository.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<Boolean>>() {
                    @Override
                    public void accept(Response<Boolean> response) throws Exception {
                        isStarred = response.code() == 204;
                        starredStatusChecked = true;
                        invalidateOptionsMenu();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected RepositoryPagerAdapter createAdapter() {
        return new RepositoryPagerAdapter(this, repository.hasIssues(), hasReadme);
    }

    @Override
    protected int getContentView() {
        return R.layout.tabbed_progress_pager;
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        adapter.onDialogResult(viewPager.getCurrentItem(), requestCode, resultCode, arguments);
    }

    @Override
    public void onBackPressed() {
        if (adapter == null || viewPager.getCurrentItem() != adapter.getItemCode() || !adapter.onBackPressed())
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_repository, menu);
        return super.onCreateOptionsMenu(menu);
    }

}

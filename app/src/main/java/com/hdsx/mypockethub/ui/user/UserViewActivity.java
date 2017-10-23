package com.hdsx.mypockethub.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.widget.ProgressBar;

import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.Intents;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ui.TabPagerActivity;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.hdsx.mypockethub.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.users.UserFollowerService;
import com.meisolsson.githubsdk.service.users.UserService;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hdsx.mypockethub.Intents.EXTRA_USER;

public class UserViewActivity extends TabPagerActivity<UserPagerAdapter> implements OrganizationSelectionProvider {

    private User user;

    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    @Inject
    AvatarLoader avatars;
    private boolean followingStatusChecked;
    private boolean isFollowing;

    public static Intent createIntent(User user) {
        return new Intents.Builder("user.VIEW").user(user).toIntent();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getIntent().getParcelableExtra(EXTRA_USER);

        App.getAppComponent().inject(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(user.login());

        if (!TextUtils.isEmpty(user.login())) {
            configurePager();
        } else {
            pbLoading.setVisibility(VISIBLE);
            setGone(true);
            ServiceGenerator.createService(getBaseContext(), UserService.class)
                    .getUser(user.login())
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<User>>() {
                        @Override
                        public void accept(@NonNull Response<User> response) throws Exception {
                            user = response.body();
                            configurePager();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            ToastUtils.show(R.string.error_person_load);
                            pbLoading.setVisibility(GONE);
                        }
                    });
        }
    }

    private void configurePager() {
        avatars.bind(getSupportActionBar(), user);
        configureTabPager();
        pbLoading.setVisibility(GONE);
        setGone(false);
        checkFollowingUserStatus();
    }

    private void checkFollowingUserStatus() {
        followingStatusChecked = false;
        ServiceGenerator.createService(this, UserFollowerService.class)
                .isFollowing(user.login())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<Boolean>>() {
                    @Override
                    public void accept(@NonNull Response<Boolean> response) throws Exception {
                        isFollowing = response.code() == 204;
                        followingStatusChecked = true;
                        invalidateOptionsMenu();
                    }
                });
    }

    @Override
    protected UserPagerAdapter createAdapter() {
        return new UserPagerAdapter(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.tabbed_progress_pager;
    }

    @Override
    public User addListener(OrganizationSelectionListener listener) {
        return user;
    }

    @Override
    public OrganizationSelectionProvider removeListener(OrganizationSelectionListener listener) {
        return null;
    }

}

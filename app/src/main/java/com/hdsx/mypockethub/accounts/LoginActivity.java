package com.hdsx.mypockethub.accounts;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hdsx.mypockethub.R;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.core.TokenStore;
import com.meisolsson.githubsdk.model.GitHubToken;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.request.RequestToken;
import com.meisolsson.githubsdk.service.users.UserService;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import retrofit2.Response;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;

public class LoginActivity extends AppCompatActivity {

    public static final String PARAM_AUTHTOKEN_TYPE = "authToken";
    public static final String PARAM_USERNAME = "username";
    public static final String OAUTH_HOST = "www.github.com";
    public static final String INTENT_EXTRA_URL = "url";
    private static int WEBVIEW_REQUEST_CODE = 0;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private MaterialDialog dialog;
    private String clientId;
    private String secret;
    private String redirectUri;
    private String accessToken;
    private String scope;
    private AccountManager manager;
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse;
    private Bundle result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mAccountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }

        clientId = getString(R.string.github_client);
        secret = getString(R.string.github_secret);
        redirectUri = getString(R.string.github_oauth);

        manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType(getString(R.string.account_type));
        if (accounts != null && accounts.length > 0) {
            openMain();
        }
        checkOauthConfig();
    }

    private void checkOauthConfig() {
        if (clientId.equals("dummy_client") || secret.equals("dummy_secret")) {
            Toast.makeText(this, R.string.error_oauth_not_configured, Toast.LENGTH_LONG).show();
        }
    }

    private void openMain() {
        if (dialog != null)
            dialog.dismiss();

//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_login:
                handleLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleLogin() {
        openLoginInBrower();
    }

    private void openLoginInBrower() {
        String initialScope = "user,public_repo,repo,delete_repo,notifications,gist";
        HttpUrl.Builder url = new HttpUrl.Builder()
                .scheme("https")
                .host(OAUTH_HOST)
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", getString(R.string.github_client))
                .addQueryParameter("scope", initialScope);

        Intent intent = new Intent(this, LoginWebViewActivity.class);
        intent.putExtra(INTENT_EXTRA_URL, url.toString());
        startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == WEBVIEW_REQUEST_CODE) {
            onUserLoggedIn(data.getData());
        }
    }

    private void onUserLoggedIn(Uri uri) {
        if (uri != null && uri.getScheme().equals(getString(R.string.github_oauth_scheme))) {
            openLoadingDialog();
            String code = uri.getQueryParameter("code");
            RequestToken token = RequestToken.builder()
                    .clientId(clientId)
                    .clientSecret(secret)
                    .redirectUri(redirectUri)
                    .code(code)
                    .build();

            ServiceGenerator.createAuthService()
                    .getToken(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<GitHubToken>>() {
                        @Override
                        public void accept(@NonNull Response<GitHubToken> response) throws Exception {
                            GitHubToken gitHubToken = response.body();
                            if (gitHubToken.accessToken() != null)
                                endAuth(gitHubToken.accessToken(), gitHubToken.scope());
                            else if (gitHubToken.error() != null) {
                                Toast.makeText(getBaseContext(), gitHubToken.error(), Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
        }
    }

    private void endAuth(final String accessToken, final String scope) {
        this.accessToken = accessToken;
        this.scope = scope;

        dialog.setContent(getString(R.string.loading_user));

        TokenStore.getInstance(this).saveToken(accessToken);
        ServiceGenerator.createService(this, UserService.class)
                .getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<User>>() {
                    @Override
                    public void accept(@NonNull Response<User> response) throws Exception {
                        User user = response.body();
                        Account account = new Account(user.login(), getString(R.string.account_type));
                        Bundle userData = AccountsHelper.buildBundle(user.name(), user.email(), user.avatarUrl(), scope);
                        userData.putString(KEY_AUTHTOKEN, accessToken);
                        manager.addAccountExplicitly(account, null, userData);
                        manager.setAuthToken(account, getString(R.string.account_type), accessToken);

                        result = new Bundle();
                        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                        result.putString(AccountManager.KEY_AUTHTOKEN, accessToken);

                        openMain();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    private void openLoadingDialog() {
        dialog = new MaterialDialog.Builder(this)
                .progress(true, 0)
                .content(R.string.login_activity_authenticating)
                .show();
    }

    @Override
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            if (result != null) {
                mAccountAuthenticatorResponse.onResult(result);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
}

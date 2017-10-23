package com.hdsx.mypockethub.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.util.Logs;
import com.meisolsson.githubsdk.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static com.hdsx.mypockethub.accounts.AccountConstants.ACCOUNT_TYPE;

public class AccountUtils {

    private static boolean HAS_AUTHENTICATOR;
    private static boolean AUTHENTICATOR_CHECKED;
    private static final String TAG = "AccountUtils";

    private static final AtomicInteger UPDATE_COUNT = new AtomicInteger();

    public static boolean isUser(Context context, User user) {
        if (user == null)
            return false;

        String login = user.login();
        if (login == null)
            return false;

        return login.equals(getLogin(context));
    }

    public static String getLogin(final Context context) {
        final Account account = getAccount(context);
        return account != null ? account.name : null;
    }

    public static Account getAccount(final Context context) {
        final Account[] accounts = AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }

    private static class AuthenticatorConflictException extends IOException {
        private static final long serialVersionUID = 641279204734869183L;
    }

    public static Account getAccount(AccountManager manager, final Activity activity) throws AccountsException
            , IOException {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }

        if (activity.isFinishing()) {
            throw new OperationCanceledException();
        }

        Account[] accounts;
        try {
            if (!hasAuthenticator(manager)) {
                throw new AuthenticatorConflictException();
            }

            while ((accounts = getAccounts(manager)).length == 0) {
                Bundle result = manager.addAccount(ACCOUNT_TYPE, null, null, null, activity, null, null).getResult();
                result.getString(KEY_ACCOUNT_NAME);
            }
        } catch (OperationCanceledException e) {
            Logs.e("OperationCanceledException");
            activity.finish();
            throw e;
        } catch (AccountsException e) {
            Logs.e("AccountsException");
            throw e;
        } catch (AuthenticatorConflictException e) {
            Logs.e("AuthenticatorConflictException");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showConflictMessage(activity);
                }
            });
            throw e;
        } catch (IOException e) {
            Logs.e("IOException");
            throw e;
        }

        return accounts[0];
    }

    private static void showConflictMessage(final Activity activity) {
        new MaterialDialog.Builder(activity)
                .title(R.string.authenticator_conflict_title)
                .content(R.string.authenticator_conflict_message)
                .positiveText(android.R.string.ok)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                    }
                })
                .onPositive(new SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        activity.finish();
                    }
                })
                .show();
    }


    public static Account[] getAccounts(AccountManager manager)
            throws AuthenticatorException, OperationCanceledException, IOException {
        AccountManagerFuture<Account[]> future = manager.getAccountsByTypeAndFeatures(ACCOUNT_TYPE
                , null, null, null);
        Account[] accounts = future.getResult();
        if (accounts != null && accounts.length > 0)
            return getPasswordAccessibleAccounts(manager, accounts);
        else
            return new Account[0];
    }

    private static Account[] getPasswordAccessibleAccounts(AccountManager manager, Account[] accounts) throws AuthenticatorConflictException {

        List<Account> accessible = new ArrayList<>(accounts.length);
        boolean exceptionThrown = false;
        for (Account account : accounts) {
            try {
                manager.getPassword(account);
                accessible.add(account);
            } catch (SecurityException e) {
                exceptionThrown = true;
            }
        }

        if (accessible.isEmpty() && exceptionThrown)
            throw new AuthenticatorConflictException();
        return accessible.toArray(new Account[accessible.size()]);
    }

    public static boolean hasAuthenticator(AccountManager manager) {
        if (!AUTHENTICATOR_CHECKED) {
            AuthenticatorDescription[] types = manager.getAuthenticatorTypes();
            if (types != null && types.length > 0) {
                for (AuthenticatorDescription descriptor : types) {
                    if (descriptor != null && ACCOUNT_TYPE.equals(descriptor.type)) {
                        HAS_AUTHENTICATOR = "com.hdsx.mypockethub".equals(descriptor.packageName);
                        break;
                    }
                }
            }
            AUTHENTICATOR_CHECKED = true;
        }
        return HAS_AUTHENTICATOR;
    }

    public static boolean isUnauthorized(Exception e) {
        String message = null;
        if (e instanceof IOException)
            message = e.getMessage();

        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            String causeMessage = cause.getMessage();
            if (!TextUtils.isEmpty(causeMessage))
                message = causeMessage;
        }

        if (TextUtils.isEmpty(message))
            return false;

        if ("Received authentication challenge is null".equals(message)) {
            return true;
        }
        return "No authentication challenges found".equals(message);
    }

    public static boolean updateAccount(Account account, final Activity activity) {
        int count = UPDATE_COUNT.get();
        synchronized (UPDATE_COUNT) {
            if (count != UPDATE_COUNT.get()) {
                return true;
            }

            AccountManager manager = AccountManager.get(activity);
            try {
                if (!hasAuthenticator(manager)) {
                    throw new AuthenticatorConflictException();
                }
                manager.updateCredentials(account, ACCOUNT_TYPE, null, activity, null, null).getResult();
                UPDATE_COUNT.addAndGet(1);
                return true;
            } catch (OperationCanceledException e) {
                Log.d(TAG, "Excepting retrieving account", e);
                activity.finish();
                return false;
            } catch (AccountsException e) {
                Log.d(TAG, "Excepting retrieving account", e);
                return false;
            } catch (AuthenticatorConflictException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showConflictMessage(activity);
                    }
                });
                return false;
            } catch (IOException e) {
                Log.d(TAG, "Excepting retrieving account", e);
                return false;
            }
        }
    }

}

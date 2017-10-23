package com.hdsx.mypockethub;

import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.hdsx.mypockethub.accounts.AccountUtils;
import com.hdsx.mypockethub.accounts.AuthenticatedUserLoader;


public abstract class ThrowableLoader<D> extends AuthenticatedUserLoader<D> {

    private D data;
    private Activity activity;
    private Exception exception;
    private static final String TAG = "ThrowableLoader";

    public ThrowableLoader(Activity activity, D data) {
        super(activity);
        this.activity = activity;
        this.data = data;
    }

    @Override
    protected D loadData(Account account) {
        try {
            return loadData();
        } catch (Exception e) {
            if (AccountUtils.isUnauthorized(e) && AccountUtils.updateAccount(account, activity))
                try {
                    loadData();
                } catch (Exception e2) {
                    e = e2;
                }

            Log.e(TAG, "Exception loading data", e);
            exception = e;
            return data;
        }
    }

    public Exception getException() {
        return exception;
    }

    public abstract D loadData() throws Exception;

    @Override
    protected D getAccountFailureData() {
        return data;
    }

}

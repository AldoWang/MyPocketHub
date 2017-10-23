package com.hdsx.mypockethub.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.app.Activity;

import com.github.kevinsawicki.wishlist.AsyncLoader;

import java.io.IOException;

public abstract class AuthenticatedUserLoader<D> extends AsyncLoader<D> {

    protected Activity activity;

    public AuthenticatedUserLoader(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public D loadInBackground() {
        AccountManager manager = AccountManager.get(activity);
        Account account;
        try {
            account = AccountUtils.getAccount(manager, activity);
        } catch (AccountsException e) {
            return getAccountFailureData();
        } catch (IOException e) {
            return getAccountFailureData();
        }

        return loadData(account);
    }

    protected abstract D loadData(Account account);

    protected abstract D getAccountFailureData();

}

package com.hdsx.mypockethub.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import static android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT;


public class AccountAuthenticatorService extends Service {

    private static AccountAuthenticator AUTHENTICATOR;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return intent.getAction().equals(ACTION_AUTHENTICATOR_INTENT) ? getAccountAuthenticator().getIBinder() : null;
    }

    private AccountAuthenticator getAccountAuthenticator() {
        if (AUTHENTICATOR == null)
            AUTHENTICATOR = new AccountAuthenticator(this);
        return AUTHENTICATOR;
    }

}

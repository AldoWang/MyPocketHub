package com.hdsx.mypockethub.accounts;

import android.os.Bundle;

public class AccountsHelper {

    public static final String USER_PIC = "USER_PIC";
    public static final String USER_URL = "USER_URL";
    private static final String USER_MAIL = "USER_MAIL";
    private static final String USER_NAME = "USER_NAME";

    public static Bundle buildBundle(String name, String mail, String avatar, String url) {
        Bundle userData = new Bundle();

        userData.putString(AccountsHelper.USER_PIC, avatar);
        userData.putString(AccountsHelper.USER_MAIL, mail);
        userData.putString(AccountsHelper.USER_NAME, name);

        if (url != null) {
            userData.putString(AccountsHelper.USER_URL, url);
        }

        return userData;
    }

}

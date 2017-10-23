package com.hdsx.mypockethub.util;

import android.widget.Toast;

import com.hdsx.mypockethub.App;

public class ToastUtils {

    public static void show(int msg) {
        Toast.makeText(App.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

}

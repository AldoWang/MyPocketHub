package com.hdsx.mypockethub.util;

import android.util.Log;

public class Logs {

    private static String TAG = "MyPocket";

    public static void e(String msg) {
        Log.e(TAG, "+++" + msg);
    }

    public static void e(Object obj) {
        Log.e(TAG, "+++" + obj);
    }

    public static void e(boolean msg) {
        Log.e(TAG, "+++" + msg);
    }

    public static void e(int count) {
        Log.e(TAG, "+++" + count);
    }

}

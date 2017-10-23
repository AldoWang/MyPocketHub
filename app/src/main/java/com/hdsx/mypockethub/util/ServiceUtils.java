package com.hdsx.mypockethub.util;

import android.content.res.Resources;
import android.util.TypedValue;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class ServiceUtils {

    public static int getIntPixels(int dp, Resources resources) {
        float pixels = TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) Math.floor(pixels + 0.5f);
    }

    public static float getPixels(final Resources resources, final int dp) {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp,
                resources.getDisplayMetrics());
    }

}

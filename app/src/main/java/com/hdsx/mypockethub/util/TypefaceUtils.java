package com.hdsx.mypockethub.util;

import android.graphics.Paint;
import android.widget.TextView;

import java.util.Arrays;

public class TypefaceUtils {

    public static int getMaxDigits(int... numbers) {
        int max = 1;
        for (int number : numbers) {
            max = Math.max(max, (int) Math.log10(number) + 1);
        }
        return max;
    }

    public static int getWidth(TextView view, int numberOfDigits) {
        Paint paint = new Paint();
        paint.setTypeface(view.getTypeface());
        paint.setTextSize(view.getTextSize());
        char[] text = new char[numberOfDigits];
        Arrays.fill(text, '0');
        return Math.round(paint.measureText(text, 0, numberOfDigits));
    }

}

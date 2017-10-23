package com.hdsx.mypockethub.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class WebView extends android.webkit.WebView {

    private boolean intercept;

    public WebView(Context context) {
        super(context);
    }

    public WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent p_event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent p_event) {
        if (intercept && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(p_event);
    }

    public void startIntercept() {
        intercept = true;
    }

    public void stopIntercept() {
        intercept = false;
    }

}

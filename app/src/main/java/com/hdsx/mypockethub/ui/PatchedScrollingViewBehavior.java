package com.hdsx.mypockethub.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.ScrollingViewBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class PatchedScrollingViewBehavior extends ScrollingViewBehavior {

    public PatchedScrollingViewBehavior() {
    }

    public PatchedScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec
            , int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if (child.getLayoutParams().height == MATCH_PARENT) {
            List<View> dependencies = parent.getDependencies(child);
            if (dependencies.isEmpty())
                return false;

            AppBarLayout appbar = findFirstAppBarLayout(dependencies);
            if (appbar != null && ViewCompat.isLaidOut(appbar)) {
                if (ViewCompat.getFitsSystemWindows(appbar))
                    ViewCompat.setFitsSystemWindows(appbar, true);

                int parentHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                int height = parentHeight - appbar.getMeasuredHeight();
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                return true;
            }
        }

        return false;
    }

    private AppBarLayout findFirstAppBarLayout(List<View> dependencies) {
        for (View view : dependencies) {
            if (view instanceof AppBarLayout)
                return (AppBarLayout) view;
        }
        return null;
    }

}

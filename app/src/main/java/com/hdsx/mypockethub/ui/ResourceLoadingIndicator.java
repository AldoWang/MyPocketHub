package com.hdsx.mypockethub.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hdsx.mypockethub.R;

public class ResourceLoadingIndicator {

    private View view;
    private HeaderFooterListAdapter<?> adapter;
    private boolean showing;

    public ResourceLoadingIndicator(Context context, int loadingResId) {
        view = LayoutInflater.from(context).inflate(R.layout.loading_item, null);
        TextView tvLoading = (TextView) view.findViewById(R.id.tv_loading);
        tvLoading.setText(loadingResId);
    }

    public ResourceLoadingIndicator setList(HeaderFooterListAdapter<?> adapter) {
        this.adapter = adapter;
        adapter.addFooter(view);
        showing = true;
        return this;
    }

    public ResourceLoadingIndicator setVisible(boolean visible) {
        if (showing != visible && adapter != null) {
            if (visible)
                adapter.addFooter(view);
            else
                adapter.removeFooter(view);
        }

        showing = visible;
        return this;
    }

}

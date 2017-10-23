package com.hdsx.mypockethub.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.hdsx.mypockethub.ThrowableLoader;
import com.hdsx.mypockethub.core.ResourcePager;

import java.util.List;

public abstract class PagedItemFragment<E> extends ItemListFragment<E> implements OnScrollListener {

    ResourcePager<E> pager;
    private ResourceLoadingIndicator loadingIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pager = createPager();
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        loadingIndicator = new ResourceLoadingIndicator(getContext(), getLoadingMessage());
        loadingIndicator.setList(getListAdapter());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnScrollListener(this);
        getListView().setFastScrollEnabled(true);
    }

    protected abstract ResourcePager<E> createPager();

    @Override
    public Loader<List<E>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<E>>(getActivity(), items) {
            @Override
            public List<E> loadData() throws Exception {
                pager.next();
                return pager.getResources();
            }
        };
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!isUsable())
            return;

        if (!pager.hasMore())
            return;

        if (getLoaderManager().hasRunningLoaders())
            return;

        if (listView != null && listView.getLastVisiblePosition() >= pager.size())
            showMore();
    }

    private void showMore() {
        refresh();
    }

    @Override
    protected void forceRefresh() {
        pager.clear();
        super.forceRefresh();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onLoadFinished(Loader<List<E>> loader, List<E> data) {
        loadingIndicator.setVisible(pager.hasMore());
        super.onLoadFinished(loader, data);
    }

    protected abstract int getLoadingMessage();

    @Override
    protected void refreshWithProgress() {
        pager.reset();
        pager = createPager();
        super.refreshWithProgress();
    }

}

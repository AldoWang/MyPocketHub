package com.hdsx.mypockethub.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ThrowableLoader;
import com.hdsx.mypockethub.util.ToastUtils;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public abstract class ItemListFragment<E> extends DialogFragment implements OnRefreshListener, LoaderCallbacks<List<E>> {

    @BindView(R.id.swipe_item)
    SwipeRefreshLayout swipeLayout;

    @BindView(android.R.id.list)
    ListView listView;

    @BindView(android.R.id.progress)
    ProgressBar progressBar;

    @BindView(android.R.id.empty)
    TextView emptyView;

    protected List<E> items = Collections.emptyList();

    protected boolean listShown = false;
    private static final String FORCE_REFRESH = "forceRefresh";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(
                R.color.pager_title_background_top_start,
                R.color.pager_title_background_end,
                R.color.text_link,
                R.color.pager_title_background_end);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick((ListView) parent, view, position, id);
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return onListItemLongClick((ListView) parent, view, position, id);
            }
        });

        configureList(getActivity(), getListView());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!items.isEmpty())
            setListShown(true, false);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onRefresh() {
        forceRefresh();
    }

    protected void forceRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        refresh(bundle);
    }

    protected void refresh() {
        refresh(null);
    }

    protected void refresh(Bundle bundle) {
        if (!isUsable()) {
            return;
        }
        getLoaderManager().restartLoader(0, bundle, this);
    }

    private HeaderFooterListAdapter<SingleTypeAdapter<E>> createAdapter() {
        SingleTypeAdapter<E> adapter = createAdapter(items);
        return new HeaderFooterListAdapter<>(getListView(), adapter);
    }

    protected abstract SingleTypeAdapter<E> createAdapter(List<E> items);

    @Override
    public void onLoadFinished(Loader<List<E>> loader, List<E> data) {
        if (!isUsable()) {
            return;
        }

        swipeLayout.setRefreshing(false);
        Exception exception = getException(loader);
        if (exception != null) {
            showError(exception, getErrorMessage(exception));
            showList();
            return;
        }

        items = data;
        getListAdapter().getWrappedAdapter().setItems(data.toArray());
        showList();
    }

    @SuppressWarnings("unchecked")
    protected HeaderFooterListAdapter<SingleTypeAdapter<E>> getListAdapter() {
        if (listView != null)
            return (HeaderFooterListAdapter<SingleTypeAdapter<E>>) listView.getAdapter();
        else
            return null;
    }

    private void showError(Exception exception, int errorMessage) {
        ToastUtils.show(errorMessage);
    }

    protected abstract int getErrorMessage(Exception exception);

    protected ItemListFragment<E> setEmptyText(int resId) {
        if (emptyView != null)
            emptyView.setText(resId);
        return this;
    }

    private void showList() {
        setListShown(true, isResumed());
    }

    private Exception getException(Loader<List<E>> loader) {
        if (loader instanceof ThrowableLoader)
            return ((ThrowableLoader<List<E>>) loader).getException();
        else
            return null;
    }

    @Override
    public void onLoaderReset(Loader<List<E>> loader) {
    }

    protected ItemListFragment<E> setListShown(boolean shown, boolean animate) {
        if (!isUsable())
            return this;

        if (listShown == shown) {
            if (listShown)
                if (items.isEmpty())
                    hide(listView).show(emptyView);
                else
                    hide(emptyView).show(listView);
            return this;
        }

        listShown = shown;
        if (shown)
            if (items.isEmpty())
                hide(listView).hide(progressBar).fadeIn(emptyView, animate).show(emptyView);
            else
                hide(progressBar).hide(emptyView).fadeIn(listView, animate).show(listView);
        else
            hide(listView).hide(emptyView).fadeIn(progressBar, animate).show(progressBar);
        return this;
    }

    private ItemListFragment<E> fadeIn(View view, boolean animate) {
        if (view != null)
            if (animate)
                view.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            else
                view.clearAnimation();
        return this;
    }

    private ItemListFragment<E> hide(final View view) {
        view.setVisibility(GONE);
        return this;
    }

    private ItemListFragment<E> show(final View view) {
        view.setVisibility(VISIBLE);
        return this;
    }

    protected void configureList(Activity activity, ListView listView) {
        listView.setAdapter(createAdapter());
    }

    protected ListView getListView() {
        return listView;
    }

    protected boolean onListItemLongClick(ListView listView, View view, int position, long id) {
        return false;
    }

    protected void onListItemClick(ListView listView, View view, int position, long id) {

    }

    @Override
    public void onDestroyView() {
        listShown = false;
        emptyView = null;
        progressBar = null;
        listView = null;
        super.onDestroyView();
    }

    protected void refreshWithProgress() {
        items.clear();
        setListShown(false);
        refresh();
    }

    protected void setListShown(boolean visible) {
        setListShown(visible, true);
    }

}

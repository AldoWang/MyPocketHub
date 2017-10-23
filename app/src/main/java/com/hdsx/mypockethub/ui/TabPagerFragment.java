package com.hdsx.mypockethub.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdsx.mypockethub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public abstract class TabPagerFragment<V extends PagerAdapter & FragmentProvider> extends PagerFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.sliding_tabs_layout)
    TabLayout tabs;

    @BindView(R.id.vp_pages)
    ViewPager viewPager;

    protected V adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getContentView(), null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        toolbar.setVisibility(GONE);
        viewPager.addOnPageChangeListener(this);
    }

    private int getContentView() {
        return R.layout.pager_with_tabs;
    }

    protected void configureTabPager() {
        createPager();
    }

    protected void createPager() {
        adapter = createAdapter();
        getActivity().invalidateOptionsMenu();
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
    }

    protected abstract V createAdapter();

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }

}


package com.hdsx.mypockethub.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;

import com.hdsx.mypockethub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class TabPagerActivity<V extends PagerAdapter & FragmentProvider>
        extends PagerActivity implements OnPageChangeListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.sliding_tabs_layout)
    TabLayout tabs;

    @BindView(R.id.vp_pages)
    protected ViewPager viewPager;

    protected V adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);

        viewPager.addOnPageChangeListener(this);
        tabs.setupWithViewPager(viewPager);

    }

    protected void configureTabPager() {
        if (adapter == null) {
            createPager();
            updateTabs();
        }
    }

    private void updateTabs() {
        tabs.setupWithViewPager(viewPager);
    }

    private void createPager() {
        adapter = createAdapter();
        invalidateOptionsMenu();
        viewPager.setAdapter(adapter);
    }

    protected TabPagerActivity<V> setGone(boolean gone) {
        if (gone) {
            viewPager.setVisibility(GONE);
            tabs.setVisibility(GONE);
        } else {
            viewPager.setVisibility(VISIBLE);
            tabs.setVisibility(VISIBLE);
        }
        return this;
    }

    protected int getContentView() {
        return R.layout.pager_with_tabs;
    }

    protected abstract V createAdapter();

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }
}

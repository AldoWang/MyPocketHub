package com.hdsx.mypockethub.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdsx.mypockethub.App;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ui.notification.NotificationActivity;
import com.hdsx.mypockethub.ui.repo.OrganizationLoader;
import com.hdsx.mypockethub.ui.user.HomePageFragment;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.meisolsson.githubsdk.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<User>>
        , OnNavigationItemSelectedListener {

    public static final String PREF_USER_LEARNED_DRAWER = "pref_user_learned_drawer";

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ActionBarDrawerToggle toggle;
    private boolean userLearnedDrawer;

    private User org;
    private List<User> orgs;

    @Inject
    AvatarLoader avatars;

    Map<MenuItem, User> menuItemOrganizationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open
                , R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!userLearnedDrawer) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                    userLearnedDrawer = true;
                }
            }
        };

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        App.getAppComponent().inject(this);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new OrganizationLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> orgs) {
        if (orgs.isEmpty())
            return;

        org = orgs.get(0);
        this.orgs = orgs;

        setUpNavigationView();

        Window window = getWindow();
        if (window == null)
            return;

        View view = window.getDecorView();
        if (view == null)
            return;

        view.post(new Runnable() {
            @Override
            public void run() {
                switchFragment(new HomePageFragment(), org);
                if (!userLearnedDrawer)
                    drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void switchFragment(Fragment fragment, User organization) {
        if (organization != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("org", organization);
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setUpNavigationView() {
        if (navigationView != null) {
            setUpHeaderView();
            setUpNavigationMenu();
        }
    }

    private void setUpNavigationMenu() {
        MenuItem organizationContainer = navigationView.getMenu().findItem(R.id.navigation_organizations);
        if (organizationContainer.hasSubMenu()) {
            SubMenu organizationsMenu = organizationContainer.getSubMenu();
            for (int i = 1; i < orgs.size(); i++) {
                User organization = orgs.get(i);
                if (organizationsMenu.findItem(organization.id()) == null) {
                    MenuItem organizationMenuItem = organizationsMenu.add(Menu.NONE, organization.id(), Menu.NONE, organization.name() != null ? organization.name() : organization.login());
                    organizationMenuItem.setIcon(R.drawable.ic_github_organization_black_24dp);
                    //Because of tinting the real image would became a grey block
                    //avatars.bind(organizationMenuItem, organization);
                    menuItemOrganizationMap.put(organizationMenuItem, organization);
                }
            }
        } else {
            throw new IllegalStateException("Menu item " + organizationContainer + " should have a submenu");
        }
    }

    private void setUpHeaderView() {
        ImageView userImage;
        TextView userRealName;
        TextView userName;

        View headerView = navigationView.getHeaderView(0);
        userImage = (ImageView) headerView.findViewById(R.id.user_picture);
        ImageView notificationIcon = (ImageView) headerView.findViewById(R.id.iv_notification);
        userRealName = (TextView) headerView.findViewById(R.id.user_real_name);
        userName = (TextView) headerView.findViewById(R.id.user_name);

        notificationIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
            }
        });

        avatars.bind(userImage, org);
        userName.setText(org.login());

        String name = org.name();
        if (name != null) {
            userRealName.setText(org.name());
        } else {
            userRealName.setVisibility(GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

}

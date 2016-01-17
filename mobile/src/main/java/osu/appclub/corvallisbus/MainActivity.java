package osu.appclub.corvallisbus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Main toolbar
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setup initial UI and Toolbar setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        setSupportActionBar(toolbar);

        //Get our Fragment Manager and begin fragment transaction
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //Load the Favorites Fragment
        FavoritesFragment favoritesFragment = new FavoritesFragment();
        ft.replace(R.id.content_frame, favoritesFragment);
        ft.commit();

        //Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        //Handle the navigation drawer when user presses the back button
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the options menu along the action bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks
        /*Remember: Specify a parent activity in the manifest so Android
        *           handles Home/Up button clicks automagically*/
        int id = item.getItemId();

        //Setting action
        if (id == R.id.action_settings) {
            //We would launch the SettingActivity here, or something...
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Switch the current view based on the selected item
        displayView(item.getItemId());

        //Close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private FavoritesFragment favoritesFragment;
    FavoritesFragment getFavoritesFragment() {
        if (favoritesFragment == null) {
            favoritesFragment = new FavoritesFragment();
        }
        return favoritesFragment;
    }

    private StopsFragment stopsFragment;
    StopsFragment getStopsFragment() {
        if (stopsFragment == null) {
            stopsFragment = new StopsFragment();
        }
        return stopsFragment;
    }

    private AlertsFragment alertsFragment;
    AlertsFragment getAlertsFragment() {
        if (alertsFragment == null) {
            alertsFragment = new AlertsFragment();
        }
        return alertsFragment;
    }

    private SettingsFragment settingsFragment;
    SettingsFragment getSettingsFragment() {
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        return settingsFragment;
    }

    //This function will switch our current view fragment
    @SuppressWarnings("StatementWithEmptyBody")
    private void displayView(int id) {
        Fragment newFragment;
        switch (id) {
            case R.id.nav_favs:
                toolbar.setTitle("Favorites");
                newFragment = getFavoritesFragment();
                break;
            case R.id.nav_stops:
                toolbar.setTitle("Stops");
                newFragment = getStopsFragment();
                break;
            case R.id.nav_alerts:
                toolbar.setTitle("Alerts");
                newFragment = getAlertsFragment();
                break;
            case R.id.nav_settings:
                toolbar.setTitle("Settings");
                newFragment = getSettingsFragment();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.content_frame);

        if (currentFragment == newFragment) {
            return;
        }

        Bundle args = new Bundle();
        //args.put...
        newFragment.setArguments(args);

        FragmentTransaction ft = fm.beginTransaction();

        //Replace current fragment
        ft.replace(R.id.content_frame, newFragment);
        ft.commit();
    }
}

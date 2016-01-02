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

import osu.appclub.corvallisbus.API.TransitAPI;

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

        //Current FAB action -- This may be used when adding favorites?
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "FAB Action is Fab!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        //Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //API Blah blah (This is just generating some dummy data)
        TransitAPI.populateFavorites();
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

    //This function will switch our current view fragment
    @SuppressWarnings("StatementWithEmptyBody")
    private void displayView(int id) {
        Fragment fragment = null;

        //Favorites
        if (id == R.id.nav_favs) {
            //Set toolbar title
            toolbar.setTitle("Favorites");

            //Create new fragment with args
            fragment = new FavoritesFragment();
            Bundle args = new Bundle();
            //args.put...
            fragment.setArguments(args);

            //Get fragment manager and start transaction
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            //Replace current fragment
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        //Stops
        else if(id == R.id.nav_stops) {
            //Set toolbar title
            toolbar.setTitle("Stops");

            //Create new fragment with args
            fragment = new StopsFragment();
            Bundle args = new Bundle();
            //args.put...
            fragment.setArguments(args);

            //Get fragment manager and start transaction
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            //Replace current fragment
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        //Alerts
        else if(id == R.id.nav_alerts) {
            //Set toolbar title
            toolbar.setTitle("Alerts");

            //Create new fragment with args
            fragment = new AlertsFragment();
            Bundle args = new Bundle();
            //args.put...
            fragment.setArguments(args);

            //Get fragment manager and start transaction
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            //Replace current fragment
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        //Settings
        else if (id == R.id.nav_settings) {
            //Set toolbar title
            toolbar.setTitle("Settings");

            //Create new fragment with args
            fragment = new SettingsFragment();
            Bundle args = new Bundle();
            //args.put...
            fragment.setArguments(args);

            //Get fragment manager and start transaction
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            //Replace current fragment
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }
}

package osu.appclub.corvallisbus;

import android.location.Location;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStaticData;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationProvider {
    private GoogleApiClient apiClient;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Kick off static data download task so it can be accessed quickly later
        new AsyncTask<Void, Void, BusStaticData>() {
            @Override
            protected BusStaticData doInBackground(Void... params) {
                return CorvallisBusAPIClient.getStaticData();
            }
        }.execute();

        // initial UI and Toolbar setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        setSupportActionBar(toolbar);

        // Load the Favorites Fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        FavoritesFragment favoritesFragment = new FavoritesFragment();
        ft.replace(R.id.content_frame, favoritesFragment);
        ft.commit();

        // Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (apiClient != null) {
            apiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (apiClient != null) {
            apiClient.disconnect();
        }
    }

    @Override
    public boolean isLocationAvailable() {
        return apiClient != null && apiClient.isConnected();
    }

    @Override
    public Location getUserLocation() {
        try {
            return LocationServices.FusedLocationApi.getLastLocation(apiClient);
        }
        catch (SecurityException e) {
            Log.d("osu.appclub", e.getMessage());
            return null;
        }
    }

    private final List<LocationAvailableListener> locationListeners = new ArrayList<>();
    @Override
    public void addLocationAvailableListener(LocationAvailableListener listener) {
        locationListeners.add(listener);
    }

    @Override
    public void removeLocationAvailableListener(LocationAvailableListener listener) {
        locationListeners.remove(listener);
    }

    @Override
    public void onConnected(Bundle bundle) {
        for (LocationAvailableListener listener : locationListeners) {
            listener.onLocationAvailable(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("osu.appclub", "API client connection failed: " + connectionResult);
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

    // Switches the displayed fragment to the fragment given by the id parameter.
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

package osu.appclub.corvallisbus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStaticData;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationProvider, BusStopSelectionQueue, ViewPager.OnPageChangeListener {
    private GoogleApiClient apiClient;
    private Toolbar toolbar;
    private ViewPager pager;

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

        pager = (ViewPager)findViewById(R.id.viewPager);
        pager.setAdapter(new CorvallisBusPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);

        TabLayout.Tab favoritesTab = tabLayout.getTabAt(0);
        assert favoritesTab != null;
        favoritesTab.setIcon(R.drawable.favorites_24dp);

        TabLayout.Tab browseTab = tabLayout.getTabAt(1);
        assert browseTab != null;
        browseTab.setIcon(R.drawable.ic_directions_bus_white_24dp);

        TabLayout.Tab alertsTab = tabLayout.getTabAt(2);
        assert alertsTab != null;
        alertsTab.setIcon(R.drawable.ic_warning_white_24dp);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // region ViewPager.OnPageChangeListener
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch(position) {
            case 0:
                toolbar.setTitle("Favorites");
                break;
            case 1:
                toolbar.setTitle("Browse Stops");
                break;
            case 2:
                toolbar.setTitle("Service Alerts");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported position selected");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    // endregion

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

    // region LocationProvider
    @Override
    public boolean isLocationResolved() {
        // TODO: check if location services enabled and if not prompt to open settings
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasPermission = permission == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }
        return apiClient != null && apiClient.isConnected() && hasPermission;
    }

    @Nullable
    @Override
    public Location getUserLocation() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(apiClient);
        } else {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length != 0) {
            // Continue regardless of whether location permissions are granted or not
            fireOnLocationResolved();
        }
    }

    private final List<LocationAvailableListener> locationListeners = new ArrayList<>();
    @Override
    public void addLocationResolutionListener(LocationAvailableListener listener) {
        locationListeners.add(listener);
    }

    @Override
    public void removeLocationResolutionListener(LocationAvailableListener listener) {
        locationListeners.remove(listener);
    }

    void fireOnLocationResolved() {
        // Prevents ConcurrentModificationException because listeners can remove themselves
        ArrayList<LocationAvailableListener> iterableListeners = new ArrayList<>(locationListeners);
        for (LocationAvailableListener listener : iterableListeners) {
            listener.onLocationResolved(this);
        }
    }
    // endregion

    // region GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnected(Bundle bundle) {
        fireOnLocationResolved();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("osu.appclub", "API client connection failed: " + connectionResult);
    }
    // endregion

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

    public void displayStopsFragment() {
        pager.setCurrentItem(1);
    }

    // region BusStopSelectionQueue
    BusStopSelectionQueue.Listener stopDetailsListener;
    @Override
    public void setStopDetailsQueueListener(@Nullable Listener listener) {
        stopDetailsListener = listener;
    }

    private Queue<Integer> busStopQueue = new ArrayBlockingQueue<>(1);
    @Override
    public void enqueueBusStop(int stopId) {
        busStopQueue.offer(stopId);
        displayStopsFragment();
        if (stopDetailsListener != null) {
            stopDetailsListener.onEnqueueBusStop(this);
        }
    }

    @Nullable
    public Integer dequeueBusStopId() {
        return busStopQueue.poll();
    }

    @Nullable
    public Integer peekBusStopId() {
        return busStopQueue.peek();
    }
    // endregion
}

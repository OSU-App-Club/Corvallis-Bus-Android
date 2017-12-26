package osu.appclub.corvallisbus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusPreferences;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.widget.CorvallisBusWidgetProvider;

public class MainActivity
        extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            LocationProvider,
            BusStopSelectionQueue,
            ViewPager.OnPageChangeListener,
            ActivityRunningMonitor,
            SearchView.OnQueryTextListener,
            StopsSearchTask.Listener {
    public static final String VIEW_STOP_ACTION = "osu.appclub.corvallisbus.VIEW_STOP_ACTION";
    public static final String EXTRA_STOP_ID = "osu.appclub.corvallisbus.EXTRA_STOP_ID";

    private GoogleApiClient apiClient;
    private Toolbar toolbar;
    private ViewPager pager;
    private SearchView searchView;

    private static final class LoadStaticDataTask extends AsyncTask<Void, Void, BusStaticData> {
        @Override
        protected BusStaticData doInBackground(Void... params) {
            return CorvallisBusAPIClient.getStaticData();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Kick off static data download task so it can be accessed quickly later
        new LoadStaticDataTask().execute();

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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

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
    public void onPageScrollStateChanged(int state) { }
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

    private boolean isActivityRunning = false;
    @Override
    protected void onResume() {
        super.onResume();
        isActivityRunning = true;
        for (ActivityRunningMonitor.Listener listener : new ArrayList<>(activityRunningListeners)) {
            listener.onResume();
        }

        Intent intent = getIntent();
        int stopID = intent.getIntExtra(EXTRA_STOP_ID, -1);
        if (stopID != -1) {
            intent.removeExtra(EXTRA_STOP_ID);
            enqueueBusStop(stopID);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Intent updateWidgetItn = new Intent(this, CorvallisBusWidgetProvider.class);
        updateWidgetItn.setAction(CorvallisBusWidgetProvider.UPDATE_ACTION);
        sendBroadcast(updateWidgetItn);

        isActivityRunning = false;
        for (ActivityRunningMonitor.Listener listener : new ArrayList<>(activityRunningListeners)) {
            listener.onPause();
        }
    }

    // region ActivityRunningMonitor
    List<ActivityRunningMonitor.Listener> activityRunningListeners = new ArrayList<>();
    @Override
    public boolean isActivityRunning() {
        return isActivityRunning;
    }

    @Override
    public void addActivityRunningListener(ActivityRunningMonitor.Listener listener) {
        activityRunningListeners.add(listener);
    }

    @Override
    public void removeActivityRunningListener(ActivityRunningMonitor.Listener listener) {
        activityRunningListeners.remove(listener);
    }
    // endregion

    // region LocationProvider
    @Override
    public boolean isLocationResolved() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasPermission = permission == PackageManager.PERMISSION_GRANTED;
        boolean wasLocationRequested = CorvallisBusPreferences.getWasLocationRequested(this);
        if (!hasPermission) {
            if (wasLocationRequested) {
                fireOnLocationResolved();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 0);
            }
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
        if (grantResults.length != 0 && apiClient != null && apiClient.isConnected()) {
            // Continue regardless of whether location permissions are granted or not
            CorvallisBusPreferences.setWasLocationRequested(this, true);
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
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasPermission = permission == PackageManager.PERMISSION_GRANTED;
        if (hasPermission || CorvallisBusPreferences.getWasLocationRequested(this)) {
            fireOnLocationResolved();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("osu.appclub", "API client connection failed: " + connectionResult);
    }
    // endregion

    // TODO: add license info and maybe settings options items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        assert searchManager != null;

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("osu.appclub", "Focus changed to " + hasFocus);
            }
         });
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    private StopsSearchTask searchTask;
    @Override
    public boolean onQueryTextChange(String newText) {
        if (searchTask != null) {
            searchTask.cancel(true);
        }

        if (newText.isEmpty()) {
            searchView.setSuggestionsAdapter(null);
        } else {
            searchTask = new StopsSearchTask(new WeakReference<>(this), newText, getUserLocation());
            searchTask.execute();
        }
        return true;
    }

    @Override
    public void searchComplete(Cursor cursor) {
        Log.d("osu.appclub", "Found " + cursor.getCount() + " results");
        if (cursor.getCount() == 0) {
            MatrixCursor placeholder = new MatrixCursor(new String[] { "_id" });
            placeholder.addRow(new Object[] { 0 });
            searchView.setSuggestionsAdapter(new CursorAdapter(this, placeholder, false) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                    return view;
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    text.setText(R.string.search_no_results);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // no-op
                        }
                    });
                }
            });
        } else {
            searchView.setSuggestionsAdapter(new CursorAdapter(this, cursor, false) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
                    return view;
                }

                @SuppressLint("DefaultLocale")
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    final int stopID = cursor.getInt(0);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchView.clearFocus();
                            enqueueBusStop(stopID);
                        }
                    });

                    TextView textStopName = (TextView) view.findViewById(android.R.id.text1);
                    textStopName.setText(cursor.getString(1));

                    TextView textDistance = (TextView) view.findViewById(android.R.id.text2);
                    if (cursor.getType(2) == Cursor.FIELD_TYPE_NULL) {
                        textDistance.setText("");
                    } else {
                        textDistance.setText(String.format("%.1f miles", cursor.getDouble(2)));
                    }

                }
            });
        }
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rikkigibson.github.io/corvallisbus"));
            startActivity(intent);
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
    public void setStopDetailsQueueListener(@Nullable BusStopSelectionQueue.Listener listener) {
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

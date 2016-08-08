package osu.appclub.corvallisbus.browsestops;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import osu.appclub.corvallisbus.ActivityRunningMonitor;
import osu.appclub.corvallisbus.BusStopSelectionQueue;
import osu.appclub.corvallisbus.ResourceLoadState;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusPreferences;
import osu.appclub.corvallisbus.LocationProvider;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.models.RouteDetailsViewModel;

/**
 * Created by rikkigibson on 1/20/16.
 */
public class BusMapPresenter implements OnMapReadyCallback, LocationProvider.LocationAvailableListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter, BusStopSelectionQueue.Listener,
        ActivityRunningMonitor.Listener {
    private final Context context;
    private final LocationProvider locationProvider;
    private final ActivityRunningMonitor activityRunningMonitor;

    private ResourceLoadState markersLoadState = ResourceLoadState.NotStarted;
    private final Map<Marker, BusStop> markersLookup = new HashMap<>();

    private final BusStopSelectionQueue stopSelectionQueue;
    private GoogleMap googleMap;
    private final LatLng CORVALLIS_LATLNG = new LatLng(44.56802, -123.27926);
    private final Handler handler = new Handler();
    private final Runnable initMarkersRunnable = new Runnable() {
        @Override
        public void run() {
            initMarkers();
        }
    };

    BitmapDescriptor green_icon;
    BitmapDescriptor green_selected_icon;
    BitmapDescriptor gold_icon;
    BitmapDescriptor gold_selected_icon;

    public OnStopSelectedListener stopSelectedListener;

    public BusMapPresenter(Context context, LocationProvider locationProvider,
                           BusStopSelectionQueue stopSelectionQueue,
                           ActivityRunningMonitor activityRunningMonitor) {
        this.context = context;
        this.locationProvider = locationProvider;
        this.stopSelectionQueue = stopSelectionQueue;
        this.activityRunningMonitor = activityRunningMonitor;
        stopSelectionQueue.setStopDetailsQueueListener(this);
        activityRunningMonitor.addActivityRunningListener(this);
    }

    // region ActivityRunningMonitor.Listener
    @Override
    public void onResume() {
        initMarkers();
    }

    @Override
    public void onPause() {

    }
    // endregion

    /**
     * OnMapReadyCallback
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setInfoWindowAdapter(this);

        green_icon = BitmapDescriptorFactory.fromResource(R.drawable.greenoval);
        green_selected_icon = BitmapDescriptorFactory.fromResource(R.drawable.greenoval_highlighted_big);
        gold_icon = BitmapDescriptorFactory.fromResource(R.drawable.goldoval);
        gold_selected_icon = BitmapDescriptorFactory.fromResource(R.drawable.goldoval_highlighted_big);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CORVALLIS_LATLNG, 14.0f));

        try {
            googleMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e) {
            Log.d("osu.appclub", e.getMessage());
        }

        googleMap.setOnMarkerClickListener(this);

        if (locationProvider.isLocationResolved()) {
            initializeUserLocation();
        } else {
            locationProvider.addLocationResolutionListener(this);
        }

        initMarkers();
    }

    /**
     * LocationProvider.LocationAvailableListener
     */
    @Override
    public void onLocationResolved(LocationProvider provider) {
        provider.removeLocationResolutionListener(this);
        initializeUserLocation();
    }

    void initializeUserLocation() {
        Location location = locationProvider.getUserLocation();
        if (location != null) {
            float[] distanceResult = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    CORVALLIS_LATLNG.latitude, CORVALLIS_LATLNG.longitude, distanceResult);
            // Only go to the user's location if they're within 20 miles of Corvallis
            if (distanceResult[0] < 32000) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
                googleMap.moveCamera(update);
            }
        }
    }

    void initMarkers() {
        if (markersLoadState != ResourceLoadState.NotStarted) {
            return;
        }
        markersLoadState = ResourceLoadState.Started;

        new AsyncTask<Void, Void, BusStaticData>() {
            @Override
            protected BusStaticData doInBackground(Void... params) {
                return CorvallisBusAPIClient.getStaticData();
            }

            @Override
            protected void onPostExecute(@Nullable BusStaticData busStaticData) {
                if (markersLoadState == ResourceLoadState.Completed) {
                    // prevent populating map multiple times
                    return;
                }
                if (busStaticData == null) {
                    markersLoadState = ResourceLoadState.NotStarted;
                    // Try again if load failed and app is running
                    if (activityRunningMonitor.isActivityRunning()) {
                        Toast.makeText(context, "Failed to load bus stops", Toast.LENGTH_SHORT)
                                .show();
                        handler.postDelayed(initMarkersRunnable, 5000);
                    }
                    return;
                }
                markersLoadState = ResourceLoadState.Completed;
                activityRunningMonitor.removeActivityRunningListener(BusMapPresenter.this);

                createMarkers(busStaticData.stops);
                selectQueuedStopIfReady();
            }
        }.execute();
    }

    void createMarkers(SparseArray<BusStop> busStops) {
        List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);

        MarkerOptions options = new MarkerOptions();
        for (int i = 0; i < busStops.size(); i++) {
            BusStop busStop = busStops.valueAt(i);

            boolean isFavorite = favoriteStopIds.contains(busStop.id);
            options.position(busStop.location);
            options.icon(isFavorite ? gold_icon : green_icon);
            Marker marker = googleMap.addMarker(options);
            markersLookup.put(marker, busStop);
        }
    }

    Marker currentMarker;

    /**
     * GoogleMap.OnMarkerClickListener
     */
    @Override
    public boolean onMarkerClick(Marker newMarker) {
        selectMarker(newMarker);
        return true;
    }

    void selectMarker(Marker newMarker) {

        List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);

        if (currentMarker != null) {
            boolean currentStopIsFavorite = favoriteStopIds.contains(markersLookup.get(currentMarker).id);
            currentMarker.setIcon(currentStopIsFavorite ? gold_icon : green_icon);
        }
        currentMarker = newMarker;

        BusStop busStop = markersLookup.get(newMarker);
        boolean isFavorite = favoriteStopIds.contains(busStop.id);

        newMarker.setIcon(isFavorite ? gold_selected_icon : green_selected_icon);
        newMarker.showInfoWindow();

        if (stopSelectedListener != null) {
            stopSelectedListener.onStopSelected(busStop);
        }
    }

    @Nullable
    Marker getMarkerByStopId(int stopId) {
        for (Map.Entry<Marker, BusStop> entry : markersLookup.entrySet()) {
            if (entry.getValue().id == stopId) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setFavoritedStateForStop(boolean isFavorite, int stopId) {
        Marker marker = getMarkerByStopId(stopId);
        if (marker != null) {
            boolean isSelected = currentMarker.equals(marker);

            final BitmapDescriptor newIcon;
            if (isSelected) {
                newIcon = isFavorite ? gold_selected_icon : green_selected_icon;
            } else {
                newIcon = isFavorite ? gold_icon : green_icon;
            }

            marker.setIcon(newIcon);
            marker.showInfoWindow();
        }
    }

    public void onEnqueueBusStop(BusStopSelectionQueue queue) {
        selectQueuedStopIfReady();
    }

    /**
     * Selects the bus stop from the queue if the view is ready and there is a stop in the queue.
     */
    void selectQueuedStopIfReady() {
        Integer stopId = stopSelectionQueue.peekBusStopId();
        if (stopId == null) {
            return;
        }

        Marker marker = getMarkerByStopId(stopId);
        if (marker == null) {
            return;
        }

        // Mark the stop id selection as consumed
        stopSelectionQueue.dequeueBusStopId();
        selectMarker(marker);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    /**
     * Call when this presenter instance will no longer be used.
     */
    public void dispose() {
        stopSelectionQueue.setStopDetailsQueueListener(null);
    }

    @Nullable Polyline selectedRoutePolyline;
    public void displayRoute(@NonNull RouteDetailsViewModel route) {
        if (selectedRoutePolyline != null) {
            selectedRoutePolyline.remove();
        }
        selectedRoutePolyline = googleMap.addPolyline(route.polyline);
    }

    public void clearDisplayedRoute() {
        if (selectedRoutePolyline != null) {
            selectedRoutePolyline.remove();
        }
        selectedRoutePolyline = null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // Showing an info window causes the selected stop marker to jump to the front.
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.empty_info_window, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public interface OnStopSelectedListener {
        void onStopSelected(BusStop stop);
    }

}

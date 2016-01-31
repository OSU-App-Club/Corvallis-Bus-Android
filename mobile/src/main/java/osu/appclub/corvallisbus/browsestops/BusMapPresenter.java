package osu.appclub.corvallisbus.browsestops;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import osu.appclub.corvallisbus.CorvallisBusPreferences;
import osu.appclub.corvallisbus.LocationProvider;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.BusStop;

/**
 * Created by rikkigibson on 1/20/16.
 */
public class BusMapPresenter implements OnMapReadyCallback, LocationProvider.LocationAvailableListener, GoogleMap.OnMarkerClickListener {
    private final Context context;
    private final LocationProvider locationProvider;
    private GoogleMap googleMap;
    private final Map<Marker, BusStop> markersLookup = new HashMap<>();

    BitmapDescriptor green_icon;
    BitmapDescriptor green_selected_icon;
    BitmapDescriptor gold_icon;
    BitmapDescriptor gold_selected_icon;

    public OnStopSelectedListener stopSelectedListener;

    public BusMapPresenter(Context context, LocationProvider locationProvider) {
        this.context = context;
        this.locationProvider = locationProvider;
    }

    /**
     * OnMapReadyCallback
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        green_icon = BitmapDescriptorFactory.fromResource(R.drawable.greenoval);
        green_selected_icon = BitmapDescriptorFactory.fromResource(R.drawable.greenoval_highlighted_big);
        gold_icon = BitmapDescriptorFactory.fromResource(R.drawable.goldoval);
        gold_selected_icon = BitmapDescriptorFactory.fromResource(R.drawable.goldoval_highlighted_big);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.56802, -123.27926), 14.0f));

        try {
            googleMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e) {
            Log.d("osu.appclub", e.getMessage());
        }

        googleMap.setOnMarkerClickListener(this);

        if (locationProvider.isLocationAvailable()) {
            updateUserLocation();
        } else {
            locationProvider.addLocationAvailableListener(this);
        }

        initMarkers();
    }

    /**
     * LocationProvider.LocationAvailableListener
     */
    @Override
    public void onLocationAvailable(LocationProvider provider) {
        provider.removeLocationAvailableListener(this);
        updateUserLocation();
    }

    void updateUserLocation() {
        Location location = locationProvider.getUserLocation();
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
            googleMap.moveCamera(update);
        }
    }

    void initMarkers() {
        new AsyncTask<Void, Void, BusStaticData>() {
            @Override
            protected BusStaticData doInBackground(Void... params) {
                return CorvallisBusAPIClient.getStaticData();
            }

            @Override
            protected void onPostExecute(@Nullable BusStaticData busStaticData) {
                if (busStaticData == null) {
                    Toast.makeText(context, "Failed to load bus stops", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                createMarkers(busStaticData.stops);
                if (stopIdToPresent != null) {
                    presentBusStop(stopIdToPresent);
                    stopIdToPresent = null;
                }
            }
        }.execute();
    }

    void createMarkers(SparseArray<BusStop> busStops) {
        List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);

        MarkerOptions options = new MarkerOptions();
        for (int i = 0; i < busStops.size(); i++) {
            BusStop busStop = busStops.valueAt(i);

            options.position(busStop.location);
            options.icon(favoriteStopIds.contains(busStop.id)
                ? gold_icon
                : green_icon);
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

        if (stopSelectedListener != null) {
            stopSelectedListener.onStopSelected(busStop.id);
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
        }
    }

    @Nullable
    BusStop getStopById(int stopId) {
        for (BusStop stop : markersLookup.values()) {
            if (stop.id == stopId) {
                return stop;
            }
        }
        return null;
    }

    @Nullable
    Integer stopIdToPresent = null;
    public void presentBusStop(int stopId) {
        Marker marker = getMarkerByStopId(stopId);
        if (marker == null) {
            stopIdToPresent = stopId;
            return;
        }
        selectMarker(marker);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    public interface OnStopSelectedListener {
        void onStopSelected(int stopId);
    }
}
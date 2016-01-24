package osu.appclub.corvallisbus.browsestops;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
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
import java.util.Map;

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

    public OnStopSelectedListener stopSelectedListener;

    public BusMapPresenter(Context context, LocationProvider locationProvider) {
        this.context = context;
        this.locationProvider = locationProvider;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        green_icon = BitmapDescriptorFactory.fromResource(R.drawable.greenoval);
        green_selected_icon = BitmapDescriptorFactory.fromResource(R.drawable.greenoval_highlighted_big);

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

                MarkerOptions options = new MarkerOptions();
                for (int i = 0; i < busStaticData.stops.size(); i++) {
                    BusStop busStop = busStaticData.stops.valueAt(i);

                    options.position(busStop.location);
                    options.icon(green_icon);
                    // TODO: options.icon...
                    Marker marker = googleMap.addMarker(options);
                    markersLookup.put(marker, busStop);
                }
            }
        }.execute();
    }

    Marker previousMarker;
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (previousMarker != null) {
            previousMarker.setIcon(green_icon);
        }
        marker.setIcon(green_selected_icon);
        previousMarker = marker;

        BusStop busStop = markersLookup.get(marker);
        if (stopSelectedListener != null) {
            stopSelectedListener.onStopSelected(busStop.id);
        }

        return true;
    }

    public interface OnStopSelectedListener {
        void onStopSelected(int stopId);
    }
}

package osu.appclub.corvallisbus;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.BusStop;

/**
 * Created by rikkigibson on 1/20/16.
 */
public class BusMapPresenter implements OnMapReadyCallback, LocationProvider.LocationAvailableListener, GoogleMap.OnMarkerClickListener {
    private final LocationProvider locationProvider;
    private GoogleMap googleMap;
    private final Map<Marker, BusStop> markersLookup = new HashMap<>();

    public OnStopSelectedListener stopSelectedListener;

    public BusMapPresenter(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

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
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
        googleMap.moveCamera(update);
    }

    void initMarkers() {
        new AsyncTask<Void, Void, BusStaticData>() {
            @Override
            protected BusStaticData doInBackground(Void... params) {
                return CorvallisBusAPIClient.getStaticData();
            }

            @Override
            protected void onPostExecute(BusStaticData busStaticData) {

                MarkerOptions options = new MarkerOptions();
                //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable)
                for (int i = 0; i < busStaticData.stops.size(); i++) {
                    BusStop busStop = busStaticData.stops.valueAt(i);

                    options.position(busStop.location);
                    // TODO: options.icon...
                    Marker marker = googleMap.addMarker(options);
                    markersLookup.put(marker, busStop);
                }
            }
        }.execute();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
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

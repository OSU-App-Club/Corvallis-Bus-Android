package osu.appclub.corvallisbus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
public class BusMapPresenter implements GoogleMap.OnMarkerClickListener {
    private final GoogleMap googleMap;
    private final Map<Marker, BusStop> markersLookup = new HashMap<>();

    public OnStopSelectedListener stopSelectedListener;

    public BusMapPresenter(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.56802, -123.27926), 14.0f));

        try {
            googleMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e) {
            Log.d("osu.appclub", e.getMessage());
        }

        googleMap.setOnMarkerClickListener(this);
        initMarkers();
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

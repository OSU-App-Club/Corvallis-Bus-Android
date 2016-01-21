package osu.appclub.corvallisbus;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusRoute;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.models.RouteArrivalsSummary;
import osu.appclub.corvallisbus.models.RouteDetailsViewModel;
import osu.appclub.corvallisbus.models.StopDetailsViewModel;

public class StopsFragment extends ListFragment implements BusMapPresenter.OnStopSelectedListener {
    MapView mapView;
    TextView textStopName;
    BusMapPresenter mapPresenter;

    StopDetailsListAdapter listAdapter;
    ArrayList<RouteDetailsViewModel> listItems = new ArrayList<>();

    //Required empty public constructor
    public StopsFragment() {

    }

    //Create a new instance of this fragment using the provided parameters.
    public static StopsFragment newInstance() {
        StopsFragment fragment = new StopsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stops, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textStopName = (TextView) getActivity().findViewById(R.id.stopName);
        mapView = (MapView) getActivity().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapPresenter = new BusMapPresenter(googleMap);
                mapPresenter.stopSelectedListener = StopsFragment.this;
            }
        });

        listAdapter = new StopDetailsListAdapter(getActivity(), listItems);
        setListAdapter(listAdapter);
    }

    public void onStopSelected(int stopId) {
        startLoadingArrivals(stopId);
    }

    public void startLoadingArrivals(final int stopId) {
        new AsyncTask<Void, Void, StopDetailsViewModel>() {

            @Override
            protected StopDetailsViewModel doInBackground(Void... params) {
                return CorvallisBusAPIClient.getStopDetailsViewModel(stopId);
            }

            @Override
            protected void onPostExecute(StopDetailsViewModel stopDetailsViewModel) {
                textStopName.setText(stopDetailsViewModel == null
                        ? ""
                        : stopDetailsViewModel.stopName);

                listItems.clear();
                if (stopDetailsViewModel != null) {
                    listItems.addAll(stopDetailsViewModel.routeDetailsList);
                }
                listAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}

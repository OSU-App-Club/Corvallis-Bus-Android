package osu.appclub.corvallisbus;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
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

import com.google.android.gms.maps.MapView;

import java.util.ArrayList;

import osu.appclub.corvallisbus.models.RouteDetailsViewModel;

public class StopsFragment extends ListFragment {
    MapView mapView;

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
        mapView = (MapView) getActivity().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

//        ImageButton buttonFavorite = (ImageButton) getActivity().findViewById(R.id.buttonFavorite);
//        buttonFavorite.setOnClickListener(new View.OnClickListener() {
//            boolean isFavorite = false;
//            @Override
//            public void onClick(View v) {
//                isFavorite = !isFavorite;
//                int colorResourceId = isFavorite ? R.color.colorFavorite : android.R.color.holo_purple;
//                int colorValue;
//                if (android.os.Build.VERSION.SDK_INT >= 23) {
//                    Resources.Theme theme = getActivity().getTheme();
//                    colorValue = getResources().getColor(colorResourceId, theme);
//                } else {
//                    colorValue = getResources().getColor(colorResourceId);
//                }
//                v.setBackgroundColor(colorValue);
//            }
//        });

        ArrayList<RouteDetailsViewModel> TEST_DATA = new ArrayList<>();

        RouteDetailsViewModel routeDetails = new RouteDetailsViewModel();
        routeDetails.routeColor = 0xFF00ADEE;
        routeDetails.routeName = "1";
        routeDetails.arrivalsSummary = "11 minutes, 01:53 PM";
        routeDetails.scheduleSummary = "Hourly until 07:03 PM";
        TEST_DATA.add(routeDetails);

        routeDetails = new RouteDetailsViewModel();
        routeDetails.routeColor = 0xFFBD559F;
        routeDetails.routeName = "5";
        routeDetails.arrivalsSummary = "25 minutes, 01:38 PM";
        routeDetails.scheduleSummary = "Last arrival at 08:08 PM";
        TEST_DATA.add(routeDetails);

        StopDetailsListAdapter listAdapter = new StopDetailsListAdapter(getActivity(), TEST_DATA);
        setListAdapter(listAdapter);
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

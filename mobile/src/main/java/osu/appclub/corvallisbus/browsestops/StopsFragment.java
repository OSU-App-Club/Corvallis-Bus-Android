package osu.appclub.corvallisbus.browsestops;

import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;

import java.util.ArrayList;

import osu.appclub.corvallisbus.LocationProvider;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.RouteDetailsViewModel;
import osu.appclub.corvallisbus.models.StopDetailsViewModel;

public class StopsFragment extends ListFragment implements BusMapPresenter.OnStopSelectedListener, FloatingActionButton.OnClickListener {
    MapView mapView;
    BusMapPresenter mapPresenter;
    TextView textStopName;
    FloatingActionButton floatingActionButton;

    StopDetailsViewModel viewModel = new StopDetailsViewModel();

    StopDetailsListAdapter listAdapter;
    final ArrayList<RouteDetailsViewModel> listItems = new ArrayList<>();

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

        floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
        textStopName = (TextView) getActivity().findViewById(R.id.stopName);

        if (getActivity() instanceof LocationProvider) {
            mapPresenter = new BusMapPresenter((LocationProvider) getActivity());
        } else {
            throw new UnsupportedOperationException("Stops fragment must be attached to an activity implementing LocationProvider");
        }
        mapPresenter.stopSelectedListener = this;

        mapView = (MapView) getActivity().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapPresenter);

        listAdapter = new StopDetailsListAdapter(getActivity(), listItems);
        setListAdapter(listAdapter);
    }

    /**
     * FloatingActionButton.OnClickListener
     */
    @Override
    public void onClick(View v) {
        viewModel.isFavorite = !viewModel.isFavorite;

        ColorStateList colorStateList = ColorStateList.valueOf(viewModel.isFavorite
                ? ContextCompat.getColor(getActivity(), R.color.colorFavorite)
                : ContextCompat.getColor(getActivity(), R.color.colorAccent));

        floatingActionButton.setBackgroundTintList(colorStateList);
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
                StopsFragment.this.viewModel = stopDetailsViewModel;

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

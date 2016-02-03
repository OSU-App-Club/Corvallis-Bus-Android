package osu.appclub.corvallisbus.browsestops;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.BusStopSelectionQueue;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusPreferences;
import osu.appclub.corvallisbus.LocationProvider;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.models.RouteDetailsViewModel;

public class StopsFragment extends ListFragment implements BusMapPresenter.OnStopSelectedListener, FloatingActionButton.OnClickListener {
    MapView mapView;
    BusMapPresenter mapPresenter;
    TextView textStopName;
    FloatingActionButton floatingActionButton;

    Context context;

    Handler handler = new Handler();

    @Nullable
    Integer selectedStopId;

    StopDetailsListAdapter listAdapter;
    final ArrayList<RouteDetailsViewModel> routeDetailsList = new ArrayList<>();

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
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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

        // A stop must be selected before enabling the favorite button
        floatingActionButton.setEnabled(false);

        textStopName = (TextView) getActivity().findViewById(R.id.stopName);

        if (getActivity() instanceof LocationProvider && getActivity() instanceof BusStopSelectionQueue) {
            mapPresenter = new BusMapPresenter(getActivity(), (LocationProvider)getActivity(), (BusStopSelectionQueue)getActivity());
        } else {
            throw new UnsupportedOperationException("Stops fragment must be attached to an activity implementing LocationProvider and BusStopSelectionQueue.");
        }
        mapPresenter.stopSelectedListener = this;

        mapView = (MapView) getActivity().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapPresenter);

        listAdapter = new StopDetailsListAdapter(getActivity(), routeDetailsList);
        setListAdapter(listAdapter);

        reloadArrivalTimesAtInterval();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // This prevents accidentally calling enqueueBusStop on a mapPresenter
        // from the...previous "iteration" of this fragment.
        // Google "android fragment lifecycle". Bring tissues for the probably ensuing tears.
        mapPresenter.dispose();
        mapPresenter = null;

        selectedStopId = null;

        handler.removeCallbacks(reloadArrivalTimesRunnable);

        // Prevent list items from showing up without any other views having content
        routeDetailsList.clear();
    }

    /**
     * FloatingActionButton.OnClickListener
     */
    @Override
    public void onClick(View v) {
        // This action doesn't have meaning if there is no selected stop
        if (selectedStopId == null) {
            return;
        }

        List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);

        // Assume the stop was just favorited if the favorites list doesn't contain that stop
        boolean stopWasFavorited = !favoriteStopIds.contains(selectedStopId);
        if (stopWasFavorited) {
            favoriteStopIds.add(selectedStopId);
        } else {
            favoriteStopIds.remove(selectedStopId);
        }
        CorvallisBusPreferences.setFavoriteStopIds(context, favoriteStopIds);

        updateFavoriteButtonState(stopWasFavorited);
        mapPresenter.setFavoritedStateForStop(stopWasFavorited, selectedStopId);
    }

    public void updateFavoriteButtonState(boolean isFavorite) {
        floatingActionButton.setEnabled(true);

        ColorStateList colorStateList = ColorStateList.valueOf(isFavorite
                ? ContextCompat.getColor(context, R.color.colorFavorite)
                : ContextCompat.getColor(context, R.color.colorAccent));

        floatingActionButton.setBackgroundTintList(colorStateList);
    }

    @Override
    /**
     * BusMapPresenter.OnStopSelectedListener
     */
    public void onStopSelected(BusStop stop) {
        List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);

        textStopName.setText(stop.name);
        updateFavoriteButtonState(favoriteStopIds.contains(stop.id));
        selectedStopId = stop.id;

        startLoadingArrivals();
    }

    private final Runnable reloadArrivalTimesRunnable = new Runnable() {
        @Override
        public void run() {
            startLoadingArrivals();
            reloadArrivalTimesAtInterval();
        }
    };
    /**
     * Calls the startLoadingArrivals at a regular interval.
     */
    private void reloadArrivalTimesAtInterval() {
        handler.postDelayed(reloadArrivalTimesRunnable, 30000);
    }

    public void startLoadingArrivals() {
        if (selectedStopId == null) {
            return;
        }

        new AsyncTask<Void, Void, List<RouteDetailsViewModel>>() {

            @Override
            protected List<RouteDetailsViewModel> doInBackground(Void... params) {
                return CorvallisBusAPIClient.getRouteDetailsViewModels(selectedStopId);
            }

            @Override
            protected void onPostExecute(final List<RouteDetailsViewModel> viewModels) {
                routeDetailsList.clear();
                if (viewModels != null) {
                    routeDetailsList.addAll(viewModels);
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

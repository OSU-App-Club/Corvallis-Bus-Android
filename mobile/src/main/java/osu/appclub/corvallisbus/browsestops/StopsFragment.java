package osu.appclub.corvallisbus.browsestops;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.ActivityRunningMonitor;
import osu.appclub.corvallisbus.BusStopSelectionQueue;
import osu.appclub.corvallisbus.MainActivity;
import osu.appclub.corvallisbus.Refresher;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusPreferences;
import osu.appclub.corvallisbus.LocationProvider;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.models.RouteDetailsViewModel;

public class StopsFragment extends ListFragment implements BusMapPresenter.OnStopSelectedListener,
        FloatingActionButton.OnClickListener {
    MapView mapView;
    BusMapPresenter mapPresenter;
    TextView textStopName;
    FloatingActionButton floatingActionButton;

    Context context;

    final Handler handler = new Handler();
    final Refresher arrivalsRefresher = new Refresher(30000) {
        @Override
        public void repeatedAction() {
            startLoadingArrivals();
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            arrivalsRefresher.restart();

            List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);
            mapPresenter.updateFavoriteStopsState(favoriteStopIds);
            updateFavoriteButtonState(favoriteStopIds.contains(selectedStopId));
        }
        else {
            arrivalsRefresher.stop();
        }
    }

    @Nullable Integer selectedStopId;

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

        textStopName = (TextView) getActivity().findViewById(R.id.stopName);

        Activity activity = getActivity();
        if (activity instanceof LocationProvider && activity instanceof BusStopSelectionQueue &&
            activity instanceof ActivityRunningMonitor) {
                mapPresenter = new BusMapPresenter(activity, (LocationProvider)activity,
                        (BusStopSelectionQueue)activity, (ActivityRunningMonitor)activity);
        } else {
            throw new UnsupportedOperationException(
                    "Stops fragment must be attached to an activity implementing " +
                    "LocationProvider, BusStopSelectionQueue and ActivityRunningMonitor.");
        }
        mapPresenter.stopSelectedListener = this;

        mapView = (MapView) getActivity().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapPresenter);

        listAdapter = new StopDetailsListAdapter(getActivity(), routeDetailsList);
        setListAdapter(listAdapter);
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
        floatingActionButton.show();

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

        mapPresenter.clearDisplayedRoute();
        startLoadingArrivals();
    }

    boolean arrivalsDidFinishLoading;
    final Runnable clearListRunnable = new Runnable() {
        @Override
        public void run() {
            if (!arrivalsDidFinishLoading) {
                routeDetailsList.clear();
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    public void startLoadingArrivals() {
        if (selectedStopId == null) {
            return;
        }

        arrivalsDidFinishLoading = false;
        handler.postDelayed(clearListRunnable, 1000);

        new AsyncTask<Void, Void, List<RouteDetailsViewModel>>() {
            @Override
            protected List<RouteDetailsViewModel> doInBackground(Void... params) {
                Log.d("osu.appclub", "Loading route details from background thread.");
                return CorvallisBusAPIClient.getRouteDetailsViewModels(selectedStopId);
            }

            @Override
            protected void onPostExecute(final List<RouteDetailsViewModel> viewModels) {
                onLoadArrivals(viewModels);
            }
        }.execute();
    }

    void onLoadArrivals(@Nullable final List<RouteDetailsViewModel> viewModels) {
        arrivalsDidFinishLoading = true;
        routeDetailsList.clear();

        if (viewModels == null || viewModels.size() == 0) {
            listAdapter.notifyDataSetChanged();
            return;
        }
        routeDetailsList.addAll(viewModels);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        RouteDetailsViewModel selectedRoute = routeDetailsList.get(position);
        mapPresenter.displayRoute(selectedRoute);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        if (getUserVisibleHint()) {
            arrivalsRefresher.restart();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }

        arrivalsRefresher.stop();
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

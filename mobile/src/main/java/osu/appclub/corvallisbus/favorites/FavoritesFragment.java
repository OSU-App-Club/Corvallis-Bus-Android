package osu.appclub.corvallisbus.favorites;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import osu.appclub.corvallisbus.CorvallisBusPreferences;
import osu.appclub.corvallisbus.LocationProvider;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;


public class FavoritesFragment extends ListFragment implements LocationProvider.LocationAvailableListener {
    LocationProvider locationProvider;
    ArrayList<FavoriteStopViewModel> listItems = new ArrayList<>();
    FavoritesListAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    //Required empty public constructor
    public FavoritesFragment() {

    }

    //Create a new instance of this fragment using the provided parameters.
    public static FavoritesFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof LocationProvider) {
            locationProvider = (LocationProvider) getActivity();
        } else {
            throw new UnsupportedOperationException("Favorites fragment must be attached to an activity which implements LocationProvider");
        }

        adapter = new FavoritesListAdapter(getActivity(), listItems);
        getListView().setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoadingFavorites();
            }
        });

        startLoadingFavorites();
    }

    /**
     * Begins the process of obtaining the user's location, then
     * sending it off to the network to get the user's favorite stops.
     */
    public void startLoadingFavorites() {
        if (locationProvider.isLocationAvailable()) {
            Location location = locationProvider.getUserLocation();
            startFavoritesDownloadTask(location);
        } else {
            locationProvider.addLocationAvailableListener(this);
        }
    }

    @Override
    public void onLocationAvailable(LocationProvider provider) {
        provider.removeLocationAvailableListener(this);
        Location location = provider.getUserLocation();
        startFavoritesDownloadTask(location);
    }

    public void startFavoritesDownloadTask(final Location location) {
        final List<Integer> stopIds = CorvallisBusPreferences.getFavoriteStopIds(getActivity());

        AsyncTask<Void, Void, List<FavoriteStopViewModel>> task = new AsyncTask<Void, Void, List<FavoriteStopViewModel>>() {
            @Override
            protected List<FavoriteStopViewModel> doInBackground(Void... params) {
                return CorvallisBusAPIClient.getFavoriteStops(stopIds, location);
            }

            @Override
            protected void onPostExecute(List<FavoriteStopViewModel> favoriteStopViewModels) {
                super.onPostExecute(favoriteStopViewModels);

                listItems.clear();

                if (favoriteStopViewModels == null) {
                    Toast.makeText(getActivity(), "Failed to load favorites list", Toast.LENGTH_SHORT).show();
                }
                else {
                    listItems.addAll(favoriteStopViewModels);
                }
                adapter.notifyDataSetInvalidated();

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        };
        task.execute();
    }
}

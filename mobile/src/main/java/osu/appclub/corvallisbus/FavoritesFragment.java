package osu.appclub.corvallisbus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.apiclient.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;


public class FavoritesFragment extends ListFragment {
    GoogleApiClient apiClient;
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
        if (apiClient == null) {
            GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    if (apiClient == null) {
                        // failure
                        return;
                    }

                    int permissionValue = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permissionValue == PackageManager.PERMISSION_GRANTED) {
                        Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
                        startFavoritesDownloadTask(location);
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            };

            GoogleApiClient.OnConnectionFailedListener failedListener = new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NotNull ConnectionResult connectionResult) {
                    // not sure what to do here
                }
            };

            apiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(failedListener)
                    .addApi(LocationServices.API)
                    .build();
        } else if (apiClient.isConnected()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            startFavoritesDownloadTask(location);
        }

    }

    public void startFavoritesDownloadTask(final Location location) {
        AsyncTask<Void, Void, List<FavoriteStopViewModel>> task = new AsyncTask<Void, Void, List<FavoriteStopViewModel>>() {
            @Override
            protected List<FavoriteStopViewModel> doInBackground(Void... params) {
                ArrayList<Integer> testStops = new ArrayList<>();
                testStops.add(11776);
                testStops.add(10308);
                testStops.add(10003);

                return CorvallisBusAPIClient.getFavoriteStops(testStops, location);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        if (apiClient != null) { apiClient.connect(); }
        super.onStart();
    }

    @Override
    public void onStop() {
        if (apiClient != null) { apiClient.disconnect(); }
        super.onStop();
    }
}

package osu.appclub.corvallisbus.favorites;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.BusStopSelectionQueue;
import osu.appclub.corvallisbus.MainActivity;
import osu.appclub.corvallisbus.Refresher;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusPreferences;
import osu.appclub.corvallisbus.LocationProvider;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;


public class FavoritesFragment extends ListFragment implements LocationProvider.LocationAvailableListener {
    LocationProvider locationProvider;
    BusStopSelectionQueue stopSelectionQueue;
    final ArrayList<FavoriteStopViewModel> listItems = new ArrayList<>();
    FavoritesListAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    final Refresher favoritesRefresher = new Refresher(30000) {
        @Override
        protected void repeatedAction() {
            startLoadingFavorites();
        }
    };

    //Required empty public constructor
    public FavoritesFragment() {

    }

    /**
     * Called when the user visibility of the fragment changes.
     * @param isVisibleToUser A value indicating whether this fragment is visible to the user.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            favoritesRefresher.restart();
        }
        else {
            favoritesRefresher.stop();
        }
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
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            favoritesRefresher.restart();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        favoritesRefresher.stop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity)getActivity();
        locationProvider = activity;
        stopSelectionQueue = activity;

        adapter = new FavoritesListAdapter(activity, listItems);

        View placeholder = activity.findViewById(R.id.favorites_placeholder);
        placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.displayStopsFragment();
            }
        });

        ListView listView = getListView();
        listView.setAdapter(adapter);
        listView.setEmptyView(placeholder);
        registerForContextMenu(listView);

        swipeRefreshLayout = (SwipeRefreshLayout) activity.findViewById(R.id.favorites_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoadingFavorites();
            }
        });

        // This crazy setup is the "best" way to make the layout start refreshing as soon as it is able.
        swipeRefreshLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                swipeRefreshLayout
                                        .getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                                swipeRefreshLayout.setRefreshing(true);
                            }
                        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FavoriteStopViewModel selectedStop = listItems.get(position);
        stopSelectionQueue.enqueueBusStop(selectedStop.stopID);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() != android.R.id.list) { return; }

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        MenuItem viewDetailItem = menu.add("View Details");
        viewDetailItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                stopSelectionQueue.enqueueBusStop(listItems.get(info.position).stopID);
                return true;
            }
        });

        if (listItems.get(info.position).isNearestStop) {
            menu.setHeaderTitle(listItems.get(info.position).stopName + " (nearest stop)");
            MenuItem addItem = menu.add("Add Favorite");
            addItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    addFavorite(info.position);
                    return true;
                }
            });
        } else {
            menu.setHeaderTitle(listItems.get(info.position).stopName);
            MenuItem removeItem = menu.add("Remove");
            removeItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    removeFavorite(info.position);
                    return true;
                }
            });
        }
    }

    void removeFavorite(int position) {
        FavoriteStopViewModel stopToRemove = listItems.get(position);
        if (stopToRemove.isNearestStop) {
            return;
        }

        Context context = getContext();
        List<Integer> stopIds = CorvallisBusPreferences.getFavoriteStopIds(context);
        stopIds.remove((Integer)stopToRemove.stopID);
        CorvallisBusPreferences.setFavoriteStopIds(context, stopIds);
        startLoadingFavorites();
    }

    void addFavorite(int position) {
        FavoriteStopViewModel stopToAdd = listItems.get(position);
        if (!stopToAdd.isNearestStop) {
            return;
        }

        Context context = getContext();
        List<Integer> stopIds = CorvallisBusPreferences.getFavoriteStopIds(context);
        stopIds.add(stopToAdd.stopID);
        CorvallisBusPreferences.setFavoriteStopIds(context, stopIds);
        startLoadingFavorites();
    }

    /**
     * Begins the process of obtaining the user's location, then
     * sending it off to the network to get the user's favorite stops.
     */
    public void startLoadingFavorites() {
        if (locationProvider == null) {
            return;
        }

        if (locationProvider.isLocationResolved()) {
            Location location = locationProvider.getUserLocation();
            startFavoritesDownloadTask(location);
        } else {
            locationProvider.addLocationResolutionListener(this);
        }
    }

    @Override
    public void onLocationResolved(LocationProvider provider) {
        provider.removeLocationResolutionListener(this);
        Location location = provider.getUserLocation();
        startFavoritesDownloadTask(location);
    }

    public void startFavoritesDownloadTask(final Location location) {
        final List<Integer> stopIds = CorvallisBusPreferences.getFavoriteStopIds(getActivity());

        new AsyncTask<Void, Void, List<FavoriteStopViewModel>>() {
            @Override
            protected List<FavoriteStopViewModel> doInBackground(Void... params) {
                Log.d("osu.appclub", "Loading favorite stops from background thread");
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
                adapter.notifyDataSetChanged();

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }.execute();
    }
}

package osu.appclub.corvallisbus.apiclient;

import android.location.Location;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import osu.appclub.corvallisbus.models.BusRoute;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.models.RouteArrivalsSummary;
import osu.appclub.corvallisbus.models.StopDetailsViewModel;

/*

Stops - Trasnit bus stop
    * Stop ID
    * Stop Name
    * Lat, Lon position
    * Routes associated with this stop

Routes - Collection of Stops
    * Route Identifier (Number or String)
    * Path (Collection of Stop IDs)
    * Color (Hex representation)
    * URL (Route info -- Links back to Corvallis Transit website)
    * Polyline (Encoded Polyline String for Google Maps API)
 */

//API Production server - https://corvallisb.us/api/<ENDPOINT>
//API GitHub - https://github.com/RikkiGibson/Corvallis-Bus-Server

@WorkerThread
public final class CorvallisBusAPIClient {
    static final String BASE_URL = "https://corvallisb.us/api";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(BusRoute.class, new BusRoute.Deserializer())
            .registerTypeAdapter(BusStop.class, new BusStop.Deserializer())
            .registerTypeAdapter(BusStaticData.class, new BusStaticData.Deserializer())
            .create();

    private CorvallisBusAPIClient() {

    }

    private static final Type favoriteStopsListType = new TypeToken<List<FavoriteStopViewModel>>(){}.getType();
    @Nullable
    public static List<FavoriteStopViewModel> getFavoriteStops(@NotNull List<Integer> stopIds, Location location) {
        String stopIdsString = TextUtils.join(",", stopIds);
        String locationString = location == null
                ? ""
                : location.getLatitude() + "," + location.getLongitude();
        String urlString = BASE_URL + "/favorites?stops=" +
                stopIdsString + "&location=" + locationString;
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());


            List<FavoriteStopViewModel> favorites = gson.fromJson(streamReader, favoriteStopsListType);
            return favorites;
        }
        catch (Exception e) {
            Log.d("osu.appclub", e.getMessage());
            return null;
        }
    }

    private static BusStaticData staticDataCache;

    /**
     * Prevents static data from being downloaded multiple times.
     */
    private static final Lock staticDataLock = new ReentrantLock(true);
    @Nullable
    public static BusStaticData getStaticData() {
        staticDataLock.lock();
        if (staticDataCache == null) {
            try {
                URL url = new URL(BASE_URL + "/static");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());

                staticDataCache = gson.fromJson(streamReader, BusStaticData.class);
            } catch (Exception e) {
                Log.d("osu.appclub", e.getMessage());
            }
        }
        staticDataLock.unlock();

        return staticDataCache;

    }

    private static final Type arrivalsSummaryMapType = new TypeToken<Map<Integer, List<RouteArrivalsSummary>>>(){}.getType();
    @Nullable
    public static List<RouteArrivalsSummary> getRouteArrivalsSummary(int stopId) {
        try {
            URL url = new URL(BASE_URL + "/arrivals-summary/" + stopId);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());

            Map<Integer, List<RouteArrivalsSummary>> arrivalsSummaries = gson.fromJson(streamReader, arrivalsSummaryMapType);
            return arrivalsSummaries.get(stopId);
        }
        catch (Exception e) {
            Log.d("osu.appclub", e.getMessage());
            return null;
        }
    }

    @Nullable
    public static StopDetailsViewModel getStopDetailsViewModel(int stopId) {
        BusStaticData staticData = getStaticData();
        List<RouteArrivalsSummary> arrivalsSummaries = getRouteArrivalsSummary(stopId);

        if (staticData != null && arrivalsSummaries != null) {
            return new StopDetailsViewModel(stopId, staticData, arrivalsSummaries);
        }

        return null;
    }
}

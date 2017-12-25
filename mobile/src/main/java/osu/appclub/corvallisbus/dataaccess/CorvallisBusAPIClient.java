package osu.appclub.corvallisbus.dataaccess;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import osu.appclub.corvallisbus.models.AlertsItem;
import osu.appclub.corvallisbus.models.BusRoute;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.models.RouteArrivalsSummary;
import osu.appclub.corvallisbus.models.RouteDetailsViewModel;

@WorkerThread
public final class CorvallisBusAPIClient {
    static final String BASE_URL = "https://corvallisb.us/api";
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .registerTypeAdapter(BusRoute.class, new BusRoute.Deserializer())
            .registerTypeAdapter(BusStop.class, new BusStop.Deserializer())
            .registerTypeAdapter(BusStaticData.class, new BusStaticData.Deserializer())
            .registerTypeAdapter(Uri.class, new UriDeserializer())
            .create();

    private static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(final JsonElement src, final Type srcType,
                               final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }

    private CorvallisBusAPIClient() {

    }

    private static final Type favoriteStopsListType = new TypeToken<List<FavoriteStopViewModel>>(){}.getType();
    @Nullable
    public static List<FavoriteStopViewModel> getFavoriteStops(@NotNull List<Integer> stopIds, Location location) {
        if (stopIds.size() == 0 && location == null) {
            return new ArrayList<>();
        }

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
    static List<RouteArrivalsSummary> getRouteArrivalsSummary(int stopId) {
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
    public static List<RouteDetailsViewModel> getRouteDetailsViewModels(int stopId) {
        BusStaticData staticData = getStaticData();
        List<RouteArrivalsSummary> arrivalsSummaries = getRouteArrivalsSummary(stopId);

        if (staticData == null || arrivalsSummaries == null) {
            return null;
        }

        List<RouteDetailsViewModel> viewModels = new ArrayList<>();
        for (RouteArrivalsSummary arrivalsSummary : arrivalsSummaries) {
            BusRoute route = staticData.routes.get(arrivalsSummary.routeName);
            viewModels.add(new RouteDetailsViewModel(arrivalsSummary, route));
        }

        return viewModels;
    }

    private static final Type alertsItemsType = new TypeToken<List<AlertsItem>>(){}.getType();
    @Nullable
    public static List<AlertsItem> getServiceAlerts() {
        try {
            URL url = new URL(BASE_URL + "/service-alerts");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());
            List<AlertsItem> alertsItems = gson.fromJson(streamReader, alertsItemsType);
            return alertsItems;
        } catch (Exception e) {
            Log.d("osu.appclub", e.getMessage());
            return null;
        }
    }
}

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

import osu.appclub.corvallisbus.models.BusRoute;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;
import osu.appclub.corvallisbus.models.BusStop;
import osu.appclub.corvallisbus.models.RouteArrivalsSummary;

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
    private static Gson gson = new GsonBuilder()
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
        String urlString = "https://corvallisb.us/api/favorites?stops=" +
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

    @Nullable
    public static BusStaticData getStaticData() {
        if (staticDataCache != null) {
            return staticDataCache;
        }

        try {
            URL url = new URL("https://corvallisb.us/api/static");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());

            staticDataCache = gson.fromJson(streamReader, BusStaticData.class);
            return staticDataCache;
        }
        catch (Exception e) {
            Log.d("osu.appclub", e.getMessage());
            return null;
        }
    }
}

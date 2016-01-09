package osu.appclub.corvallisbus.apiclient;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.models.FavoriteStopViewModel;
import osu.appclub.corvallisbus.models.TransitRoute;
import osu.appclub.corvallisbus.models.TransitStop;

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

public final class CorvallisBusAPIClient {
    private static Gson gson = new Gson();
    // TODO: make this a Map
    // TODO: figure out if static fields persist longer or something
    //Array of key value pairs to represent all transit stops
    //Pair<Integer, TransitStop> -- <StopID, TransitStop>
    //Using the StopID as the key will make getting/setting stop info easy
    private static List<Pair<Integer, TransitStop>> TRANSIT_STOPS;

    //Array of key value pairs to represent all transit routes <RouteName, TransitRoute>
    private static List<Pair<String, TransitRoute>> TRANSIT_ROUTES;

    //Array of TransitStops representing the user's favorites
    private static ArrayList<TransitStop> TRANSIT_FAVS;

    private CorvallisBusAPIClient() {
        TRANSIT_STOPS = new ArrayList<>();
        TRANSIT_ROUTES = new ArrayList<>();
        TRANSIT_FAVS = new ArrayList<>();
    }

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

            Type listType = new TypeToken<List<FavoriteStopViewModel>>(){}.getType();
            List<FavoriteStopViewModel> favorites = gson.fromJson(streamReader, listType);
            return favorites;
        }
        catch (Exception e) {
            Log.d("osu.appclub", e.getMessage());
            return null;
        }
    }

    public static void populateFavorites() {
        TRANSIT_FAVS = new ArrayList<>();

        //DUMMY DATA FOR NOW
        TransitStop stop1 = new TransitStop();
        TransitStop stop2 = new TransitStop();
        TransitStop stop3 = new TransitStop();

        stop1.setName("STOP 1");
        stop2.setName("STOP 2");
        stop3.setName("STOP 3");

        TRANSIT_FAVS.add(stop1);
        TRANSIT_FAVS.add(stop2);
        TRANSIT_FAVS.add(stop3);
    }

    public static ArrayList<TransitStop> getFavorites() {
        return TRANSIT_FAVS;
    }
}

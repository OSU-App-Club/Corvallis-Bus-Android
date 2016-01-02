package osu.appclub.corvallisbus.API;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

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

public final class TransitAPI {
    //Array of key value pairs to represent all transit stops
    //Pair<Integer, TransitStop> -- <StopID, TransitStop>
    //Using the StopID as the key will make getting/setting stop info easy
    private static List<Pair<Integer, TransitStop>> TRANSIT_STOPS;

    //Array of key value pairs to represent all transit routes <RouteName, TransitRoute>
    private static List<Pair<String, TransitRoute>> TRANSIT_ROUTES;

    //Array of TransitStops representing the user's favorites
    private static ArrayList<TransitStop> TRANSIT_FAVS;

    private TransitAPI() {
        TRANSIT_STOPS = new ArrayList<>();
        TRANSIT_ROUTES = new ArrayList<>();
        TRANSIT_FAVS = new ArrayList<>();
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

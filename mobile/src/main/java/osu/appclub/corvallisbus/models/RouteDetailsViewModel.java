package osu.appclub.corvallisbus.models;

import android.net.Uri;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by rikkigibson on 1/16/16.
 * Represents data to be displayed in a row of the stop details table.
 */
public class RouteDetailsViewModel {
    public String routeName;
    public int routeColor;
    public Uri url;
    public PolylineOptions polyline;
    public String arrivalsSummary;
    public String scheduleSummary;

    public RouteDetailsViewModel(RouteArrivalsSummary arrivalsSummary, BusRoute route) {
        this.routeName = route.routeNo;
        this.routeColor = route.color;
        this.url = route.url;
        this.polyline = route.polyline;
        this.arrivalsSummary = arrivalsSummary.arrivalsSummary;
        this.scheduleSummary = arrivalsSummary.scheduleSummary;
    }
}

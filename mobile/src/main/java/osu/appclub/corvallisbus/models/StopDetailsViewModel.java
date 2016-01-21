package osu.appclub.corvallisbus.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rikkigibson on 1/20/16.
 * Contains view state for the Stops view.
 * Eventually will contain indication of whether the stop is a favorite, etc.
 */
public class StopDetailsViewModel {
    public String stopName;
    public List<RouteDetailsViewModel> routeDetailsList;

    public StopDetailsViewModel(int stopId, BusStaticData staticData, List<RouteArrivalsSummary> arrivalsSummaries) {
        stopName = staticData.stops.get(stopId).name;
        routeDetailsList = new ArrayList<>();

        for (RouteArrivalsSummary arrivalsSummary : arrivalsSummaries) {
            RouteDetailsViewModel routeDetails = new RouteDetailsViewModel(arrivalsSummary, staticData.routes.get(arrivalsSummary.routeName));
            routeDetailsList.add(routeDetails);
        }
    }
}

package osu.appclub.corvallisbus.models;

/**
 * Created by rikkigibson on 1/2/16.
 */
public class FavoriteStopViewModel {
    private String stopName;
    private int stopID;
    private String distanceFromUser;
    private boolean isNearestStop;

    private String firstRouteColor;
    private String firstRouteName;
    private String firstRouteArrivals;

    private String secondRouteColor;
    private String secondRouteName;
    private String secondRouteArrivals;

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public int getStopID() {
        return stopID;
    }

    public void setStopID(int stopID) {
        this.stopID = stopID;
    }

    public String getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(String distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    public boolean isNearestStop() {
        return isNearestStop;
    }

    public void setIsNearestStop(boolean isNearestStop) {
        this.isNearestStop = isNearestStop;
    }

    public String getFirstRouteColor() {
        return firstRouteColor;
    }

    public void setFirstRouteColor(String firstRouteColor) {
        this.firstRouteColor = firstRouteColor;
    }

    public String getFirstRouteName() {
        return firstRouteName;
    }

    public void setFirstRouteName(String firstRouteName) {
        this.firstRouteName = firstRouteName;
    }

    public String getFirstRouteArrivals() {
        return firstRouteArrivals;
    }

    public void setFirstRouteArrivals(String firstRouteArrivals) {
        this.firstRouteArrivals = firstRouteArrivals;
    }

    public String getSecondRouteColor() {
        return secondRouteColor;
    }

    public void setSecondRouteColor(String secondRouteColor) {
        this.secondRouteColor = secondRouteColor;
    }

    public String getSecondRouteName() {
        return secondRouteName;
    }

    public void setSecondRouteName(String secondRouteName) {
        this.secondRouteName = secondRouteName;
    }

    public String getSecondRouteArrivals() {
        return secondRouteArrivals;
    }

    public void setSecondRouteArrivals(String secondRouteArrivals) {
        this.secondRouteArrivals = secondRouteArrivals;
    }
}

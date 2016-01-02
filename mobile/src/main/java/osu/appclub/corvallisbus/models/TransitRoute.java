package osu.appclub.corvallisbus.models;

import com.google.android.gms.maps.model.Polyline;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TransitRoute {
    private String name;
    private List<Integer> path;
    private String color;
    private URL url;
    private Polyline poly;

    public TransitRoute() {
        name = "";
        path = new ArrayList<>();
        color = "";
    }
}

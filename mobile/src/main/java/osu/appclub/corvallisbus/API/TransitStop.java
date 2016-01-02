package osu.appclub.corvallisbus.API;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TransitStop {
    //Transit Stop Information
    private int ID ;
    private String name;
    private LatLng location;
    private List<String> routes;

    public TransitStop() {
        ID = -1;
        name = "";
        location = new LatLng(0.0, 0.0);
        routes = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

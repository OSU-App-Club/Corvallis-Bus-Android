package osu.appclub.corvallisbus.models;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.maps.android.PolyUtil;

import java.lang.reflect.Type;
import java.util.List;

public class BusRoute {
    public String routeNo;
    public int[] path;
    public int color;
    public Uri url;
    public PolylineOptions polyline;

    public static class Deserializer implements JsonDeserializer<BusRoute> {

        @Override
        public BusRoute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            BusRoute busRoute = new BusRoute();

            busRoute.routeNo = jsonObject.get("routeNo").getAsString();
            busRoute.color = Translation.toColorValue(jsonObject.get("color").getAsString());
            busRoute.path = context.deserialize(jsonObject.get("path"), int[].class);
            busRoute.url = Uri.parse(jsonObject.get("url").getAsString());

            String encodedPoints = jsonObject.get("polyline").getAsString();
            List<LatLng> points = PolyUtil.decode(encodedPoints);

            PolylineOptions options = new PolylineOptions();
            options.color(busRoute.color);
            options.addAll(points);
            busRoute.polyline = options;

            return busRoute;
        }
    }
}

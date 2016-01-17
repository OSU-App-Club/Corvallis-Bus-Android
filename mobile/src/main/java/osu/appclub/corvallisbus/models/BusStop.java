package osu.appclub.corvallisbus.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BusStop {
    public int id;
    public String name;
    public LatLng location;
    public String[] routeNames;

    public static class Deserializer implements JsonDeserializer<BusStop> {

        @Override
        public BusStop deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            BusStop busStop = new BusStop();

            busStop.id = jsonObject.get("id").getAsInt();
            busStop.name = jsonObject.get("name").getAsString();
            busStop.location = new LatLng(jsonObject.get("lat").getAsDouble(), jsonObject.get("lng").getAsDouble());
            busStop.routeNames = context.deserialize(jsonObject.get("routeNames"), String[].class);

            return busStop;
        }
    }
}

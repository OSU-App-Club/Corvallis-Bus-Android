package osu.appclub.corvallisbus.models;

import android.util.SparseArray;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by rikkigibson on 1/17/16.
 * Deserialization target for static data endpoint.
 */
public class BusStaticData {
    Map<String, BusRoute> routes;
    SparseArray<BusStop> stops;

    public static class Deserializer implements JsonDeserializer<BusStaticData> {
        final Type routesType = new TypeToken<Map<String, BusRoute>>(){}.getType();

        @Override
        public BusStaticData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            BusStaticData staticData = new BusStaticData();

            JsonObject stopsDictionary = jsonObject.get("stops").getAsJsonObject();
            SparseArray<BusStop> stops = new SparseArray<>();
            for (Map.Entry<String, JsonElement> element : stopsDictionary.entrySet()) {
                BusStop stop = context.deserialize(element.getValue(), BusStop.class);
                stops.put(Integer.parseInt(element.getKey()), stop);
            }
            staticData.stops = stops;
            staticData.routes = context.deserialize(jsonObject.get("routes"), routesType);

            return staticData;
        }
    }
}

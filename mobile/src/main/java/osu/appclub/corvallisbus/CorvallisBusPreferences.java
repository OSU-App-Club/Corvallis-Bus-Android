package osu.appclub.corvallisbus;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by rikkigibson on 1/23/16.
 */
public class CorvallisBusPreferences {
    private final Context context;
    private static final Gson gson = new Gson();

    public static final String PREFERENCES_KEY = "osu.appclub.corvallisbus";
    public static final String FAVORITE_STOPS_KEY = "favoriteStops";

    public CorvallisBusPreferences(Context context) {
        this.context = context;
    }

    private static final Type INTEGER_LIST_TYPE = new TypeToken<List<Integer>>(){}.getType();
    public List<Integer> getFavoriteStopIds() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        String favoriteStopsJson = sharedPreferences.getString(FAVORITE_STOPS_KEY, "[]");
        List<Integer> favoriteStops = gson.fromJson(favoriteStopsJson, INTEGER_LIST_TYPE);
        return favoriteStops;
    }

    public void setFavoriteStopIds(@NonNull List<Integer> favoriteStopIds) {
        SharedPreferences.Editor preferencesEditor = context.getSharedPreferences(PREFERENCES_KEY, 0).edit();
        String favoriteStopsJson = gson.toJson(favoriteStopIds);
        preferencesEditor.putString(FAVORITE_STOPS_KEY, favoriteStopsJson);
        preferencesEditor.apply();
    }
}

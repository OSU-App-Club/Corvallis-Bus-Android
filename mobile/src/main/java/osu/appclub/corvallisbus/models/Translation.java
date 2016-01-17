package osu.appclub.corvallisbus.models;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

/**
 * Created by rikkigibson on 1/16/16.
 */
public class Translation {
    public static int toColorValue(@NotNull String color) {
        if (color.isEmpty()) {
            return 0;
        }
        try {
            int colorValue = Integer.parseInt(color, 16) + 0xFF000000;
            return colorValue;
        }
        catch (Exception e) {
            Log.d("osu.appclub", "Route color failed to parse");
            return 0;
        }
    }
}


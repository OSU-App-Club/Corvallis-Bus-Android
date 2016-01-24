package osu.appclub.corvallisbus;

import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by rikkigibson on 1/22/16.
 */
public interface LocationProvider {
    boolean isLocationAvailable();

    @Nullable
    Location getUserLocation();

    void addLocationAvailableListener(LocationAvailableListener listener);
    void removeLocationAvailableListener(LocationAvailableListener listener);

    interface LocationAvailableListener {
        void onLocationAvailable(LocationProvider provider);
    }
}

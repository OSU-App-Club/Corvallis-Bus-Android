package osu.appclub.corvallisbus;

import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by rikkigibson on 1/22/16.
 */
public interface LocationProvider {
    /**
     * Checks if location is available and initiates location permissions checks in Android 6.
     *
     * @return A value indicating whether the user's location is not pending availability.
     * For instance, it returns false if the location service is still spinning up.
     * It returns false if the user has not yet granted permission.
     * It returns true if the location service is available, or if the user has denied permission.
     */
    boolean isLocationResolved();

    @Nullable
    Location getUserLocation();

    void addLocationResolutionListener(LocationAvailableListener listener);
    void removeLocationResolutionListener(LocationAvailableListener listener);

    interface LocationAvailableListener {
        void onLocationResolved(LocationProvider provider);
    }
}

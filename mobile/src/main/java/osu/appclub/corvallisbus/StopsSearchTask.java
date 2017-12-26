package osu.appclub.corvallisbus;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.BusStaticData;
import osu.appclub.corvallisbus.models.BusStop;

public class StopsSearchTask extends AsyncTask<Void, Void, Cursor> {
    private final WeakReference<? extends Listener> listenerRef;
    private final List<Pattern> searchPatterns;

    @Nullable
    private final Location location;
    private static final double METERS_PER_MILE = 1609.344;

    interface Listener {
        void searchComplete(Cursor cursor);
    }

    StopsSearchTask(WeakReference<? extends Listener> listenerRef, String searchText, @Nullable Location location) {
        this.listenerRef = listenerRef;
        this.location = location;

        searchPatterns = new ArrayList<>();
        String[] parts = searchText.split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                searchPatterns.add(Pattern.compile(part, Pattern.CASE_INSENSITIVE + Pattern.LITERAL));
            }
        }
    }

    @Override
    protected Cursor doInBackground(Void... voids) {
        BusStaticData staticData = CorvallisBusAPIClient.getStaticData();
        if (staticData == null) {
            return null;
        }

        SparseArray<BusStop> stops = staticData.stops;
        List<Object[]> rows = new ArrayList<>();
        float[] distanceResult = new float[1];
        for (int i = 0; i < stops.size(); i++) {
            BusStop candidate = stops.valueAt(i);
            boolean matchesAll = true;
            for (Pattern pattern : searchPatterns) {
                matchesAll = matchesAll && pattern.matcher(candidate.name).find();
            }

            if (matchesAll) {
                Double distance;
                if (location == null) {
                    distance = null;
                } else {
                    Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                            candidate.location.latitude, candidate.location.longitude, distanceResult);
                    distance = distanceResult[0] / METERS_PER_MILE;
                }
                rows.add(new Object[] { candidate.id, candidate.name, distance });
            }
        }

        Collections.sort(rows, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] a1, Object[] a2) {
                if (a1[2] == null || a2[2] == null) {
                    return 0;
                }

                return (int) (((double)a1[2] - (double)a2[2]) * 100);
            }
        });

        MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "stopName", "distance" });
        for (Object[] row : rows) {
            cursor.addRow(row);
        }

        return cursor;
    }

    @Override
    protected void onPostExecute(Cursor busStops) {
        Listener listener = listenerRef.get();
        if (listener != null) {
            listener.searchComplete(busStops);
        }
    }
}
package osu.appclub.corvallisbus.widget;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.MainActivity;
import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusPreferences;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;
import osu.appclub.corvallisbus.models.Translation;

public class CorvallisBusWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CorvallisBusRemoteViewsFactory(getApplicationContext());
    }
}

class CorvallisBusRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private List<FavoriteStopViewModel> favoriteStops = new ArrayList<>();
    private final Context context;
    private final GoogleApiClient apiClient;

    CorvallisBusRemoteViewsFactory(Context context) {
        this.context = context;
        apiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onCreate() {
        Log.d("osu.appclub", "WIDGET: RemoteViewsFactory.onCreate called");
        apiClient.connect();
    }

    @Override
    public void onDestroy() {
        apiClient.disconnect();
    }

    @Override
    public int getCount() {
        return favoriteStops.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_row);
        FavoriteStopViewModel favorite = favoriteStops.get(i);

        rv.setTextViewText(R.id.stopName, favorite.stopName);

        rv.setViewVisibility(R.id.isNearestStop, favorite.isNearestStop ? View.VISIBLE : View.INVISIBLE);
        rv.setTextViewText(R.id.distanceFromUser, favorite.distanceFromUser);

        rv.setTextViewText(R.id.firstRouteName, favorite.firstRouteName);

        rv.setTextViewText(R.id.firstRouteArrivals, favorite.firstRouteArrivals);
        rv.setImageViewBitmap(R.id.firstRouteBackground, getBackground(context, Translation.toColorValue(favorite.firstRouteColor)));

        rv.setTextViewText(R.id.secondRouteName, favorite.secondRouteName);
        rv.setTextViewText(R.id.secondRouteArrivals, favorite.secondRouteArrivals);
        rv.setImageViewBitmap(R.id.secondRouteBackground, getBackground(context, Translation.toColorValue(favorite.secondRouteColor)));

        Intent stopDetails = new Intent();
        stopDetails.putExtra(MainActivity.EXTRA_STOP_ID, favorite.stopID);
        rv.setOnClickFillInIntent(R.id.widget_row, stopDetails);

        return rv;
    }

    private static Bitmap getBackground(Context context, int bgColor) {
        float dp = context.getResources().getDisplayMetrics().density;

        GradientDrawable grad = new GradientDrawable();
        grad.setColor(bgColor);
        grad.setCornerRadius(5.5f*dp);

        Bitmap bitmap = Bitmap.createBitmap(Math.round(50*dp), Math.round(20*dp), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        grad.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        grad.draw(canvas);

        return bitmap;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    private Location getUserLocation() {
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(apiClient);
        } else {
            return null;
        }
    }

    @Override
    public void onDataSetChanged() {
        Log.d("osu.appclub", "WIDGET: RemoteViewsFactory.onDataSetChanged called");
        List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);
        Location loc = getUserLocation();

        favoriteStops.clear();
        List<FavoriteStopViewModel> newFavorites = CorvallisBusAPIClient.getFavoriteStops(favoriteStopIds, loc);
        if (newFavorites == null) {
            // TODO: send intent back to provider to display toast
            Log.d("osu.appclub", "WIDGET: Failed to load favorites");
        } else {
            favoriteStops.addAll(newFavorites);
        }
    }

}
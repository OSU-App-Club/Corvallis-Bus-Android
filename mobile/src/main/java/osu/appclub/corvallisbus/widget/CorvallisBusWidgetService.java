package osu.appclub.corvallisbus.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

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
    CorvallisBusRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        Log.d("osu.appclub", "WIDGET: RemoteViewsFactory.onCreate called");
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return favoriteStops.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.favorites_row);
        FavoriteStopViewModel favorite = favoriteStops.get(i);

        int textColor = ContextCompat.getColor(context, R.color.colorWidgetText);
        rv.setTextViewText(R.id.stopName, favorite.stopName);
        rv.setTextColor(R.id.stopName, textColor);

        rv.setViewVisibility(R.id.isNearestStop, favorite.isNearestStop ? View.VISIBLE : View.INVISIBLE);
        rv.setTextViewText(R.id.distanceFromUser, favorite.distanceFromUser);
        rv.setTextColor(R.id.distanceFromUser, textColor);

        rv.setTextViewText(R.id.firstRouteName, favorite.firstRouteName);
        // TODO: figure out how to set background of gradient drawable (does it need to be in layout XML and get an ID?)
        rv.setInt(R.id.firstRouteName, "setBackgroundColor", Translation.toColorValue(favorite.firstRouteColor));
        rv.setTextViewText(R.id.firstRouteArrivals, favorite.firstRouteArrivals);
        rv.setTextColor(R.id.firstRouteArrivals, textColor);

        rv.setTextViewText(R.id.secondRouteName, favorite.secondRouteName);
        rv.setInt(R.id.secondRouteName, "setBackgroundColor", Translation.toColorValue(favorite.secondRouteColor));
        rv.setTextViewText(R.id.secondRouteArrivals, favorite.secondRouteArrivals);
        rv.setTextColor(R.id.secondRouteArrivals, textColor);

        Intent stopDetails = new Intent();
        stopDetails.putExtra(MainActivity.EXTRA_STOP_ID, favorite.stopID);
        rv.setOnClickFillInIntent(R.id.favorites_row, stopDetails);

        return rv;
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

    @Override
    public void onDataSetChanged() {
        Log.d("osu.appclub", "WIDGET: RemoteViewsFactory.onDataSetChanged called");
        List<Integer> favoriteStopIds = CorvallisBusPreferences.getFavoriteStopIds(context);

        favoriteStops.clear();
        List<FavoriteStopViewModel> newFavorites = CorvallisBusAPIClient.getFavoriteStops(favoriteStopIds, null);
        if (newFavorites == null) {
            // TODO: send intent back to provider to display toast
            Log.d("osu.appclub", "WIDGET: Failed to load favorites");
        } else {
            favoriteStops.addAll(newFavorites);
        }
    }

}
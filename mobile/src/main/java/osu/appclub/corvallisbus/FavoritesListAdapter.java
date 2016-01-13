package osu.appclub.corvallisbus;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import osu.appclub.corvallisbus.models.FavoriteStopViewModel;

public class FavoritesListAdapter extends ArrayAdapter<FavoriteStopViewModel> {

    private final Context context;
    private final ArrayList<FavoriteStopViewModel> favorites;

    public FavoritesListAdapter(Context context, ArrayList<FavoriteStopViewModel> favorites) {
        super(context, R.layout.favorites_row, favorites);
        this.context = context;
        this.favorites = favorites;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflate our row
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.favorites_row, parent, false);
        }

        FavoriteStopViewModel favorite = favorites.get(position);

        //Set the UI elements accordingly
        ((TextView)convertView.findViewById(R.id.stopName)).setText(favorite.stopName);

        TextView firstRouteName = (TextView)convertView.findViewById(R.id.firstRouteName);
        firstRouteName.setText(favorite.firstRouteName);
        firstRouteName.setBackgroundColor(favorite.firstRouteColor.isEmpty() ? Color.GRAY : toColorValue(favorite.firstRouteColor));

        ((TextView)convertView.findViewById(R.id.firstRouteArrivals)).setText(favorite.firstRouteArrivals);

        TextView secondRouteName = (TextView)convertView.findViewById(R.id.secondRouteName);
        secondRouteName.setText(favorite.secondRouteName);
        secondRouteName.setBackgroundColor(toColorValue(favorite.secondRouteColor));

        ((TextView)convertView.findViewById(R.id.secondRouteArrivals)).setText(favorite.secondRouteArrivals);

        // change these types of statements to 2-liners if desired
        ((TextView)convertView.findViewById(R.id.distanceFromUser)).setText(favorite.distanceFromUser);
        convertView.findViewById(R.id.isNearestStop).setVisibility(favorite.isNearestStop ? View.VISIBLE : View.INVISIBLE);

        //Return our row
        return convertView;
    }

    public int toColorValue(@NotNull String color) {
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

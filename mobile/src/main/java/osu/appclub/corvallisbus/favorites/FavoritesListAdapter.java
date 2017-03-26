package osu.appclub.corvallisbus.favorites;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.models.FavoriteStopViewModel;
import osu.appclub.corvallisbus.models.Translation;

public class FavoritesListAdapter extends ArrayAdapter<FavoriteStopViewModel> {

    // TODO: are these instance variables needed?
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

        GradientDrawable firstBG = new GradientDrawable();
        firstBG.setColor(favorite.firstRouteColor.isEmpty() ? Color.GRAY : Translation.toColorValue(favorite.firstRouteColor));
        firstBG.setCornerRadius(15);
        firstRouteName.setBackground(firstBG);

        ((TextView)convertView.findViewById(R.id.firstRouteArrivals)).setText(favorite.firstRouteArrivals);

        TextView secondRouteName = (TextView)convertView.findViewById(R.id.secondRouteName);
        secondRouteName.setText(favorite.secondRouteName);

        GradientDrawable secondBG = new GradientDrawable();
        secondBG.setColor(Translation.toColorValue(favorite.secondRouteColor));
        secondBG.setCornerRadius(15);
        secondRouteName.setBackground(secondBG);

        ((TextView)convertView.findViewById(R.id.secondRouteArrivals)).setText(favorite.secondRouteArrivals);

        // change these types of statements to 2-liners if desired
        ((TextView)convertView.findViewById(R.id.distanceFromUser)).setText(favorite.distanceFromUser);
        convertView.findViewById(R.id.isNearestStop).setVisibility(favorite.isNearestStop ? View.VISIBLE : View.INVISIBLE);

        //Return our row
        return convertView;
    }
}

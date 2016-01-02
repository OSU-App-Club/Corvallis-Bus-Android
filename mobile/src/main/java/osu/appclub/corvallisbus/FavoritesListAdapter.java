package osu.appclub.corvallisbus;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import osu.appclub.corvallisbus.models.TransitStop;

public class FavoritesListAdapter extends ArrayAdapter<TransitStop> {

    private final Context context;
    private final ArrayList<TransitStop> favs;

    public FavoritesListAdapter(Context context, ArrayList<TransitStop> favs) {
        super(context, R.layout.favorites_row, favs);
        this.context = context;
        this.favs = favs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflate our row
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.favorites_row, parent, false);

        //Set the UI elements accordingly
        TextView stopName = (TextView)rowView.findViewById(R.id.stopName);
        stopName.setText(favs.get(position).getName());

        //Return our row
        return rowView;
    }
}

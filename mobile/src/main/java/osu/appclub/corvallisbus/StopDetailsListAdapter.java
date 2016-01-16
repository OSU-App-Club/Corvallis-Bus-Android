package osu.appclub.corvallisbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import osu.appclub.corvallisbus.models.RouteDetailsViewModel;
import osu.appclub.corvallisbus.models.Translation;

/**
 * Created by rikkigibson on 1/16/16.
 */
public class StopDetailsListAdapter extends ArrayAdapter<RouteDetailsViewModel> {
    public StopDetailsListAdapter(Context context, ArrayList<RouteDetailsViewModel> list) {
        super(context, R.layout.stop_details_row, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.stop_details_row, parent, false);
        }

        RouteDetailsViewModel routeDetails = getItem(position);

        TextView routeName = (TextView) convertView.findViewById(R.id.routeName);
        routeName.setText(routeDetails.routeName);
        routeName.setBackgroundColor(routeDetails.routeColor);

        ((TextView)convertView.findViewById(R.id.arrivalsSummary)).setText(routeDetails.arrivalsSummary);
        ((TextView)convertView.findViewById(R.id.arrivalsSummary)).setText(routeDetails.arrivalsSummary);


        return convertView;
    }
}

package osu.appclub.corvallisbus.browsestops;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.maps.android.heatmaps.Gradient;

import java.util.ArrayList;

import osu.appclub.corvallisbus.R;
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

        final RouteDetailsViewModel routeDetails = getItem(position);
        assert routeDetails != null;

        TextView routeName = (TextView) convertView.findViewById(R.id.routeName);
        routeName.setText(routeDetails.routeName);

        float dp = getContext().getResources().getDisplayMetrics().density;
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(6.67f*dp);
        bg.setColor(routeDetails.routeColor);
        routeName.setBackground(bg);

        ((TextView)convertView.findViewById(R.id.arrivalsSummary)).setText(routeDetails.arrivalsSummary);
        ((TextView)convertView.findViewById(R.id.scheduleSummary)).setText(routeDetails.scheduleSummary);
        convertView.findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, routeDetails.url);
                try {
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Couldn't open the web page for the selected route.", Toast.LENGTH_SHORT)
                         .show();
                }
            }
        });

        return convertView;
    }
}

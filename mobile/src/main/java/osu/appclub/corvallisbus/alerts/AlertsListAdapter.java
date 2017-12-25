package osu.appclub.corvallisbus.alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import osu.appclub.corvallisbus.models.AlertsItem;

/**
 * Created by rikkigibson on 1/3/16.
 */
public class AlertsListAdapter extends ArrayAdapter<AlertsItem> {
    final private static DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);

    public AlertsListAdapter(Context context, List<AlertsItem> objects) {
        super(context, android.R.layout.simple_list_item_2, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        AlertsItem alertsItem = getItem(position);
        TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
        text1.setText(alertsItem.title);

        TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
        text2.setText(dateFormat.format(alertsItem.publishDate));

        return convertView;
    }
}

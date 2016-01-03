package osu.appclub.corvallisbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.mcsoxford.rss.RSSItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by rikkigibson on 1/3/16.
 */
public class AlertsListAdapter extends ArrayAdapter<RSSItem> {
    final DateFormat dateFormat;

    public AlertsListAdapter(Context context, List<RSSItem> objects) {
        super(context, android.R.layout.simple_list_item_2, objects);
        this.dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // TODO: not sure how to implement holder pattern as the warning suggests
        View view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);

        RSSItem rssItem = getItem(position);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        text1.setText(rssItem.getTitle());

        String formattedDate = dateFormat.format(rssItem.getPubDate());
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        text2.setText(formattedDate);

        return view;
    }
}

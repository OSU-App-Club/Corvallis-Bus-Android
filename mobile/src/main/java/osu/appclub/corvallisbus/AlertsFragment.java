package osu.appclub.corvallisbus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import osu.appclub.corvallisbus.models.AlertsItem;


public class AlertsFragment extends ListFragment {
    final AlertsItem PLACEHOLDER_ITEM;
    final ArrayList<AlertsItem> listItems = new ArrayList<>();
    AlertsListAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    //Required empty public constructor
    public AlertsFragment() {
        PLACEHOLDER_ITEM = new AlertsItem();
        PLACEHOLDER_ITEM.title = "No current service alerts!";
        PLACEHOLDER_ITEM.dateText = "Tap to view the service alerts website";
        PLACEHOLDER_ITEM.link = Uri.parse("http://www.corvallisoregon.gov/index.aspx?page=1105");
    }

    //Create a new instance of this fragment using the provided parameters.
    public static AlertsFragment newInstance() {
        AlertsFragment fragment = new AlertsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_alerts, container, false);
        return root;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        AlertsItem alertsItem = listItems.get(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, alertsItem.link);
        try {
            startActivity(intent);
        }
        catch (Exception e) {
            Toast.makeText(getActivity(), "Couldn't open the link for the selected item.", Toast.LENGTH_SHORT)
                 .show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoadingAlerts();
            }
        });

        startLoadingAlerts();
    }

    public void startLoadingAlerts() {
        if (adapter == null) {
            adapter = new AlertsListAdapter(getActivity(), listItems);
            setListAdapter(adapter);
        }

        AsyncTask<Void, Void, List<AlertsItem>> task = new AsyncTask<Void, Void, List<AlertsItem>>() {
            final String FEED_URI = "https://www.corvallisoregon.gov/Rss.aspx?type=5&cat=100,104,105,106,107,108,109,110,111,112,113,114,58,119&dept=12&paramtime=Current";
            final RSSReader reader = new RSSReader();

            @Override
            protected List<AlertsItem> doInBackground(Void... params) {
                try {
                    RSSFeed feed = reader.load(FEED_URI);
                    List<RSSItem> rssItems = feed.getItems();

                    ArrayList<AlertsItem> alertsItems = new ArrayList<>(rssItems.size());
                    for (RSSItem rssItem : rssItems) {
                        alertsItems.add(AlertsItem.fromRSSItem(rssItem));
                    }
                    return alertsItems;
                }
                catch(Exception e) {
                    Log.d("corvallisbus", "RSS reader failed to get items");
                    Log.d("corvallisbus", e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<AlertsItem> alertsItems) {
                listItems.clear();

                if (alertsItems == null) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to load Service Alerts feed", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    listItems.addAll(alertsItems);
                }

                if (listItems.isEmpty()) {
                    listItems.add(PLACEHOLDER_ITEM);
                }

                // better to just have the app explode if this thing is somehow null
                adapter.notifyDataSetInvalidated();

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        };
        task.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

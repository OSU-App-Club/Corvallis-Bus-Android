package osu.appclub.corvallisbus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import java.util.ArrayList;
import java.util.List;


public class AlertsFragment extends ListFragment {
    final ArrayList<RSSItem> listItems = new ArrayList<>();

    //Required empty public constructor
    public AlertsFragment() {

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
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        RSSItem rssItem = listItems.get(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, rssItem.getLink());
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

        final AlertsListAdapter adapter = new AlertsListAdapter(getActivity(), listItems);
        setListAdapter(adapter);

        // TODO: This captures several members of the parent class. Should it be factored into a named class?
        AsyncTask<Void, Void, List<RSSItem>> task = new AsyncTask<Void, Void, List<RSSItem>>() {
            final String FEED_URI = "https://www.corvallisoregon.gov/Rss.aspx?type=5&cat=100,104,105,106,107,108,109,110,111,112,113,114,58,119&dept=12&paramtime=Current";
            final RSSReader reader = new RSSReader();

            @Override
            protected List<RSSItem> doInBackground(Void... params) {
                try {
                    RSSFeed feed = reader.load(FEED_URI);
                    List<RSSItem> items = feed.getItems();
                    return items;
                }
                catch(RSSReaderException e) {
                    Log.d("corvallisbus", "RSS reader failed to get items");
                    Log.d("corvallisbus", e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<RSSItem> rssItems) {
                listItems.clear();

                if (rssItems == null) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to load Service Alerts feed", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    listItems.addAll(rssItems);
                    adapter.notifyDataSetInvalidated();
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

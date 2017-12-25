package osu.appclub.corvallisbus.alerts;

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
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.Toast;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;

import java.util.ArrayList;
import java.util.List;

import osu.appclub.corvallisbus.R;
import osu.appclub.corvallisbus.dataaccess.CorvallisBusAPIClient;
import osu.appclub.corvallisbus.models.AlertsItem;


public class AlertsFragment extends ListFragment {
    final ArrayList<AlertsItem> listItems = new ArrayList<>();
    AlertsListAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

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
        View root = inflater.inflate(R.layout.fragment_alerts, container, false);
        return root;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
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

    private static final Uri SERVICE_ALERTS_URI = Uri.parse("https://corvallisb.us/api/service-alerts/html");
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.alerts_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoadingAlerts();
            }
        });

        View placeholder = getActivity().findViewById(R.id.alerts_placeholder);
        placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, SERVICE_ALERTS_URI);
                startActivity(intent);
            }
        });

        getListView().setEmptyView(placeholder);
        // This crazy setup is the "best" way to make the layout start refreshing as soon as it is able.
        swipeRefreshLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                swipeRefreshLayout
                                        .getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                                swipeRefreshLayout.setRefreshing(true);
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
                Log.d("osu.appclub", "Loading service alerts from background thread");
                List<AlertsItem> alertsItems = CorvallisBusAPIClient.getServiceAlerts();
                return alertsItems;
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

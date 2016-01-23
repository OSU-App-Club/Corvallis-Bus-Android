package osu.appclub.corvallisbus.models;

import android.net.Uri;

import org.mcsoxford.rss.RSSItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by rikkigibson on 1/11/16.
 * Because RSSItem has private constructors and setters for reasons and I decided against mucking with it.
 */
public class AlertsItem {
    final private static DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);

    public String title;
    public String dateText;
    public Uri link;

    public static AlertsItem fromRSSItem(RSSItem rssItem) {
        AlertsItem alertsItem = new AlertsItem();
        alertsItem.title = rssItem.getTitle();
        alertsItem.dateText = dateFormat.format(rssItem.getPubDate());
        alertsItem.link = rssItem.getLink();

        return alertsItem;
    }
}

package osu.appclub.corvallisbus.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import osu.appclub.corvallisbus.MainActivity;
import osu.appclub.corvallisbus.R;

public class CorvallisBusWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("osu.appclub", "WIDGET: AppWidgetProvider.onUpdate called");
        for (int id : appWidgetIds) {
            Intent intent = new Intent(context, CorvallisBusWidgetService.class);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.corvallisbus_appwidget);
            rv.setRemoteAdapter(R.id.widget_list, intent);
            rv.setEmptyView(R.id.widget_list, R.id.widget_placeholder);

            Intent launchApp = new Intent(context, MainActivity.class);
            PendingIntent piPlaceholder = PendingIntent.getActivity(context, 0, launchApp, 0);
            rv.setOnClickPendingIntent(R.id.widget_placeholder, piPlaceholder);

            Intent viewStopDetails = new Intent(context, MainActivity.class);
            viewStopDetails.setAction(MainActivity.VIEW_STOP_ACTION);
            PendingIntent piStopDetails = PendingIntent.getActivity(context, 0, viewStopDetails, 0);
            rv.setPendingIntentTemplate(R.id.widget_list, piStopDetails);

            appWidgetManager.updateAppWidget(id, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

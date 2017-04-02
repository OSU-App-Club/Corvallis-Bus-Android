package osu.appclub.corvallisbus.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import osu.appclub.corvallisbus.MainActivity;
import osu.appclub.corvallisbus.R;

public class CorvallisBusWidgetProvider extends AppWidgetProvider {
    public static final String UPDATE_ACTION = "osu.appclub.corvallisbus.UPDATE_ACTION";

    private void setUpdateAlarm(Context context) {
        Intent updateItt = new Intent(context, this.getClass());
        updateItt.setAction(UPDATE_ACTION);
        PendingIntent updatePi = PendingIntent.getBroadcast(context, 0, updateItt, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        long time = System.currentTimeMillis();
        am.setWindow(AlarmManager.RTC, time + 25000, time + 35000, updatePi);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (UPDATE_ACTION.equals(intent.getAction())) {
            setUpdateAlarm(context);
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, this.getClass()));
            awm.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setUpdateAlarm(context);
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

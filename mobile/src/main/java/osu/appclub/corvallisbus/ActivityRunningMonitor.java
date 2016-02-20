package osu.appclub.corvallisbus;

/**
 * Created by rikkigibson on 2/20/16.
 */
public interface ActivityRunningMonitor {
    boolean isActivityRunning();
    void addActivityRunningListener(Listener listener);
    void removeActivityRunningListener(Listener listener);
    interface Listener {
        void onResume();
        void onPause();
    }
}

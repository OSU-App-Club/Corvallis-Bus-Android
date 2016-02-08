package osu.appclub.corvallisbus;

import android.os.Handler;

/**
 * Repeatedly runs a Runnable on a Handler at the given time interval.
 * When started multiple times, removes the previous callback posted.
 * This effectively resets the time interval of the refresh.
 */
public abstract class Refresher {
    private final Handler handler = new Handler();
    private final Runnable repeater;

    public Refresher(final long delayMillis) {
        repeater = new Runnable() {
            @Override
            public void run() {
                repeatedAction();
                handler.postDelayed(repeater, delayMillis);
            }
        };
    }

    abstract protected void repeatedAction();

    public void restart() {
        handler.removeCallbacks(repeater);
        repeater.run();
    }

    public void stop() {
        handler.removeCallbacks(repeater);
    }
}

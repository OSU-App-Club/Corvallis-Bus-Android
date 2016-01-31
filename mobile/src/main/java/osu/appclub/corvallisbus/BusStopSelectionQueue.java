package osu.appclub.corvallisbus;

import android.support.annotation.Nullable;

/**
 * Created by rikkigibson on 1/30/16.
 */
public interface BusStopSelectionQueue {
    void enqueueBusStop(int stopId);

    /**
     * Removes the ID of a stop to select from the queue and returns it.
     * Called to indicate that the action of selecting a stop has been completed.
     * @return The stop ID of the stop to select if one was enqueued; otherwise, null.
     */
    @Nullable
    Integer dequeueBusStopId();

    @Nullable
    Integer peekBusStopId();

    void setStopDetailsQueueListener(@Nullable Listener listener);

    interface Listener {
        void onEnqueueBusStop(BusStopSelectionQueue queue);
    }
}

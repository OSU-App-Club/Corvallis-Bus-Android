package osu.appclub.corvallisbus.apiclient;

import android.os.AsyncTask;

/**
 * Downloads JSON from a given URL, parses it into the Result type and executes the callback given
 * by an override of this class. We'll see if that's a viable pattern or not.
 */
public abstract class JSONDownloadTask<Params, Result> extends AsyncTask<Params, Void, Result> {

    @Override
    protected Result doInBackground(Params... params) {
        return null;
    }
}

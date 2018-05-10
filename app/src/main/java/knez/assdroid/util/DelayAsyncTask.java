package knez.assdroid.util;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

public class DelayAsyncTask extends AsyncTask<Void, Void, Void> {

    @NonNull
    private final WeakReference<Callback> callback;
    private final long delayMillis;

    public DelayAsyncTask(@NonNull Callback callback, long delayMillis) {
        this.callback = new WeakReference<>(callback);
        this.delayMillis = delayMillis;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ignored) {}
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Callback actualCallback = callback.get();
        if (actualCallback != null) actualCallback.onDelayCompleted();
    }

    @Override protected void onPreExecute() { }
    @Override protected void onProgressUpdate(Void... values) {}

    public interface Callback { void onDelayCompleted(); }
}

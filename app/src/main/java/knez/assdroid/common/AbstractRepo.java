package knez.assdroid.common;

import android.support.annotation.Nullable;

import java.util.List;

import knez.assdroid.util.Threader;
import timber.log.Timber;

public abstract class AbstractRepo {

    /**
     * Takes your list of callbacks and gives each callback to callbackCaller to execute it.
     * In other words, you get something like this:
     * callbacks.each({ ... do what callbackCaller says ... }
     * If there is an exception while calling a callback, it is caught and logged.
     * Callbacks are executed on given Threader or on current thread, if none is given.
     */
    protected <T> void fireCallbacks(
            List<T> callbacks, CallbackCaller<T> callbackCaller, @Nullable Threader threader) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (callbacks) {
            for (T callback : callbacks) {
                try {
                    Runnable task = () -> callbackCaller.callCallback(callback);
                    if (threader != null) threader.justExecute(task);
                    else task.run();
                } catch (Exception ex) {
                    Timber.e(ex);
                }
            }
        }
    }

    public interface CallbackCaller<T> {
        void callCallback(T callback);
    }

}

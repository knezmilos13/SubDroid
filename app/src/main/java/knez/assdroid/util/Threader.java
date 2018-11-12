package knez.assdroid.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Threader {

    @NonNull private final Handler handler;
    @Nullable private Callback callback;

    public Threader(Looper looper) {
        this.handler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                if(callback != null) callback.onTimeUp(msg.what);
            }
        };
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    /** Note: cancels all previous tasks before running the current one */
    public void cancelAndReschedule(int taskCode, long delayMillis) {
        cancelTasks(taskCode);
        handler.sendMessageDelayed(handler.obtainMessage(taskCode), delayMillis);
    }

    public void cancelTasks(int taskCode) {
        handler.removeMessages(taskCode);
    }

    public void cancelAllTasks() {
        handler.removeCallbacksAndMessages(null);
    }

    public boolean hasTask(int taskCode) {
        return handler.hasMessages(taskCode);
    }

    public void justExecute(@NonNull Runnable runnable) {
        handler.post(runnable);
    }

    public interface Callback {
        void onTimeUp(int taskCode);
    }

}

package knez.assdroid.util.logging;

import android.support.annotation.NonNull;

import timber.log.Timber;

public class DebugTree extends Timber.DebugTree {

    private final boolean showThread;

    public DebugTree(boolean showThread) {
        this.showThread = showThread;
    }

    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if(showThread && tag != null) {
            String threadName = Thread.currentThread().getName();
            tag = "<" + threadName + "> " + tag;
        }
        super.log(priority, tag, message, t);
    }

    @Override
    protected String createStackElementTag(@NonNull StackTraceElement element) {
        return super.createStackElementTag(element) + ":" + element.getLineNumber();
    }

}
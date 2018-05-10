package knez.assdroid;

import com.facebook.stetho.Stetho;

import knez.assdroid.util.logging.ActivityLifecycleLogger;
import knez.assdroid.util.logging.DebugTree;
import timber.log.Timber;

public class DebugApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

    @Override
    protected void setUpLoggingAndExceptionHandling() {
        super.setUpLoggingAndExceptionHandling();
        Timber.plant(new DebugTree(BuildConfig.DEBUG_LOG_CURRENT_THREAD));

        registerActivityLifecycleCallbacks(new ActivityLifecycleLogger(getAppComponent().getLogger()));
    }

}

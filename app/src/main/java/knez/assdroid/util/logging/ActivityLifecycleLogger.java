package knez.assdroid.util.logging;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;

import timber.log.Timber;

public class ActivityLifecycleLogger implements Application.ActivityLifecycleCallbacks {

    @NonNull private final Timber.Tree logger;

    public ActivityLifecycleLogger(@NonNull Timber.Tree logger) {
        this.logger = logger;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        logger.v("Activity %s created", activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        logger.v("Activity %s resumed", activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        logger.v("Activity %s paused", activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        logger.v("Activity %s destroyed", activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        logger.v("Activity %s stopped", activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        logger.v("Activity %s started", activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        logger.v("Activity %s saved instance state", activity);
    }

}

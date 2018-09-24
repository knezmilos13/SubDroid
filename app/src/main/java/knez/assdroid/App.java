package knez.assdroid;

import android.app.Activity;
import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;
import knez.assdroid.common.injection.AppComponent;
import knez.assdroid.common.injection.DaggerAppComponent;
import knez.assdroid.util.logging.CrashlyticsTree;
import timber.log.Timber;

public class App extends Application implements HasActivityInjector {

    private static AppComponent appComponent;

    @Inject protected DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject protected Timber.Tree logger;

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) return;

        if(BuildConfig.DEBUG && BuildConfig.STRICT_MODE) enableStrictMode();


        appComponent = buildDaggerComponent();
        appComponent.inject(this);

        AndroidThreeTen.init(this);

        // this logger is configured in the following line, but you need a reference before that
        setUpLoggingAndExceptionHandling();

        logger.v("Application class initialized");
    }

    // Overridden in test app class (which is a subclass of this app class)
    protected AppComponent buildDaggerComponent() {
        return DaggerAppComponent
                .builder()
                .appInstance(this)
                .refWatcherInstance(LeakCanary.install(this))
                .build();
    }

    protected void setUpLoggingAndExceptionHandling() {
        Fabric.with(this, new Crashlytics());

        Crashlytics.setString("GIT SHA", BuildConfig.GIT_SHA);
        Crashlytics.setLong("BUILD TIME", BuildConfig.GIT_TIMESTAMP);

        Timber.plant(new CrashlyticsTree());
    }


    // ------------------------------------------------------------------------------ PUBLIC HELPERS

    public static AppComponent getAppComponent() { return appComponent; }


    // ------------------------------------------------------------------------------------ INTERNAL

    private static void enableStrictMode() {
        StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                new StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyFlashScreen()
                        .penaltyLog();
        StrictMode.VmPolicy.Builder vmPolicyBuilder =
                new StrictMode.VmPolicy.Builder()
                        .detectAll()
                        .penaltyLog();

        threadPolicyBuilder.penaltyFlashScreen();

        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

}

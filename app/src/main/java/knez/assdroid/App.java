package knez.assdroid;

import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import knez.assdroid.common.injection.AppComponent;
import knez.assdroid.common.injection.DaggerAppComponent;
import knez.assdroid.common.util.AppConfig;
import knez.assdroid.util.logging.CrashlyticsTree;
import timber.log.Timber;

public class App extends Application {

	private static AppComponent appComponent;

	@Override
	public void onCreate() {
		super.onCreate();

		if (LeakCanary.isInAnalyzerProcess(this)) return;

		if(BuildConfig.DEBUG && BuildConfig.STRICT_MODE) enableStrictMode();


		appComponent = buildDaggerComponent();


		AndroidThreeTen.init(this);

		// this logger is configured in the following line, but you need a reference before that
		setUpLoggingAndExceptionHandling();

		appComponent.getLogger().v("Application class initialized");
	}

	// Overridden in test app class (which is a subclass of this app class)
	protected AppComponent buildDaggerComponent() {
		return DaggerAppComponent
				.builder()
				.appInstance(this)
				.appConfigInstance(new AppConfig())
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

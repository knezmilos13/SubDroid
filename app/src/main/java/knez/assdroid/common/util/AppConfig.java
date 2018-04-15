package knez.assdroid.common.util;

import android.support.annotation.Nullable;

import knez.assdroid.BuildConfig;

@SuppressWarnings("SameParameterValue") // of course it's same, it's set in gradle
public class AppConfig {

    private boolean debug;
    private boolean debugLogFragmentState;

    public AppConfig() {

        debug = BuildConfig.DEBUG;
        debugLogFragmentState = BuildConfig.DEBUG_LOG_FRAGMENT_STATE;
    }

    public boolean isDebug() { return debug; }
    public boolean shouldDebugLogFragmentState() { return debugLogFragmentState; }

}

package knez.assdroid.common.util;

import knez.assdroid.BuildConfig;

@SuppressWarnings("SameParameterValue") // of course it's same, it's set in gradle
public class AppConfig {

    private static final long TYPING_DELAY_MILLIS = 400;

    private final boolean debug;
    private final boolean debugLogFragmentState;

    public AppConfig() {
        debug = BuildConfig.DEBUG;
        debugLogFragmentState = BuildConfig.DEBUG_LOG_FRAGMENT_STATE;
    }

    public boolean isDebug() { return debug; }
    public boolean shouldDebugLogFragmentState() { return debugLogFragmentState; }
    public long getTypingDelayMillis() { return TYPING_DELAY_MILLIS; }

}

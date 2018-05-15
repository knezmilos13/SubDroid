package knez.assdroid.common.injection;

import android.content.SharedPreferences;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.Constants;
import knez.assdroid.common.SharedPreferenceKey;
import knez.assdroid.util.preferences.BooleanPreference;
import knez.assdroid.util.preferences.IntPreference;
import knez.assdroid.util.preferences.StringPreference;

@Module
class PreferencesModule {

    @Provides @Singleton @Named("tagReplacement")
    static StringPreference provideTagReplacementPreference(SharedPreferences sharedPreferences) {
        return new StringPreference(sharedPreferences, SharedPreferenceKey.TAG_REPLACEMENT,
                Constants.DEFAULT_TAG_REPLACEMENT);
    }

    @Provides @Singleton @Named("subLineShowTimings")
    static BooleanPreference provideSubLineShowTimingsPreference(SharedPreferences sharedPreferences) {
        return new BooleanPreference(sharedPreferences, SharedPreferenceKey.SUBTITLE_LINE_SHOW_TIMINGS,
                Constants.SUB_LINE_DEFAULT_SHOW_TIMINGS);
    }

    @Provides @Singleton @Named("subLineShowStyleActor")
    static BooleanPreference provideSubLineShowStyleActorPreference(SharedPreferences sharedPreferences) {
        return new BooleanPreference(sharedPreferences, SharedPreferenceKey.SUBTITLE_LINE_SHOW_STYLE_ACTOR,
                Constants.SUB_LINE_DEFAULT_SHOW_STYLE_ACTOR);
    }

    @Provides @Singleton @Named("subLineTextSize")
    static IntPreference provideSubLineTextSizePreference(SharedPreferences sharedPreferences) {
        return new IntPreference(sharedPreferences, SharedPreferenceKey.SUBTITLE_LINE_TEXT_SIZE_DP,
                Constants.SUB_LINE_DEFAULT_SUB_TEXT_SIZE_DP);
    }

    @Provides @Singleton @Named("subLineOtherSize")
    static IntPreference provideSubLineOtherSizePreference(SharedPreferences sharedPreferences) {
        return new IntPreference(sharedPreferences, SharedPreferenceKey.SUBTITLE_LINE_OTHER_SIZE_DP,
                Constants.SUB_LINE_DEFAULT_OTHER_TEXT_SIZE_DP);
    }

}

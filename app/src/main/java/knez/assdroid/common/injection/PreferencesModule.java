package knez.assdroid.common.injection;

import android.content.SharedPreferences;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.Constants;
import knez.assdroid.common.SharedPreferenceKey;
import knez.assdroid.util.preferences.StringPreference;

@Module
class PreferencesModule {

    @Provides @Singleton @Named("tagReplacement")
    static StringPreference provideTagReplacementPreference(SharedPreferences sharedPreferences) {
        return new StringPreference(sharedPreferences, SharedPreferenceKey.TAG_REPLACEMENT,
                Constants.DEFAULT_TAG_REPLACEMENT);
    }

}

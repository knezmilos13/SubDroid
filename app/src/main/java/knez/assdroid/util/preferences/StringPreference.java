package knez.assdroid.util.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class StringPreference implements PersistedValue<String> {

    @NonNull private final SharedPreferences sharedPreferences;
    @NonNull private final String key;
    @Nullable private final String defaultValue;

    public StringPreference(@NonNull SharedPreferences sharedPreferences, @NonNull String key) {
        this(sharedPreferences, key, null);
    }

    public StringPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key,
                            final @Nullable String defaultValue) {
        this.sharedPreferences = sharedPreferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override @Nullable
    public String get() {
        return this.sharedPreferences.getString(this.key, this.defaultValue);
    }

    @Override
    public boolean isSet() {
        return this.sharedPreferences.contains(this.key);
    }

    @Override
    public void set(final @NonNull String value) {
        this.sharedPreferences.edit().putString(this.key, value).apply();
    }

    @Override
    public void delete() {
        this.sharedPreferences.edit().remove(this.key).apply();
    }
}

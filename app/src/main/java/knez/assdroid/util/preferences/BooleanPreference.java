package knez.assdroid.util.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public final class BooleanPreference {
    private final SharedPreferences sharedPreferences;
    private final String key;
    private final boolean defaultValue;

    public BooleanPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key) {
        this(sharedPreferences, key, false);
    }

    public BooleanPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key,
                             final boolean defaultValue) {
        this.sharedPreferences = sharedPreferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public boolean get() {
        return this.sharedPreferences.getBoolean(this.key, this.defaultValue);
    }

    public boolean isSet() {
        return this.sharedPreferences.contains(this.key);
    }

    public void set(final boolean value) {
        this.sharedPreferences.edit().putBoolean(this.key, value).apply();
    }

    public void delete() {
        this.sharedPreferences.edit().remove(this.key).apply();
    }
}

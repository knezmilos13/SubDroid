package knez.assdroid.util.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class StringPreference {
    private final SharedPreferences sharedPreferences;
    private final String key;
    private final String defaultValue;

    public StringPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key) {
        this(sharedPreferences, key, null);
    }

    public StringPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key,
                            final @Nullable String defaultValue) {
        this.sharedPreferences = sharedPreferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String get() {
        return this.sharedPreferences.getString(this.key, this.defaultValue);
    }

    public boolean isSet() {
        return this.sharedPreferences.contains(this.key);
    }

    public void set(final @NonNull String value) {
        this.sharedPreferences.edit().putString(this.key, value).apply();
    }

    public void delete() {
        this.sharedPreferences.edit().remove(this.key).apply();
    }
}

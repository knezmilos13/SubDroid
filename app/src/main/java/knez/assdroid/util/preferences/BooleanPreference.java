package knez.assdroid.util.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public final class BooleanPreference implements PersistedValue<Boolean> {

    @NonNull private SharedPreferences sharedPreferences;
    @NonNull private String key;
    @NonNull private Boolean defaultValue;

    public BooleanPreference(@NonNull SharedPreferences sharedPreferences,
                             @NonNull String key,
                             @NonNull Boolean defaultValue) {
        this.sharedPreferences = sharedPreferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override @NonNull
    public Boolean get() {
        return this.sharedPreferences.getBoolean(this.key, this.defaultValue);
    }

    @Override
    public boolean isSet() {
        return this.sharedPreferences.contains(this.key);
    }

    @Override
    public void set(@NonNull Boolean value) {
        this.sharedPreferences.edit().putBoolean(this.key, value).apply();
    }

    @Override
    public void delete() {
        this.sharedPreferences.edit().remove(this.key).apply();
    }

}

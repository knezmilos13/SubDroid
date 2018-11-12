package knez.assdroid.util.preferences;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

public final class IntPreference implements PersistedValue<Integer> {

    @NonNull private final SharedPreferences sharedPreferences;
    @NonNull private final String key;
    @NonNull private final Integer defaultValue;

    public IntPreference(@NonNull SharedPreferences sharedPreferences,
                         @NonNull String key,
                         @NonNull Integer defaultValue) {
        this.sharedPreferences = sharedPreferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override @NonNull
    public Integer get() {
        return this.sharedPreferences.getInt(this.key, this.defaultValue);
    }

    @Override
    public boolean isSet() {
    return this.sharedPreferences.contains(this.key);
  }

    @Override
    public void set(@NonNull Integer value) {
        this.sharedPreferences.edit().putInt(this.key, value).apply();
    }

    @Override
    public void delete() {
    this.sharedPreferences.edit().remove(this.key).apply();
  }

}

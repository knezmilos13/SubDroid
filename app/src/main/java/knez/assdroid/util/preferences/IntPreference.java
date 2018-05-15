package knez.assdroid.util.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public final class IntPreference {
  private final SharedPreferences sharedPreferences;
  private final String key;
  private final int defaultValue;

  public IntPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key) {
    this(sharedPreferences, key, 0);
  }

  public IntPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key,
                       final int defaultValue) {
    this.sharedPreferences = sharedPreferences;
    this.key = key;
    this.defaultValue = defaultValue;
  }

  public int get() {
    return this.sharedPreferences.getInt(this.key, this.defaultValue);
  }

  public boolean isSet() {
    return this.sharedPreferences.contains(this.key);
  }

  public void set(final int value) {
    this.sharedPreferences.edit().putInt(this.key, value).apply();
  }

  public void delete() {
    this.sharedPreferences.edit().remove(this.key).apply();
  }
}

package knez.assdroid.common;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

public class StorageHelper {

    @NonNull private final SharedPreferences sharedPreferences;
    @NonNull private final Gson gson;

    public StorageHelper(@NonNull final SharedPreferences sharedPreferences,
                         @NonNull final Gson gson) {
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
    }


    // ------------------------------------------------------------------------------------- GENERIC

    public void putString(@NonNull String key, @Nullable String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    @Nullable public String getString(@NonNull String key, @Nullable String def) {
        return sharedPreferences.getString(key, def);
    }

    public void putInt(@NonNull String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(@NonNull String key, int def) {
        return sharedPreferences.getInt(key, def);
    }

    public void putBoolean(@NonNull String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(@NonNull String key, boolean def) {
        return sharedPreferences.getBoolean(key, def);
    }

    public void putLong(@NonNull String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(@NonNull String key, long def) {
        return sharedPreferences.getLong(key, def);
    }

    public void remove(@NonNull String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    @Nullable public <T> T readJson(@NonNull String key, @NonNull Class<T> clazz) {
        String rawJson = getString(key, null);
        if(rawJson == null) return null;
        return gson.fromJson(rawJson, clazz);
    }

    public void writeJson(@NonNull String key, @NonNull Object object) {
        String rawJson = gson.toJson(object);
        putString(key, rawJson);
    }

}

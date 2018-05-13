package knez.assdroid.common.injection;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.common.StorageHelper;

@Module
class StorageHelperModule {

    @Provides @Singleton
    SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides @Singleton
    StorageHelper getStorageHelper(SharedPreferences sharedPreferences, Gson gson) {
        return new StorageHelper(sharedPreferences, gson);
    }

}

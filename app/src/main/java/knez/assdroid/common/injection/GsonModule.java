package knez.assdroid.common.injection;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.common.gson.GsonFactory;

@Module
class GsonModule {

    @Provides @Singleton
    GsonFactory getGsonFactory() {
        return new GsonFactory();
    }

    @Provides @Singleton
    Gson getGson(GsonFactory gsonFactory) {
        return gsonFactory.getNewStandardGson().create();
    }

}

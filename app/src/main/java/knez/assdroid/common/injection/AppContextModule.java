package knez.assdroid.common.injection;

import android.content.Context;
import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.App;
import knez.assdroid.util.Threader;
import timber.log.Timber;

@Module(includes = { GsonModule.class, StorageHelperModule.class, FileHandlerModule.class })
class AppContextModule {

    @Provides @Singleton
    Context providesContext(App app) {
        return app;
    }

    @Provides @Singleton @Named("ioExecutor")
    ExecutorService getIoThreadPoolExecutor() {
        return new ThreadPoolExecutor(0, 2, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }

    @Provides @Named("singleThreadExecutor")
    ExecutorService getSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides @Named("mainThreader")
    Threader getMainThreader() { return new Threader(Looper.getMainLooper()); }

    @Provides @Singleton
    Timber.Tree getLogger() {
        return Timber.asTree();
    }

}

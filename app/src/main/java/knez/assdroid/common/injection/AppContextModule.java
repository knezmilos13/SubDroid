package knez.assdroid.common.injection;

import android.content.Context;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.App;
import knez.assdroid.common.Navigator;
import timber.log.Timber;

@Module
public class AppContextModule {

    @Provides @Singleton
    Context providesContext(App app) {
        return app;
    }

    @Provides @Singleton @Named("apiExecutor")
    ExecutorService getApiThreadPoolExecutor() {
        return new ThreadPoolExecutor(0, 2, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }

    @Provides @Singleton
    Timber.Tree getLogger() {
        return Timber.asTree();
    }

    @Provides @Singleton
    Navigator getNavigator(Context context) {
        return new Navigator(context);
    }

}

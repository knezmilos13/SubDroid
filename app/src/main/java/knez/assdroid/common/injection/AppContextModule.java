package knez.assdroid.common.injection;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Looper;

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
import knez.assdroid.common.util.AppConfig;
import knez.assdroid.editor.EditorPresenter;
import knez.assdroid.logika.SubtitleHandler;
import knez.assdroid.util.Threader;
import timber.log.Timber;

@Module
public class AppContextModule {

    @Provides @Singleton
    Context providesContext(App app) {
        return app;
    }

    @Provides @Singleton @Named("ioExecutor")
    ExecutorService getIoThreadPoolExecutor() {
        return new ThreadPoolExecutor(0, 2, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }

    @Provides @Named("mainThreader")
    Threader getMainThreader() { return new Threader(Looper.getMainLooper()); }

    @Provides @Singleton
    Timber.Tree getLogger() {
        return Timber.asTree();
    }

    // TODO: ako implementiras non-singleton scope, onda mozes da napravis ne-singleton navigator
    // koji ce kao parametar da prima Activity koji ces da obezbedis na svakom ekranu ponaosob
    @Provides @Singleton
    Navigator getNavigator(Context context) {
        return new Navigator(context);
    }

    @Provides @Singleton
    SubtitleHandler getSubtitleHandler(
            ContentResolver contentResolver,
            @Named("ioExecutor") ExecutorService executorService,
            @Named("mainThreader") Threader mainThreader, Timber.Tree logger, Context context) {
        return new SubtitleHandler(contentResolver, executorService, mainThreader, logger, context);
    }

    @Provides @Singleton
    ContentResolver getContentResolver(Context context) {
        return context.getContentResolver();
    }

    @Provides
    EditorPresenter getEditorPresenter(
            SubtitleHandler subtitleHandler, Navigator navigator, AppConfig appConfig) {
        return new EditorPresenter(
                subtitleHandler,
                navigator,
                appConfig.getTypingDelayMillis()
        );
    }

}

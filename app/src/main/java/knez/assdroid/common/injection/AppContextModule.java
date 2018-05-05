package knez.assdroid.common.injection;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.App;
import knez.assdroid.common.StorageHelper;
import knez.assdroid.common.gson.GsonFactory;
import knez.assdroid.editor.EditorMVP;
import knez.assdroid.editor.EditorPresenter;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.translator.TranslatorMVP;
import knez.assdroid.translator.TranslatorPresenter;
import knez.assdroid.util.FileHandler;
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

    @Provides @Singleton
    ContentResolver getContentResolver(Context context) {
        return context.getContentResolver();
    }

    @Provides @Singleton
    SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides @Singleton
    GsonFactory getGsonFactory() {
        return new GsonFactory();
    }

    @Provides @Singleton
    Gson getGson(GsonFactory gsonFactory) {
        return gsonFactory.getNewStandardGson().create();
    }

    @Provides @Singleton
    StorageHelper getStorageHelper(SharedPreferences sharedPreferences, Gson gson) {
        return new StorageHelper(sharedPreferences, gson);
    }

    @Provides @Singleton
    SubtitleLineVsoFactory getSubtitleLineVsoFactory() {
        return new SubtitleLineVsoFactory();
    }

    @Provides
    EditorMVP.PresenterInterface getEditorPresenter(
            SubtitleController subtitleController, SubtitleLineVsoFactory subtitleLineVsoFactory,
            StorageHelper storageHelper) {
        return new EditorPresenter(subtitleController, subtitleLineVsoFactory, storageHelper);
    }

    @Provides @Singleton
    FileHandler getFileHandler(ContentResolver contentResolver) {
        return new FileHandler(contentResolver);
    }

    @Provides
    TranslatorMVP.PresenterInterface getTranslatorPresenter(
            SubtitleController subtitleController, Timber.Tree logger) {
        return new TranslatorPresenter(subtitleController, logger);
    }

}

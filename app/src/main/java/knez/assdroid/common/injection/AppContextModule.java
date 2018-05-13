package knez.assdroid.common.injection;

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
import knez.assdroid.common.StorageHelper;
import knez.assdroid.editor.EditorMVP;
import knez.assdroid.editor.EditorPresenter;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.translator.TranslatorMVP;
import knez.assdroid.translator.TranslatorPresenter;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import knez.assdroid.util.preferences.StringPreference;
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

    @Provides @Named("mainThreader")
    Threader getMainThreader() { return new Threader(Looper.getMainLooper()); }

    @Provides @Singleton
    Timber.Tree getLogger() {
        return Timber.asTree();
    }


    // ---------------------------------------------------------------------------------- PRESENTERS

    @Provides
    EditorMVP.PresenterInterface getEditorPresenter(
            SubtitleController subtitleController, SubtitleLineVsoFactory subtitleLineVsoFactory,
            StorageHelper storageHelper, FileHandler fileHandler) {
        return new EditorPresenter(
                subtitleController, subtitleLineVsoFactory, storageHelper, fileHandler);
    }

    @Provides
    TranslatorMVP.PresenterInterface getTranslatorPresenter(
            SubtitleController subtitleController, SubtitleLine.Builder subLineBuilder,
            Timber.Tree logger, FileHandler fileHandler,
            @Named("tagReplacement") StringPreference tagReplacementPreference) {
        return new TranslatorPresenter(subtitleController, subLineBuilder, logger, fileHandler,
                tagReplacementPreference);
    }

}

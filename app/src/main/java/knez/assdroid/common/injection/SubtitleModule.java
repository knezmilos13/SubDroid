package knez.assdroid.common.injection;

import android.content.ContentResolver;

import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.SubtitleHandlerRepository;
import knez.assdroid.subtitle.ass.AssFormatter;
import knez.assdroid.subtitle.ass.AssParser;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import solid.collections.SolidList;
import timber.log.Timber;

@Module
public class SubtitleModule {

    @Provides @Singleton
    SubtitleController getSubtitleHandler(
            SubtitleHandlerRepository subtitleHandlerRepository,
            FileHandler fileHandler,
            @Named("ioExecutor") ExecutorService executorService,
            @Named("mainThreader") Threader mainThreader,
            Timber.Tree logger) {
        return new SubtitleController(
                subtitleHandlerRepository, fileHandler, executorService, mainThreader, logger);
    }

    @Provides @Singleton
    SubtitleHandlerRepository getSubtitleHandlerRepository(
            AssParser assParser, AssFormatter assFormatter) {
        return new SubtitleHandlerRepository(
                SolidList.list(assParser),
                SolidList.list(assFormatter)
        );
    }

    @Provides @Singleton
    AssParser getAssParser() {
        return new AssParser();
    }

    @Provides @Singleton
    AssFormatter getAssFormatter() {
        return new AssFormatter();
    }

}

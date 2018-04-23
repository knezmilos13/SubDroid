package knez.assdroid.common.injection;

import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.handler.SubtitleHandlerRepository;
import knez.assdroid.subtitle.handler.ass.AssFormatter;
import knez.assdroid.subtitle.handler.ass.AssParser;
import knez.assdroid.subtitle.handler.ass.SubtitleSectionParser;
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

    // TODO: ass specificne delove u ass specificni modul
    @Provides @Singleton
    AssParser getAssParser(SubtitleSectionParser subtitleSectionParser) {
        return new AssParser(subtitleSectionParser);
    }

    @Provides @Singleton
    SubtitleSectionParser getSubtitleSectionParser() {
        return new SubtitleSectionParser();
    }

    @Provides @Singleton
    AssFormatter getAssFormatter() {
        return new AssFormatter();
    }

}

package knez.assdroid.common.injection;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.common.StorageHelper;
import knez.assdroid.common.db.SubtitleContentDao;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleFormatter;
import knez.assdroid.subtitle.handler.SubtitleHandlerRepository;
import knez.assdroid.subtitle.handler.SubtitleParser;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import timber.log.Timber;

@Module(includes = SubtitleAssModule.class)
class SubtitleModule {

    @Provides @Singleton
    SubtitleController getSubtitleHandler(
            SubtitleHandlerRepository subtitleHandlerRepository,
            SubtitleContentDao subtitleContentDao,
            FileHandler fileHandler,
            StorageHelper storageHelper,
            @Named("ioExecutor") ExecutorService executorService,
            @Named("mainThreader") Threader mainThreader,
            Timber.Tree logger) {
        return new SubtitleController(subtitleHandlerRepository, fileHandler, subtitleContentDao,
                storageHelper, executorService, mainThreader, logger);
    }

    @Provides @Singleton
    SubtitleHandlerRepository getSubtitleHandlerRepository(
            Set<SubtitleParser> subtitleParsers,
            Set<SubtitleFormatter> subtitleFormatters) {
        return new SubtitleHandlerRepository(subtitleParsers, subtitleFormatters);
    }

    @Provides
    SubtitleLine.Builder getSubtitleLineBuilder() {
        return new SubtitleLine.Builder();
    }

}

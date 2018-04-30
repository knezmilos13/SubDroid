package knez.assdroid.common.injection;

import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import knez.assdroid.common.StorageHelper;
import knez.assdroid.common.db.SubtitleContentDao;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.handler.SubtitleHandlerRepository;
import knez.assdroid.subtitle.handler.ass.AssFormatter;
import knez.assdroid.subtitle.handler.ass.AssParser;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import solid.collections.SolidList;
import timber.log.Timber;

@Module(includes = SubtitleAssModule.class)
public class SubtitleModule {

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
            AssParser assParser, AssFormatter assFormatter) {
        return new SubtitleHandlerRepository(
                SolidList.list(assParser),
                SolidList.list(assFormatter)
        );
    }

}

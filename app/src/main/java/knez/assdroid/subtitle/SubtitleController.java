package knez.assdroid.subtitle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import knez.assdroid.common.AbstractRepo;
import knez.assdroid.common.db.SubtitleContentDao;
import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.handler.SubtitleContent;
import knez.assdroid.subtitle.handler.SubtitleHandlerRepository;
import knez.assdroid.subtitle.handler.SubtitleParser;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import solid.collections.Pair;
import timber.log.Timber;

import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

public class SubtitleController extends AbstractRepo {

    @NonNull private final SubtitleHandlerRepository subtitleHandlerRepository;
    @NonNull private final FileHandler fileHandler;
    @NonNull private final SubtitleContentDao subtitleContentDao;
    @NonNull private final ExecutorService executorService;
    @NonNull private final Threader mainThreader;
    @NonNull private final Timber.Tree logger;

    @NonNull private final List<Callback> callbacks = Collections.synchronizedList(new ArrayList<>());

    @Nullable private SubtitleFile currentSubtitleFile;

    public SubtitleController(@NonNull SubtitleHandlerRepository subtitleHandlerRepository,
                              @NonNull FileHandler fileHandler,
                              @NonNull SubtitleContentDao subtitleContentDao,
                              @NonNull ExecutorService executorService,
                              @NonNull Threader mainThreader,
                              @NonNull Timber.Tree logger) {
        this.subtitleHandlerRepository = subtitleHandlerRepository;
        this.fileHandler = fileHandler;
        this.subtitleContentDao = subtitleContentDao;
        this.executorService = executorService;
        this.mainThreader = mainThreader;
        this.logger = logger;
    }

    @AnyThread
    public void attachListener(Callback callback) {
        synchronized (callbacks) {
            if (callbacks.contains(callback)) return;
            callbacks.add(callback);
        }
    }

    @AnyThread
    public void detachListener(Callback callback) {
        callbacks.remove(callback);
    }

    @AnyThread
    public boolean canLoadSubtitle(@NonNull String subtitleFilename) {
        return subtitleHandlerRepository.canOpenSubtitleFile(subtitleFilename);
    }

    @AnyThread
    public void loadSubtitle(@NonNull Uri subtitlePath) {
        executorService.execute(() -> _loadSubtitle(subtitlePath));
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    @WorkerThread
    private void _loadSubtitle(@NonNull Uri subtitlePath) {
        String subtitleFilename = fileHandler.getFileNameFromUri(subtitlePath);

        SubtitleParser subtitleParser = subtitleHandlerRepository.getParserForSubtitleFile(subtitleFilename);
        if(subtitleParser == null) {
            fireCallbacks(callbacks, callback -> callback.onInvalidSubtitleFormat(subtitleFilename),
                    mainThreader);
            return;
        }

        List<String> fileContent;
        try {
            fileContent = fileHandler.readFileContent(subtitlePath);
        } catch (IOException e) {
            logger.e(e);
            fireCallbacks(callbacks, callback -> callback.onFileReadingFailed(subtitleFilename),
                    mainThreader);
            return;
        }

        Pair<SubtitleContent, List<ParsingError>> result = subtitleParser.parseSubtitle(fileContent);
        SubtitleContent subtitleContent = result.first;
        List<ParsingError> parsingErrors = result.second;

        currentSubtitleFile = new SubtitleFile(false, subtitlePath, subtitleFilename, result.first);

        subtitleContentDao.storeSubtitleContent(subtitleContent);

		fireCallbacks(callbacks,
				callback -> callback.onSubtitleFileLoaded(currentSubtitleFile, parsingErrors),
				mainThreader);
    }


    // ------------------------------------------------------------------------------------- CLASSES

    @UiThread
    public interface Callback {
        void onInvalidSubtitleFormat(@NonNull String subtitleFilename);
        void onFileReadingFailed(@NonNull String subtitleFilename);
		void onSubtitleFileLoaded(@NonNull SubtitleFile currentSubtitleFile,
								  @NonNull List<ParsingError> parsingErrors);
	}

}

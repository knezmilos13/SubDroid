package knez.assdroid.subtitle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import knez.assdroid.common.AbstractRepo;
import knez.assdroid.common.StorageHelper;
import knez.assdroid.common.db.SubtitleContentDao;
import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;
import knez.assdroid.subtitle.handler.SubtitleFormatter;
import knez.assdroid.subtitle.handler.SubtitleHandlerRepository;
import knez.assdroid.subtitle.handler.SubtitleParser;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import solid.collections.Pair;
import timber.log.Timber;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

public class SubtitleController extends AbstractRepo {

    private static final String STORAGE_KEY_SUBTITLE_STORED =
            SubtitleController.class.getCanonicalName() + ".subtitle_stored";
    private static final String STORAGE_KEY_SUBTITLE_NAME =
            SubtitleController.class.getCanonicalName() + ".subtitle_name";
    private static final String STORAGE_KEY_SUBTITLE_URI =
            SubtitleController.class.getCanonicalName() + ".subtitle_uri";
    private static final String STORAGE_KEY_SUBTITLE_EDITED =
            SubtitleController.class.getCanonicalName() + ".subtitle_edited";

    @NonNull private final SubtitleHandlerRepository subtitleHandlerRepository;
    @NonNull private final FileHandler fileHandler;
    @NonNull private final SubtitleContentDao subtitleContentDao;
    @NonNull private final StorageHelper storageHelper;
    @NonNull private final ExecutorService executorService;
    @NonNull private final Threader mainThreader;
    @NonNull private final Timber.Tree logger;

    @NonNull private final List<Callback> callbacks = Collections.synchronizedList(new ArrayList<>());

    @Nullable private SubtitleFile currentSubtitleFile;

    public SubtitleController(@NonNull SubtitleHandlerRepository subtitleHandlerRepository,
                              @NonNull FileHandler fileHandler,
                              @NonNull SubtitleContentDao subtitleContentDao,
                              @NonNull StorageHelper storageHelper,
                              @NonNull ExecutorService executorService,
                              @NonNull Threader mainThreader,
                              @NonNull Timber.Tree logger) {
        this.subtitleHandlerRepository = subtitleHandlerRepository;
        this.fileHandler = fileHandler;
        this.subtitleContentDao = subtitleContentDao;
        this.storageHelper = storageHelper;
        this.executorService = executorService;
        this.mainThreader = mainThreader;
        this.logger = logger;
    }

    public void attachListener(Callback callback) {
        synchronized (callbacks) {
            if (callbacks.contains(callback)) return;
            callbacks.add(callback);
        }
    }

    @Nullable
    public SubtitleFile getCurrentSubtitleFile() {
        return currentSubtitleFile;
    }

    public boolean hasStoredSubtitle() {
        return storageHelper.getBoolean(STORAGE_KEY_SUBTITLE_STORED, false);
    }

    public void detachListener(Callback callback) {
        callbacks.remove(callback);
    }

    public boolean canLoadExtension(@NonNull String subtitleExtension) {
        return subtitleHandlerRepository.canOpenSubtitleExtension(subtitleExtension);
    }

    public boolean canWriteSubtitle(@NonNull String subtitleExtension) {
        return subtitleHandlerRepository.canSaveToSubtitleFormat(subtitleExtension);
    }

    public void parseSubtitle(@NonNull Uri subtitlePath) {
        executorService.execute(() -> _parseSubtitle(subtitlePath));
    }

    public void reloadCurrentSubtitleFile() {
        executorService.execute(this::_reloadCurrentSubtitleFile);
    }

    public void writeSubtitle(@NonNull Uri uri) {
        executorService.execute(() -> _writeSubtitle(uri));
    }

    @NonNull
    public SubtitleFile createNewSubtitleFile() {
        SubtitleFile subtitleFile = new SubtitleFile(false, null, null,
                new SubtitleContent(new ArrayList<>(), new HashMap<>()));
        currentSubtitleFile = subtitleFile;
        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true);
        subtitleContentDao.clearSubtitle();

        return subtitleFile;
    }

    @Nullable
    public SubtitleLine getLineForId(long id) {
        if(currentSubtitleFile == null) return null;

        List<SubtitleLine> subtitleLines = currentSubtitleFile.getSubtitleContent().getSubtitleLines();
        for(SubtitleLine subtitleLine : subtitleLines)
            if(subtitleLine.getId() == id) return subtitleLine;
        return null;
    }

    @Nullable
    public SubtitleLine getLineForNumber(int number) {
        if(currentSubtitleFile == null || number < 1) return null;

        List<SubtitleLine> subtitleLines = currentSubtitleFile.getSubtitleContent().getSubtitleLines();
        int requestedIndex = number - 1; // line number starts with 1, logically speaking

        if(subtitleLines.size() <= requestedIndex) return null;
        else return subtitleLines.get(requestedIndex);
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    @WorkerThread
    private void _parseSubtitle(@NonNull Uri subtitlePath) {
        String subtitleFilename = fileHandler.getFileNameFromUri(subtitlePath);
        String subtitleExtension = subtitleFilename.substring(subtitleFilename.lastIndexOf(".")+1);

        SubtitleParser subtitleParser = subtitleHandlerRepository.getParserForSubtitleExtension(subtitleExtension);
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

        // Filename should be kept without the extension since the app itself is format-neutral
        String subtitleName = subtitleFilename.substring(0, subtitleFilename.lastIndexOf("."));

        currentSubtitleFile = new SubtitleFile(false, subtitlePath, subtitleName, result.first);

        subtitleContentDao.storeSubtitleContent(subtitleContent);

        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true);
        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_EDITED, false);
        storageHelper.putString(STORAGE_KEY_SUBTITLE_NAME, subtitleName);
        storageHelper.putString(STORAGE_KEY_SUBTITLE_URI, subtitlePath.toString());

		fireCallbacks(callbacks,
				callback -> callback.onSubtitleFileParsed(currentSubtitleFile, parsingErrors),
				mainThreader);
    }

    @WorkerThread
    private void _reloadCurrentSubtitleFile() {
        boolean subtitleStored = storageHelper.getBoolean(STORAGE_KEY_SUBTITLE_STORED, false);
        if(!subtitleStored) { // Shouldn't happen - always ask controller if subtitle is stored first
            logger.w("Performing reload subtitle file but no file stored! Creating a new file.");

            storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true);
            subtitleContentDao.clearSubtitle(); // just in case
        }

        String filename = storageHelper.getString(STORAGE_KEY_SUBTITLE_NAME, null);
        Uri uriPath = Uri.parse(storageHelper.getString(STORAGE_KEY_SUBTITLE_URI, null));
        boolean currentSubtitleEdited = storageHelper.getBoolean(STORAGE_KEY_SUBTITLE_EDITED, false);

        SubtitleContent subtitleContent = subtitleContentDao.loadSubtitleContent();
        currentSubtitleFile = new SubtitleFile(
                currentSubtitleEdited, uriPath, filename, subtitleContent);

        fireCallbacks(callbacks,
                callback -> callback.onSubtitleFileReloaded(currentSubtitleFile),
                mainThreader);
    }

    @WorkerThread
    private void _writeSubtitle(@NonNull Uri destPath) {
        if(currentSubtitleFile == null) {
            return;
            // TODO nije realisticno ali eto bas ako se desi neki fuckup
        }

        String destFilename = fileHandler.getFileNameFromUri(destPath);
        String subtitleExtension = destFilename.substring(destFilename.lastIndexOf(".") + 1);

        SubtitleFormatter subtitleFormatter =
                subtitleHandlerRepository.getFormatterForSubtitleFormat(subtitleExtension);

        if(subtitleFormatter == null) {
            fireCallbacks(callbacks, callback -> callback.onInvalidSubtitleFormat(destFilename),
                    mainThreader); // TODO ovo invalidSubtitleFormat ti je isto i za read i write, aj nekako to razdvoj malo
            return;
        }


        // TODO: ovaj bi mogao da vraca tipa serialization errore kao onaj sto vraca parsing errore
        List<String> serializedSubtitle =
                subtitleFormatter.serializeSubtitle(currentSubtitleFile.getSubtitleContent());

        try {
            fileHandler.writeFileContent(destPath, serializedSubtitle);
        } catch (IOException e) {
            logger.e(e);
            fireCallbacks(callbacks, callback -> callback.onFileWritingFailed(destFilename),
                    mainThreader);
            return;
        }

        currentSubtitleFile.setEdited(false);

        // Filename should be kept without the extension since the app itself is format-neutral
        String subtitleName = destFilename.substring(0, destFilename.lastIndexOf("."));
        currentSubtitleFile.setName(subtitleName);

        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_EDITED, false);
        storageHelper.putString(STORAGE_KEY_SUBTITLE_NAME, subtitleName);

        fireCallbacks(callbacks,
                callback -> callback.onSubtitleFileSaved(currentSubtitleFile), mainThreader);
    }


    // ------------------------------------------------------------------------------------- CLASSES

    @UiThread
    public interface Callback {
        void onInvalidSubtitleFormat(@NonNull String subtitleFilename);
        void onFileReadingFailed(@NonNull String subtitleFilename);
		void onSubtitleFileParsed(@NonNull SubtitleFile subtitleFile,
                                  @NonNull List<ParsingError> parsingErrors);
        void onSubtitleFileReloaded(@NonNull SubtitleFile subtitleFile);
        void onFileWritingFailed(@NonNull String destFilename);
        void onSubtitleFileSaved(@NonNull SubtitleFile subtitleFile);
    }

}

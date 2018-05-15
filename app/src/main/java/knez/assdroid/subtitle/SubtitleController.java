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
import knez.assdroid.subtitle.handler.TagPrettifier;
import knez.assdroid.subtitle.handler.ass.AssTagsPrettifier;
import knez.assdroid.subtitle.handler.ass.FormatConstants;
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

    /** Set only initially in order to know if there is a file in storage or not */
    private static final String STORAGE_KEY_SUBTITLE_STORED =
            SubtitleController.class.getCanonicalName() + ".subtitle_stored";
    private static final String STORAGE_KEY_SUBTITLE_NAME =
            SubtitleController.class.getCanonicalName() + ".subtitle_name";
    private static final String STORAGE_KEY_SUBTITLE_EXTENSION =
            SubtitleController.class.getCanonicalName() + ".subtitle_extension";
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
    private boolean isLoadingFile;
    private boolean isWritingFile;

    // TODO sinhronizovan pristup subtajtl fajlu... preko nekog objekta drugog posto ovja moze biti null

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

    public boolean isLoadingFile() { return isLoadingFile; }
    public boolean isWritingFile() { return isWritingFile; }

    public boolean canLoadExtension(@NonNull String subtitleExtension) {
        return subtitleHandlerRepository.canOpenSubtitleExtension(subtitleExtension);
    }

    public boolean canWriteSubtitle(@NonNull String subtitleExtension) {
        return subtitleHandlerRepository.canSaveToSubtitleFormat(subtitleExtension);
    }

    // TODO oces i ovo sa repoom kao za load/write titlova?
    public TagPrettifier getTagPrettifierForCurrentSubtitle(@NonNull String tagReplacement) {
        if(currentSubtitleFile == null) return null;
        String extension = currentSubtitleFile.getExtension();
        if(extension == null)
            return new AssTagsPrettifier(tagReplacement);
        else if(extension.equals(FormatConstants.EXTENSION_ASS))
            return new AssTagsPrettifier(tagReplacement);
        else {
            logger.e("Requested tag prettifier for an unknown subtitle format - %s", extension);
            return null;
        }
    }

    public void parseSubtitle(@NonNull Uri subtitlePath) {
        isLoadingFile = true;
        executorService.execute(() -> _parseSubtitle(subtitlePath));
    }

    public void reloadCurrentSubtitleFile() {
        executorService.execute(this::_reloadCurrentSubtitleFile);
    }

    public void writeSubtitle(@NonNull Uri uri) {
        isWritingFile = true;
        executorService.execute(() -> _writeSubtitle(uri));
    }

    public void createNewSubtitleFile() {
        currentSubtitleFile = new SubtitleFile(false, null, null, null,
                new SubtitleContent(new ArrayList<>(), new HashMap<>()));
        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true);
        subtitleContentDao.clearSubtitle();
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

    public void updateLine(@NonNull SubtitleLine updatedLine) {
        if(currentSubtitleFile == null)
            throw new IllegalStateException("No subtitle file! Can not update line!");

        SubtitleLine lineToUpdate = getLineForId(updatedLine.getId());
        if(lineToUpdate == null)
            throw new IllegalStateException("Subtitle line not found! Can not update!");

        subtitleContentDao.updateSubtitleLine(updatedLine);

        List<SubtitleLine> subtitleLines = currentSubtitleFile.getSubtitleContent().getSubtitleLines();
        subtitleLines.set(updatedLine.getLineNumber() - 1, updatedLine);

        if(!currentSubtitleFile.isEdited()) {
            storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_EDITED, true);
            currentSubtitleFile.setEdited(true);
        }

        // TODO ako se ne poklapaju i ID i line number stare i izmenjene linije, imas problem
        // TODO i oces da javis listenerima da se promenila linija?
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    @WorkerThread
    private void _parseSubtitle(@NonNull Uri subtitlePath) {
        String subtitleFilename = fileHandler.getFileNameFromUri(subtitlePath);
        String subtitleExtension = subtitleFilename.substring(subtitleFilename.lastIndexOf(".")+1);

        SubtitleParser subtitleParser = subtitleHandlerRepository.getParserForSubtitleExtension(subtitleExtension);
        if(subtitleParser == null) {
            isLoadingFile = false;
            fireCallbacks(callbacks, callback -> callback.onInvalidSubtitleFormatForLoading(subtitleFilename),
                    mainThreader);
            return;
        }

        List<String> fileContent;
        try {
            fileContent = fileHandler.readFileContent(subtitlePath);
        } catch (IOException e) {
            isLoadingFile = false;
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

        currentSubtitleFile = new SubtitleFile(
                false, subtitlePath, subtitleName, subtitleExtension, result.first);

        subtitleContentDao.storeSubtitleContent(subtitleContent);

        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true);
        storeSubtitleFileValues(currentSubtitleFile);

        isLoadingFile = false;
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

        String name = storageHelper.getString(STORAGE_KEY_SUBTITLE_NAME, null);
        String extension = storageHelper.getString(STORAGE_KEY_SUBTITLE_EXTENSION, null);
        String uriString = storageHelper.getString(STORAGE_KEY_SUBTITLE_URI, null);
        Uri uriPath = uriString == null? null : Uri.parse(uriString);
        boolean currentSubtitleEdited = storageHelper.getBoolean(STORAGE_KEY_SUBTITLE_EDITED, false);

        SubtitleContent subtitleContent = subtitleContentDao.loadSubtitleContent();
        currentSubtitleFile = new SubtitleFile(
                currentSubtitleEdited, uriPath, name, extension, subtitleContent);

        fireCallbacks(callbacks,
                callback -> callback.onSubtitleFileReloaded(currentSubtitleFile),
                mainThreader);
    }

    @WorkerThread
    private void _writeSubtitle(@NonNull Uri destPath) {
        if(currentSubtitleFile == null) {
            isWritingFile = false;
            return;
            // TODO nije realisticno ali eto bas ako se desi neki fuckup
        }

        // TODO: ovaj baca java.lang.SecurityException... napravi metodu tipa "proveri permisije" koje ce da frljne to
        String destFilename = fileHandler.getFileNameFromUri(destPath);
        String destExtension = destFilename.substring(destFilename.lastIndexOf(".") + 1);

        SubtitleFormatter subtitleFormatter =
                subtitleHandlerRepository.getFormatterForSubtitleFormat(destExtension);

        if(subtitleFormatter == null) {
            isWritingFile = false;
            fireCallbacks(callbacks, callback -> callback.onInvalidSubtitleFormatForWriting(destFilename),
                    mainThreader); // TODO ovo invalidSubtitleFormat ti je isto i za read i write, aj nekako to razdvoj malo
            return;
        }


        // TODO: ovaj bi mogao da vraca tipa serialization errore kao onaj sto vraca parsing errore
        List<String> serializedSubtitle =
                subtitleFormatter.serializeSubtitle(currentSubtitleFile.getSubtitleContent());

        try {
            fileHandler.writeFileContent(destPath, serializedSubtitle);
        } catch (IOException e) {
            isWritingFile = false;
            logger.e(e);
            fireCallbacks(callbacks, callback -> callback.onFileWritingFailed(destFilename),
                    mainThreader);
            return;
        }

        String subtitleName = destFilename.substring(0, destFilename.lastIndexOf("."));
        currentSubtitleFile = new SubtitleFile(false, destPath, subtitleName, destExtension,
                currentSubtitleFile.getSubtitleContent());

        storeSubtitleFileValues(currentSubtitleFile);

        isWritingFile = false;
        fireCallbacks(callbacks,
                callback -> callback.onSubtitleFileSaved(currentSubtitleFile), mainThreader);
    }

    private void storeSubtitleFileValues(@NonNull SubtitleFile subtitleFile) {
        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_EDITED, subtitleFile.isEdited());
        storageHelper.putString(STORAGE_KEY_SUBTITLE_NAME, subtitleFile.getName());
        storageHelper.putString(STORAGE_KEY_SUBTITLE_EXTENSION, subtitleFile.getExtension());
        storageHelper.putString(STORAGE_KEY_SUBTITLE_URI,
                subtitleFile.getUriPath() == null? null : subtitleFile.getUriPath().toString());
    }


    // ------------------------------------------------------------------------------------- CLASSES

    @UiThread
    public interface Callback {
        default void onInvalidSubtitleFormatForWriting(@NonNull String subtitleFilename) { }
        default void onInvalidSubtitleFormatForLoading(@NonNull String subtitleFilename) { }
        default void onFileReadingFailed(@NonNull String subtitleFilename) { }
        default void onSubtitleFileParsed(@NonNull SubtitleFile subtitleFile,
                                          @NonNull List<ParsingError> parsingErrors) { }
        default void onSubtitleFileReloaded(@NonNull SubtitleFile subtitleFile) { }
        default void onFileWritingFailed(@NonNull String destFilename) { }
        default void onSubtitleFileSaved(@NonNull SubtitleFile subtitleFile) { }
    }

}

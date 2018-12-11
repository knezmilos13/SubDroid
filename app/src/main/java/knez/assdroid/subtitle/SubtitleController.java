package knez.assdroid.subtitle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
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
import knez.assdroid.util.apache.FilenameUtils;
import solid.collections.Pair;
import timber.log.Timber;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

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

    @NonNull private SubtitleFile currentSubtitleFile; // TODO da li ovo moze da se zameni observableom? Da nema pristupa ovako?
    @NonNull private final Object currentSubtitleFileLock = new Object();

    private Subject<SubtitleAction> subtitleFileActionInput;
    private ConnectableObservable<SubtitleEvent> subtitleFileEventObservable;

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

        currentSubtitleFile = new SubtitleFile();
    }

    // TODO sta bude ako ga iseces novim pozivom


    // ------------------------------------------------------------------ GLOBAL SUBTITLE FILE STATE

    @NonNull
    public Observable<SubtitleEvent> getSubtitleObservable() {
        // TODO synchronized
        if(subtitleFileEventObservable == null) initializeSubtitleFileObservables();
        return subtitleFileEventObservable;
    }

    private void initializeSubtitleFileObservables() {
        subtitleFileActionInput = PublishSubject.<SubtitleAction>create().toSerialized();

        subtitleFileEventObservable = subtitleFileActionInput
                .switchMap(subtitleAction -> {
                    if (subtitleAction.subtitleActionType.equals(SubtitleActionType.NEW_SUBTITLE))
                        return createNewSubtitleEventObservable();
                    else if(subtitleAction.subtitleActionType.equals(SubtitleActionType.RELOAD_SUBTITLE))
                        return reloadSubtitleEventObservable();
                    else if(subtitleAction.subtitleActionType.equals(SubtitleActionType.LOAD_SUBTITLE)
                            && subtitleAction.data instanceof Uri)
                        return loadSubtitleEventObservable((Uri) subtitleAction.data);
                    else
                        throw new RuntimeException("Invalid SubtitleAction: " + subtitleAction.toString());
                    })
                .startWith(new BasicSubtitleEvent(currentSubtitleFile, SubtitleEventType.LOADING))
                .replay(1);

        // TODO: a gde snimas subtitle file trenutni?

        subtitleFileEventObservable.connect();

        subtitleFileActionInput.onNext(hasStoredSubtitle()?
                new SubtitleAction(SubtitleActionType.RELOAD_SUBTITLE)
                : new SubtitleAction(SubtitleActionType.NEW_SUBTITLE));
    }

    public void createNewSubtitle() {
        subtitleFileActionInput.onNext(new SubtitleAction(SubtitleActionType.NEW_SUBTITLE));
    }

    public void loadSubtitleFile(@NonNull Uri subtitlePath) {
        subtitleFileActionInput.onNext(
                new SubtitleAction(SubtitleActionType.LOAD_SUBTITLE, subtitlePath));
    }

    private boolean hasStoredSubtitle() {
        return storageHelper.getBoolean(STORAGE_KEY_SUBTITLE_STORED, false);
    }

    @NonNull
    private Observable<? extends SubtitleEvent> createNewSubtitleEventObservable() {
        return fullLoadObservable(createNewSubtitleObservable());
    }

    @NonNull
    private Observable<SubtitleFile> createNewSubtitleObservable() {
        return Observable.fromCallable(() -> {
            Thread.sleep(5000); // TODO temp
            SubtitleFile subtitleFile = new SubtitleFile(false, null, null, null,
                    new SubtitleContent(new ArrayList<>(), new HashMap<>()), true, true);
            storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true); // TODO pristupas spolja stvarima?
            subtitleContentDao.clearSubtitle();
            return subtitleFile;
        });
    }

    @NonNull
    private ObservableSource<? extends SubtitleEvent> reloadSubtitleEventObservable() {
        return fullLoadObservable(reloadSubtitleObservable());
    }

    @NonNull
    private Observable<SubtitleFile> reloadSubtitleObservable() {
        return Observable.fromCallable(() -> {
            Thread.sleep(5000); // TODO temp

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
            return new SubtitleFile(
                    currentSubtitleEdited, uriPath, name, extension, subtitleContent, true, true);
        });
    }

    @NonNull
    private ObservableSource<SubtitleEvent> loadSubtitleEventObservable(@NonNull Uri subtitlePath) {
        return loadSubtitleObservable(subtitlePath)
                .startWith(new BasicSubtitleEvent(currentSubtitleFile, SubtitleEventType.LOADING))
                .subscribeOn(Schedulers.io());
    }

    @WorkerThread
    private Observable<SubtitleEvent> loadSubtitleObservable(@NonNull Uri subtitlePath) {
        return Observable.fromCallable(() -> {

            String subtitleFilename = fileHandler.getFileNameFromUri(subtitlePath);
            String subtitleExtension = FilenameUtils.getExtension(subtitleFilename);

            // TODO: super je sve ovo sto si smislio da vratis gresku kroz drugu podklasu eventa
            // aaaaaaaaaaaliiii ideja je bila i da tako mozes da imas zadnju vrednost eventa uvek
            // snimljenu kroz observable, i sta ako ti vrati ovaj slogirani event? I posle svi koji se
            // zakace dobiju tipa event "loading failed"

            SubtitleParser subtitleParser =
                    subtitleHandlerRepository.getParserForSubtitleExtension(subtitleExtension);
            if(subtitleParser == null)
                return new LoadingFailedEvent(subtitleFilename, true, false);

            List<String> fileContent;
            try {
                fileContent = fileHandler.readFileContent(subtitlePath);
            } catch (IOException e) {
                logger.e(e);
                return new LoadingFailedEvent(subtitleFilename, false, true);
            }

            Pair<SubtitleContent, List<ParsingError>> result = subtitleParser.parseSubtitle(fileContent);
            SubtitleContent subtitleContent = result.first;
            List<ParsingError> parsingErrors = result.second;

            // Filename should be kept without the extension since the app itself is format-neutral
            String subtitleName = FilenameUtils.getName(subtitleFilename);

            SubtitleFile subtitleFile = new SubtitleFile(
                    false, subtitlePath, subtitleName, subtitleExtension, result.first, true, true);

            subtitleContentDao.storeSubtitleContent(subtitleContent);

            storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true);
            storeSubtitleFileValues(subtitleFile);

            return new BasicSubtitleEvent(subtitleFile, SubtitleEventType.LOAD);
        });
    }

    @NonNull
    private Observable<? extends SubtitleEvent> fullLoadObservable(Observable<SubtitleFile> worker) {
        return worker
                .map(subtitleFile -> new BasicSubtitleEvent(subtitleFile, SubtitleEventType.LOAD))
                .startWith(new BasicSubtitleEvent(currentSubtitleFile, SubtitleEventType.LOADING))
                .subscribeOn(Schedulers.io());
    }


















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

    public void writeSubtitle(@NonNull Uri uri) {
//        isWritingFile = true;
        executorService.execute(() -> _writeSubtitle(uri));
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
//            currentSubtitleFile.setEdited(true);
        }

        // TODO ako se ne poklapaju i ID i line number stare i izmenjene linije, imas problem
        // TODO i oces da javis listenerima da se promenila linija?
    }


    // ------------------------------------------------------------------------------------ INTERNAL



    @WorkerThread
    private void _writeSubtitle(@NonNull Uri destPath) {
        if(currentSubtitleFile == null) {
//            isWritingFile = false;
            return;
            // TODO nije realisticno ali eto bas ako se desi neki fuckup
        }

        // TODO: ovaj baca java.lang.SecurityException... napravi metodu tipa "proveri permisije" koje ce da frljne to
        String destFilename = fileHandler.getFileNameFromUri(destPath);
        String destExtension = FilenameUtils.getExtension(destFilename);

        if(destExtension.trim().equals("")) destExtension = currentSubtitleFile.getExtension();

        SubtitleFormatter subtitleFormatter = null;
        if(destExtension != null) subtitleFormatter =
                subtitleHandlerRepository.getFormatterForSubtitleFormat(destExtension);

        if(subtitleFormatter == null) {
//            isWritingFile = false;
//            fireCallbacks(callbacks, callback -> callback.onInvalidSubtitleFormatForWriting(destFilename),
//                    mainThreader); // TODO ovo invalidSubtitleFormat ti je isto i za read i write, aj nekako to razdvoj malo
            return;
        }


        // TODO: ovaj bi mogao da vraca tipa serialization errore kao onaj sto vraca parsing errore
        List<String> serializedSubtitle =
                subtitleFormatter.serializeSubtitle(currentSubtitleFile.getSubtitleContent());

        try {
            fileHandler.writeFileContent(destPath, serializedSubtitle);
        } catch (IOException e) {
//            isWritingFile = false;
            logger.e(e);
//            fireCallbacks(callbacks, callback -> callback.onFileWritingFailed(destFilename),
//                    mainThreader);
            return;
        }

        String subtitleName = FilenameUtils.getName(destFilename);
        currentSubtitleFile = new SubtitleFile(false, destPath, subtitleName, destExtension,
                currentSubtitleFile.getSubtitleContent(), true, true);

        storeSubtitleFileValues(currentSubtitleFile);

//        isWritingFile = false;
//        fireCallbacks(callbacks,
//                callback -> callback.onSubtitleFileSaved(currentSubtitleFile), mainThreader);
    }

    private void storeSubtitleFileValues(@NonNull SubtitleFile subtitleFile) {
        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_EDITED, subtitleFile.isEdited());
        storageHelper.putString(STORAGE_KEY_SUBTITLE_NAME, subtitleFile.getName());
        storageHelper.putString(STORAGE_KEY_SUBTITLE_EXTENSION, subtitleFile.getExtension());
        storageHelper.putString(STORAGE_KEY_SUBTITLE_URI,
                subtitleFile.getUriPath() == null? null : subtitleFile.getUriPath().toString());
    }


    // ------------------------------------------------------------------------------------- CLASSES

    enum SubtitleActionType { NEW_SUBTITLE, RELOAD_SUBTITLE, LOAD_SUBTITLE }

    private static class SubtitleAction {
        @NonNull final SubtitleActionType subtitleActionType;
        @Nullable final Object data;
        private SubtitleAction(SubtitleActionType subtitleActionType, Object data) {
            this.subtitleActionType = subtitleActionType;
            this.data = data;
        }
        private SubtitleAction(SubtitleActionType subtitleActionType) {
            this(subtitleActionType, null);
        }
        @NonNull @Override
        public String toString() {
            return "SubtitleActionType: " + subtitleActionType.name() + ", data: " + data.toString();
        }
    }

    public enum SubtitleEventType {
        /** The subtitle file has been loaded/reloaded and all of the data has changed. */
        LOAD, LOADING // TODO da napravis sve klasama?
    }

    public interface SubtitleEvent { };

    public static class BasicSubtitleEvent implements SubtitleEvent {
        public final SubtitleFile subtitleFile; // todo dal je nullable?
        @NonNull public final SubtitleEventType subtitleEventType;

        public BasicSubtitleEvent(SubtitleFile subtitleFile, SubtitleEventType subtitleEventType) {
            this.subtitleFile = subtitleFile;
            this.subtitleEventType = subtitleEventType;
        }
    }

    public static class LoadingFailedEvent implements SubtitleEvent {
        @NonNull public final String subtitleFileName;
        public final boolean isInvalidFileFormat;
        public final boolean hadParsingError;
        public LoadingFailedEvent(@NonNull String subtitleFileName, boolean isInvalidFileFormat, boolean hadParsingError) {
            this.subtitleFileName = subtitleFileName;
            this.isInvalidFileFormat = isInvalidFileFormat; // todo nemoj ovako ruzno sa dva booleana
            this.hadParsingError = hadParsingError;
        }
    }

    @UiThread
    public interface Callback {
        default void onInvalidSubtitleFormatForWriting(@NonNull String subtitleFilename) { }
        default void onSubtitleFileParsed(@NonNull SubtitleFile subtitleFile,
                                          @NonNull List<ParsingError> parsingErrors) { }
        default void onFileWritingFailed(@NonNull String destFilename) { }
        default void onSubtitleFileSaved(@NonNull SubtitleFile subtitleFile) { }
    }

}

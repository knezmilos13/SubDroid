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

    enum SubtitleAction { NEW_SUBTITLE, RELOAD_SUBTITLE}

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
    private ConnectableObservable<SubtitleEvent> subtitleEventObservable;

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


    // ------------------------------------------------------------------ GLOBAL SUBTITLE FILE STATE

    @NonNull
    public Observable<SubtitleEvent> getSubtitleObservable() {
        // TODO synchronized
        if(subtitleEventObservable == null) initializeSubtitleFileObservables();
        return subtitleEventObservable;
    }

    private void initializeSubtitleFileObservables() {
        subtitleFileActionInput = PublishSubject.<SubtitleAction>create().toSerialized();

        subtitleEventObservable = subtitleFileActionInput
                .switchMap((Function<SubtitleAction, ObservableSource<SubtitleEvent>>) subtitleAction -> {
                    if (subtitleAction.equals(SubtitleAction.NEW_SUBTITLE))
                        return createNewSubtitleAction();
                    else
                        return reloadSubtitleAction();
                    })
                .concatWith(Observable.just(
                        new SubtitleEvent(currentSubtitleFile, SubtitleEventType.LOADING)))
                .replay(1);

        subtitleEventObservable.connect();

        subtitleFileActionInput.onNext(hasStoredSubtitle()?
                SubtitleAction.RELOAD_SUBTITLE : SubtitleAction.NEW_SUBTITLE);
    }

    public void createNewSubtitle() {
        subtitleFileActionInput.onNext(SubtitleAction.NEW_SUBTITLE);
    }

    private boolean hasStoredSubtitle() {
        return storageHelper.getBoolean(STORAGE_KEY_SUBTITLE_STORED, false);
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

    public void parseSubtitle(@NonNull Uri subtitlePath) {
//        isLoadingFile = true;
        executorService.execute(() -> _parseSubtitle(subtitlePath));
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

    // TODO ovo realno ne valja, sta ako mi uleti neko drugi i ugasi loading? ili pokrene duplo?
    // mozda da je ovo odvojen observable? Ili da ima otkazivanja observablea?
    // Mozda bi switchMap mogao da se koristi? Posto on radi neko satro otkazivanje?
    // tipa da izmedju observablea i funnela ima switch map tako da svaki novi task
    // mzoe da svicuje stari. A da se subtajtl fajl snima tek nakon switch mapa tako da jednom
    // kad svicujes, onaj stari task i da se odradi ne utice na stanje

    // Npr: observable koji generise INTENT, ACTION ili tako nesto
    // i onda na njega zakacis switch map koji svicuje drugi observable na osnovu toga
    // (taj drugi dobija npr pozivom getCreateNewObservable, sto vraca ovaj kod ispod)
    // i onda na to zakacis funnel (koji ti i ne treba ovakav kakav je jer ces stalno da svicujes)
    // u funnelu imas snimac trenutnog stanja tek sinhronizovan

    // Ili Schedulers.single() pa da su svi poslovi na istom threadu todo
    // Sto je sasvim ok, ako ne mozes da cancelujes a bar da obezbedis redosled izvrsavanja pa nek cekaju
    // a u kombinaciji sa switchom i ne moraju da cekaju... tj. moraju, cak i da swicujes, ako su
    // i svicovani i novi na istom threadu, dzabe, cekaces onaj da se zavrsi, zar ne?

    @NonNull
    private Observable<SubtitleEvent> createNewSubtitleAction() {
        Observable<SubtitleEvent> work = Observable.fromCallable(() -> {
            Thread.sleep(5000); // TODO temp
            SubtitleFile subtitleFile = new SubtitleFile(false, null, null, null,
                    new SubtitleContent(new ArrayList<>(), new HashMap<>()), true, true);
            storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true); // TODO pristupas spolja stvarima?
            subtitleContentDao.clearSubtitle();
            return subtitleFile;
        }).map(subtitleFile -> new SubtitleEvent(subtitleFile, SubtitleEventType.FULL_LOAD));

        return Observable
                .just(new SubtitleEvent(currentSubtitleFile, SubtitleEventType.LOADING))
                .mergeWith(work)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    private ObservableSource<SubtitleEvent> reloadSubtitleAction() {
        Observable<SubtitleEvent> work = Observable.fromCallable(() -> {
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

        }).map(subtitleFile -> new SubtitleEvent(currentSubtitleFile, SubtitleEventType.FULL_LOAD));

        return Observable
                .just(new SubtitleEvent(currentSubtitleFile, SubtitleEventType.LOADING))
                .mergeWith(work)
                .subscribeOn(Schedulers.io());
    }







    @WorkerThread
    private void _parseSubtitle(@NonNull Uri subtitlePath) {
        String subtitleFilename = fileHandler.getFileNameFromUri(subtitlePath);
        String subtitleExtension = FilenameUtils.getExtension(subtitleFilename);

        SubtitleParser subtitleParser = subtitleHandlerRepository.getParserForSubtitleExtension(subtitleExtension);
        if(subtitleParser == null) {
//            isLoadingFile = false;
//            fireCallbacks(callbacks, callback -> callback.onInvalidSubtitleFormatForLoading(subtitleFilename),
//                    mainThreader);
            return;
        }

        List<String> fileContent;
        try {
            fileContent = fileHandler.readFileContent(subtitlePath);
        } catch (IOException e) {
//            isLoadingFile = false;
            logger.e(e);
//            fireCallbacks(callbacks, callback -> callback.onFileReadingFailed(subtitleFilename),
//                    mainThreader);
            return;
        }

        Pair<SubtitleContent, List<ParsingError>> result = subtitleParser.parseSubtitle(fileContent);
        SubtitleContent subtitleContent = result.first;
        List<ParsingError> parsingErrors = result.second;

        // Filename should be kept without the extension since the app itself is format-neutral
        String subtitleName = FilenameUtils.getName(subtitleFilename);

        currentSubtitleFile = new SubtitleFile(
                false, subtitlePath, subtitleName, subtitleExtension, result.first, true, true);

        subtitleContentDao.storeSubtitleContent(subtitleContent);

        storageHelper.putBoolean(STORAGE_KEY_SUBTITLE_STORED, true);
        storeSubtitleFileValues(currentSubtitleFile);

//        isLoadingFile = false;
//        fireCallbacks(callbacks,
//				callback -> callback.onSubtitleFileParsed(currentSubtitleFile, parsingErrors),
//				mainThreader);
    }

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

    public enum SubtitleEventType {
        /** The subtitle file has been loaded/reloaded and all of the data has changed. */
        FULL_LOAD, LOADING
    }

    public static class SubtitleEvent {

        public final SubtitleFile subtitleFile; // todo dal je nullable?
        @NonNull public final SubtitleEventType subtitleEventType;

        public SubtitleEvent(SubtitleFile subtitleFile, SubtitleEventType subtitleEventType) {
            this.subtitleFile = subtitleFile;
            this.subtitleEventType = subtitleEventType;
        }
    }

    @UiThread
    public interface Callback {
        // TODO kako ces da javljas ove errore?
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

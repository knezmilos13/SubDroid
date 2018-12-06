package knez.assdroid.editor;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import knez.assdroid.common.SharedPreferenceKey;
import knez.assdroid.common.mvp.CommonSubtitleMvp;
import knez.assdroid.common.mvp.CommonSubtitlePresenter;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.SubtitleContent;
import knez.assdroid.subtitle.handler.TagPrettifier;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import knez.assdroid.util.apache.FilenameUtils;
import knez.assdroid.util.preferences.PersistedValue;
import knez.assdroid.util.preferences.PersistedValueReader;
import timber.log.Timber;

import static knez.assdroid.subtitle.SubtitleController.*;

public class EditorPresenter extends CommonSubtitlePresenter
        implements EditorMvp.PresenterInterface {

    @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
    @NonNull private final ExecutorService singleThreadExecutor;
    @NonNull private final Threader mainThreader;
    @NonNull private final Timber.Tree logger;

    @NonNull private final PersistedValueReader<String> tagReplacementPreference;
    @NonNull private final PersistedValueReader<Integer> subLineTextSizePreference;
    @NonNull private final PersistedValueReader<Integer> subLineOtherSizePreference;
    @NonNull private final PersistedValue<Boolean> subLineShowTimingsPreference;
    @NonNull private final PersistedValue<Boolean> subLineShowActorStylePreference;
    @NonNull private final PersistedValue<Boolean> simplifyTagsPreference;

    @Nullable private EditorMvp.ViewInterface viewInterface;
    private boolean presenterInitialized = false;

    @NonNull private final Object vsoSyncObject = new Object();

    private Future<?> vsosCreationFuture;
    private Future<?> vsosPartialCreationFuture;
    private Future<?> vsosTagReplacementChangeFuture;
    private Future<?> searchCounterFuture;

    @NonNull private final List<SubtitleLineVso> subtitleLineVsos = new ArrayList<>();
    @NonNull private final SubtitleLineVso.SharedSettings sharedSubtitleLineSettings;

    // Note: null = no search (search view not shown); empty string = empty search (search view shown)
    @Nullable private String currentSearchQuery = null;
    @Nullable private SubtitleLineVso activeSearchResultVso = null;
    @NonNull private ArrayList<SubtitleLineVso> searchResults = new ArrayList<>();
    private int activeSearchResultIndex = -1;

    EditorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLineVsoFactory subtitleLineVsoFactory,
            @NonNull FileHandler fileHandler,
            @NonNull ExecutorService singleThreadExecutor,
            @NonNull Threader mainThreader,
            @NonNull Timber.Tree logger,
            @NonNull PersistedValueReader<String> tagReplacementPreference,
            @NonNull PersistedValueReader<Integer> subLineTextSizePreference,
            @NonNull PersistedValueReader<Integer> subLineOtherSizePreference,
            @NonNull PersistedValue<Boolean> subLineShowTimingsPreference,
            @NonNull PersistedValue<Boolean> subLineShowActorStylePreference,
            @NonNull PersistedValue<Boolean> simplifyTagsPreference) {
        super(subtitleController, fileHandler);
        this.subtitleLineVsoFactory = subtitleLineVsoFactory;
        this.singleThreadExecutor = singleThreadExecutor;
        this.mainThreader = mainThreader;
        this.logger = logger;

        this.tagReplacementPreference = tagReplacementPreference;
        this.subLineTextSizePreference = subLineTextSizePreference;
        this.subLineOtherSizePreference = subLineOtherSizePreference;
        this.subLineShowTimingsPreference = subLineShowTimingsPreference;
        this.subLineShowActorStylePreference = subLineShowActorStylePreference;
        this.simplifyTagsPreference = simplifyTagsPreference;

        this.sharedSubtitleLineSettings = new SubtitleLineVso.SharedSettings(
                subLineTextSizePreference.get(),
                subLineOtherSizePreference.get(),
                subLineShowTimingsPreference.get(),
                subLineShowActorStylePreference.get(),
                currentSearchQuery);
    }


    // ---------------------------------------------------------------------------- SETUP & TEARDOWN

    @Override
    public void onAttach(@NonNull EditorMvp.ViewInterface viewInterface) {
        this.viewInterface = viewInterface;

        Observable<SubtitleEvent> subtitleObservable = subtitleController.getSubtitleObservable();

        subtitleObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subtitleEventObserver);

        // TODO if reattach, ne moras da trazis load, al' ako nije, trazi onda.

        // TODO disposable?


        // if reattaching to the same presenter (e.g. after orientation change)
//        if(presenterInitialized) { // TODO
//            showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
//            viewInterface.showSubtitleLines(new ArrayList<>(subtitleLineVsos));
//
//            if(currentSearchQuery != null) {
//                viewInterface.showSearchSectionWithQuery(currentSearchQuery);
//                updateActiveSearchResult(activeSearchResultIndex, true);
//            }
//
//            if(subtitleController.isLoadingFile()) viewInterface.showProgressLoadingFile();
//            else if(subtitleController.isWritingFile()) viewInterface.showProgressSavingFile();
//
//            return;
//        }

        presenterInitialized = true;

        // TODO ovo neki observable kraci na relaciji view-prezenter? Ili svaki pref. da je observable?
        viewInterface.showCurrentQuickSettings(
                subLineShowTimingsPreference.get(),
                subLineShowActorStylePreference.get(),
                simplifyTagsPreference.get());
    }

    @Override
    public void onDetach() {
        viewInterface = null;
    }


    // ------------------------------------------------------------------------- PRESENTER INTERFACE

    @Override
    public void onSearchSubmitted(@NonNull String text) {
        if(Objects.equals(text, currentSearchQuery)) return;

        updateSearchQuery(text);
        asyncUpdateSearchCounter(true);
    }

    @Override
    public void onEndSearchRequested() {
        if(searchCounterFuture != null) searchCounterFuture.cancel(true);

        updateSearchQuery(null);
        updateActiveSearchResult(-1, false);

        if(viewInterface != null) viewInterface.closeSearchSection();
    }

    @Override
    public void onPrevSearchResultRequested() {
        if(activeSearchResultIndex == -1 || searchResults.isEmpty() || activeSearchResultVso == null) return;
        if(searchResults.size() == 1) return;

        updateActiveSearchResult(activeSearchResultIndex == 0?
                searchResults.size() - 1 : activeSearchResultIndex-1, true);
    }

    // TODO ovo next/prev dira stvari koje mozda cackas sa drugog threada

    @Override
    public void onNextSearchResultRequested() {
        if(activeSearchResultIndex == -1 || searchResults.isEmpty() || activeSearchResultVso == null) return;
        if(searchResults.size() == 1) return;

        updateActiveSearchResult(activeSearchResultIndex == searchResults.size() - 1?
                activeSearchResultIndex = 0 : activeSearchResultIndex+1, true);
    }

    @Override
    public void onAboutScreenRequested() {
        if(viewInterface != null) viewInterface.showAboutScreen();
    }

    @Override
    public void onFileSelectedForLoad(@NonNull Uri uri) {
        String filename = fileHandler.getFileNameFromUri(uri);
        String subtitleExtension = FilenameUtils.getExtension(filename);

        if(!subtitleController.canLoadExtension(subtitleExtension)) {
            viewInterface.showErrorLoadingSubtitleInvalidFormat(filename);
            return;
        }

        // to allow saving into same file (if needed)
        fileHandler.takePermissionForUri(uri);

        viewInterface.showProgressLoadingFile();
        subtitleController.parseSubtitle(uri);
    }

    @Override
    public void onSubtitleLineClicked(long id) {
//        if(viewInterface == null || subtitleController.isLoadingFile()) return;
//        viewInterface.showTranslatorScreen(id);
    }

    @Override
    public void onSubtitleEditedExternally(@NonNull ArrayList<Long> editedLineIds) {
//        showSubtitleTitle(subtitleController.getCurrentSubtitleFile()); // to update the "*"
//
//        // TODO: preuzmi sve
//        List<SubtitleLine> editedLines = new ArrayList<>();
//        for(Long id : editedLineIds) {
//            SubtitleLine line = subtitleController.getLineForId(id);
//            if(line == null) continue; // defensive wtf // todo loguj
//            editedLines.add(line);
//        }
//
//        clearActiveSearchResult();
//
//        asyncRecreateSomeVsos(editedLines);
//        asyncUpdateSearchCounter(false);
    }

    @Override
    public void onNewSubtitleRequested() {
        subtitleController.createNewSubtitle();
    }

    @Override
    public void onShowTimingsSettingChanged(boolean isChecked) {
        subLineShowTimingsPreference.set(isChecked);
        sharedSubtitleLineSettings.setShowTimings(isChecked);
        if(viewInterface != null) viewInterface.updateSubtitleLines();
    }

    @Override
    public void onShowActorStyleSettingChanged(boolean isChecked) {
        subLineShowActorStylePreference.set(isChecked);
        sharedSubtitleLineSettings.setShowActorAndStyle(isChecked);
        if(viewInterface != null) viewInterface.updateSubtitleLines();
    }

    @Override
    public void onSimplifyTagsSettingChanged(boolean isChecked) {
        simplifyTagsPreference.set(isChecked);
        asyncChangeTagReplacement();
    }

    @Override
    public void onSettingsChanged(@NonNull HashSet<String> changedSettings) {
        super.onSettingsChanged(changedSettings);

        if(changedSettings.contains(SharedPreferenceKey.SUBTITLE_LINE_TEXT_SIZE_DP)
                || changedSettings.contains(SharedPreferenceKey.SUBTITLE_LINE_OTHER_SIZE_DP)) {
            updateSubtitleLineSharedSettings();
        }

        if(changedSettings.contains(SharedPreferenceKey.TAG_REPLACEMENT)
                && simplifyTagsPreference.get()) {
            // ^ don't recreate tag replacements if tags are not simplified
            asyncChangeTagReplacement();
        }
    }

    @Override @Nullable
    public CommonSubtitleMvp.ViewInterface getViewInterface() {
        return viewInterface;
    }



    // ------------------------------------------------------------------------------- REPO CALLBACK

    @NotNull
    private final DisposableObserver<SubtitleEvent> subtitleEventObserver =
            new DisposableObserver<SubtitleEvent>() {
                @Override
                public void onNext(SubtitleEvent subtitleEvent) {
                    Timber.e("WTF - Dobio konacni event: %s", subtitleEvent.subtitleEventType.name());

                    if(subtitleEvent.subtitleEventType.equals(SubtitleEventType.LOADING)) {
                        if(viewInterface != null) viewInterface.showProgressLoadingFile();
                        return;
                    }

                    if(subtitleEvent.subtitleEventType.equals(SubtitleEventType.FULL_LOAD)) {
                        clearActiveSearchResult();

                        showSubtitleTitle(subtitleEvent.subtitleFile);

                        SubtitleContent content = subtitleEvent.subtitleFile.getSubtitleContent();
                        List<SubtitleLine> lines =
                                content == null ? new ArrayList<>() : content.getSubtitleLines();
                        asyncCreateAllVsos(lines);

                        if(viewInterface != null) viewInterface.hideProgress();
                        return;
                    }
                }

                @Override
                protected void onStart() {
                    Timber.e("WTF - POZVAN ON START");
                    super.onStart();
                }

                @Override
                public void onError(Throwable e) {
                    // TODO sta ako se ovo desi? Ne zelim ovo ikad... Mada mogu uvek da zovem opet da se subscribujem ili ono auto ima nesto
                }

                @Override
                public void onComplete() {
                    // TODO ne zelim ovo ikad, jel ima neka varijanta bez ovoga
                }
            };


    @Override @Deprecated
    public void onInvalidSubtitleFormatForLoading(@NonNull String subtitleFilename) {
        if(viewInterface == null) return;
        viewInterface.showErrorLoadingSubtitleInvalidFormat(subtitleFilename);
        viewInterface.hideProgress();
    }

    @Override @Deprecated
    public void onFileReadingFailed(@NonNull String subtitleFilename) {
        if(viewInterface == null) return;
        viewInterface.hideProgress();
        viewInterface.showErrorLoadingFailed(subtitleFilename);
    }

    @Override @Deprecated
    public void onSubtitleFileParsed(@NonNull SubtitleFile subtitleFile,
                                     @NonNull List<ParsingError> parsingErrors) {
        // TODO: utvrdi da li su neke fatalne greske i prikazi nekakav dijalog
        for(ParsingError parsingError : parsingErrors) {

        }

        onEndSearchRequested();

        if(viewInterface != null) {
            if(currentSearchQuery != null) onEndSearchRequested();
            viewInterface.removeAllSubtitleLines();
            showSubtitleTitle(subtitleFile);
            viewInterface.hideProgress();
        }

        asyncCreateAllVsos(subtitleFile.getSubtitleContent().getSubtitleLines());
    }

    @Override @Deprecated
    public void onSubtitleFileReloaded(@NonNull SubtitleFile subtitleFile) {
        showSubtitleTitle(subtitleFile);
        asyncCreateAllVsos(subtitleFile.getSubtitleContent().getSubtitleLines());
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    @MainThread
    private void updateSubtitleLineSharedSettings() {
        sharedSubtitleLineSettings.setTextSize(subLineTextSizePreference.get());
        sharedSubtitleLineSettings.setOtherSize(subLineOtherSizePreference.get());
        sharedSubtitleLineSettings.setShowTimings(subLineShowTimingsPreference.get());
        sharedSubtitleLineSettings.setShowActorAndStyle(subLineShowActorStylePreference.get());

        if(viewInterface != null) viewInterface.updateSubtitleLines();
    }

    @MainThread
    private void updateSearchQuery(@Nullable String searchQuery) {
        currentSearchQuery = searchQuery;
        sharedSubtitleLineSettings.setSearchQuery(currentSearchQuery);
        if(viewInterface != null) viewInterface.updateSubtitleLines();
    }

    @MainThread
    private void updateActiveSearchResult(int newSearchResultIndex, boolean scrollToActive) {
        clearActiveSearchResult();

        this.activeSearchResultIndex = newSearchResultIndex;

        if(activeSearchResultIndex != -1) {
            activeSearchResultVso = searchResults.get(activeSearchResultIndex);
            activeSearchResultVso.setPrimarySearchResult(true);
        }

        if(viewInterface == null) return;

        if(activeSearchResultIndex == -1) {
            // Two cases: no search query inputted or no results for query
            if(currentSearchQuery == null || currentSearchQuery.equals(""))
                viewInterface.hideSearchNumbers();
            else
                viewInterface.showSearchNumbers(0, 0);
        } else {
            viewInterface.showSearchNumbers(searchResults.size(), activeSearchResultIndex + 1);
            viewInterface.updateSubtitleLine(activeSearchResultVso);
            if(scrollToActive) viewInterface.scrollToLine(activeSearchResultVso);
        }
    }

    @MainThread
    private void clearActiveSearchResult() {
        if(activeSearchResultVso == null) return;

        activeSearchResultVso.setPrimarySearchResult(false);
        if(viewInterface != null) viewInterface.updateSubtitleLine(activeSearchResultVso);

        activeSearchResultVso = null;
    }


    // ---------------------------------------------------------------------------- INTERNAL - ASYNC

    private void asyncUpdateSearchCounter(boolean scrollToFirstResult) {
        String searchQuery = currentSearchQuery;

        if(searchCounterFuture != null && !searchCounterFuture.isDone())
            searchCounterFuture.cancel(true);

        int firstShownLineNumber = viewInterface == null? 1 : viewInterface.getFirstShownLineNumber();

        searchCounterFuture = singleThreadExecutor.submit(() -> {
            synchronized (vsoSyncObject) {
                searchResults.clear();

                if(searchQuery == null || searchQuery.isEmpty()) {
                    mainThreader.justExecute(() -> updateActiveSearchResult(-1, false));
                    return;
                }

                int nextClosestSearchResultIndex = -1;
                for(SubtitleLineVso subtitleLineVso : subtitleLineVsos) {
                    if(subtitleLineVso.getText().contains(searchQuery)) {
                        searchResults.add(subtitleLineVso);
                        if(nextClosestSearchResultIndex == -1
                                && subtitleLineVso.getLineNumber() >= firstShownLineNumber) {
                            nextClosestSearchResultIndex = searchResults.size()-1;
                        }
                    }
                }

                int finalNextClosestSearchResultIndex = nextClosestSearchResultIndex;
                mainThreader.justExecute(() -> updateActiveSearchResult(searchResults.size() == 0?
                        -1 : finalNextClosestSearchResultIndex, scrollToFirstResult));
            }
        });
    }

    private void asyncCreateAllVsos(@NonNull List<SubtitleLine> subtitleLines) {
        // Cancel all other tasks, if any are in progress
        if(vsosPartialCreationFuture != null && !vsosPartialCreationFuture.isDone())
            vsosPartialCreationFuture.cancel(true);
        if(vsosTagReplacementChangeFuture != null && !vsosTagReplacementChangeFuture.isDone())
            vsosTagReplacementChangeFuture.cancel(true);

            // If this happens, fuck it, just wait until the previous task is complete; [defensive]
        if(vsosCreationFuture != null && !vsosCreationFuture.isDone())
            try { vsosCreationFuture.get(); }
            catch (InterruptedException | ExecutionException e) {
                logger.e(e, "Vso full creation crashed while synchronously executing!");
            }

        TagPrettifier tagPrettifier = simplifyTagsPreference.get()?
                subtitleController.getTagPrettifierForCurrentSubtitle(tagReplacementPreference.get())
                : null;
        vsosCreationFuture = singleThreadExecutor.submit(() -> {
            synchronized (vsoSyncObject) {
                List<SubtitleLineVso> vsos = subtitleLineVsoFactory.createSubtitleLineVsos(
                        subtitleLines,
                        tagPrettifier,
                        sharedSubtitleLineSettings
                );

                if(Thread.interrupted()) return;

                subtitleLineVsos.addAll(vsos);

                if(viewInterface == null) return;

                // Copy serves 2 purposes - makes us safe from changes by the view (which shouldn't
                // happen, but better safe than sorry), and makes us safe from any changes that
                // could happen to subtitleLineVsos between this thread and the main thread
                List<SubtitleLineVso> listCopy = new ArrayList<>(subtitleLineVsos);
                mainThreader.justExecute(() -> viewInterface.showSubtitleLines(listCopy));
            }
        });
    }

    private void asyncRecreateSomeVsos(@NonNull List<SubtitleLine> subtitleLines) {
        // If full vso creation task is already in progress, just give up.
        if(vsosCreationFuture != null && !vsosCreationFuture.isDone())
            return;

            // If this happens, fuck it, just wait until the previous task is complete; [defensive]
        if(vsosPartialCreationFuture != null && !vsosPartialCreationFuture.isDone())
            try { vsosPartialCreationFuture.get(); }
            catch (InterruptedException | ExecutionException e) {
                logger.e(e, "Vso partial creation crashed while synchronously executing!");
            }
        vsosPartialCreationFuture = singleThreadExecutor.submit(() -> {
            synchronized (vsoSyncObject) {
                List<SubtitleLineVso> vsos = subtitleLineVsoFactory.createSubtitleLineVsos(
                        subtitleLines,
                        subtitleController.getTagPrettifierForCurrentSubtitle(
                                tagReplacementPreference.get()),
                        sharedSubtitleLineSettings
                );

                for(SubtitleLineVso editedVso : vsos) {
                    for (int i = 0; i < subtitleLineVsos.size(); i++) {
                        if (subtitleLineVsos.get(i).getId() == editedVso.getId()) {
                            subtitleLineVsos.set(i, editedVso);
                            break;
                        }
                    }
                }

                if(viewInterface == null) return;

                mainThreader.justExecute(() -> viewInterface.updateSubtitleLines(vsos));
            }
        });
    }

    private void asyncChangeTagReplacement() {

        if(vsosTagReplacementChangeFuture != null && !vsosTagReplacementChangeFuture.isDone())
            vsosTagReplacementChangeFuture.cancel(true);

        boolean simplifyTags = simplifyTagsPreference.get();

        // whatever other task is in progress, just execute this one. It will convert current vsos
        // whatever they may be after the current task completes (since a single thread is used)
/*
        vsosTagReplacementChangeFuture = singleThreadExecutor.submit(() -> {
            synchronized (vsoSyncObject) {

                // If current subtitle lines and their vsos are not the same, just call a full recreate
                // Note: should also check if IDs are the same
                if(subtitleController.getCurrentSubtitleFile() == null) return;
                List<SubtitleLine> lines =
                        subtitleController.getCurrentSubtitleFile().getSubtitleContent().getSubtitleLines();
                if(lines.size() != subtitleLineVsos.size()) {
                    asyncCreateAllVsos(lines);
                    return;
                }

                if(simplifyTags)
                    subtitleLineVsoFactory.modifyTagReplacements(
                            lines, subtitleLineVsos, tagReplacementPreference.get(),
                            subtitleController.getTagPrettifierForCurrentSubtitle(
                                    tagReplacementPreference.get()));
                else
                    subtitleLineVsoFactory.removeTagReplacements(lines, subtitleLineVsos);

                if(viewInterface == null) return;

                mainThreader.justExecute(() -> viewInterface.showSubtitleLines(subtitleLineVsos));
            }
        });*/
    }

}
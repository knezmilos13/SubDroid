package knez.assdroid.editor;

import android.net.Uri;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import knez.assdroid.common.SharedPreferenceKey;
import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.common.mvp.CommonSubtitlePresenter;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.handler.TagPrettifier;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import knez.assdroid.util.apache.FilenameUtils;
import knez.assdroid.util.preferences.BooleanPreference;
import knez.assdroid.util.preferences.IntPreference;
import knez.assdroid.util.preferences.StringPreference;
import timber.log.Timber;

public class EditorPresenter extends CommonSubtitlePresenter
        implements EditorMVP.PresenterInterface {

    @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
    @NonNull private final ExecutorService singleThreadExecutor;
    @NonNull private final Threader mainThreader;
    @NonNull private final Timber.Tree logger;

    @NonNull private final StringPreference tagReplacementPreference;
    @NonNull private final IntPreference subLineTextSizePreference;
    @NonNull private final IntPreference subLineOtherSizePreference;
    @NonNull private final BooleanPreference subLineShowTimingsPreference;
    @NonNull private final BooleanPreference subLineShowActorStylePreference;
    @NonNull private final BooleanPreference simplifyTagsPreference;

    private EditorMVP.ViewInterface viewInterface;
    private boolean presenterInitialized = false;

    @NonNull private final Object vsoSyncObject = new Object();

    private Future<?> vsosCreationFuture;
    private Future<?> vsosPartialCreationFuture;
    private Future<?> vsosTagReplacementChangeFuture;
    private Future<?> searchFuture;

    @Nullable private String currentSearchQuery = null;
    @NonNull private final List<SubtitleLineVso> subtitleLineVsos = new ArrayList<>();
    @NonNull private final SubtitleLineVso.SharedSettings sharedSubtitleLineSettings;
    @Nullable private SubtitleLineVso currentSearchResultVso = null;
    @NonNull private ArrayList<SubtitleLineVso> searchResults = new ArrayList<>();
    private int currentSearchResultIndex = -1;

    public EditorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLineVsoFactory subtitleLineVsoFactory,
            @NonNull FileHandler fileHandler,
            @NonNull ExecutorService singleThreadExecutor,
            @NonNull Threader mainThreader,
            @NonNull Timber.Tree logger,
            @NonNull StringPreference tagReplacementPreference,
            @NonNull IntPreference subLineTextSizePreference,
            @NonNull IntPreference subLineOtherSizePreference,
            @NonNull BooleanPreference subLineShowTimingsPreference,
            @NonNull BooleanPreference subLineShowActorStylePreference,
            @NonNull BooleanPreference simplifyTagsPreference) {
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
    public void onAttach(@NonNull EditorMVP.ViewInterface viewInterface) {
        this.viewInterface = viewInterface;

        // TODO and what if file was changed in the mean time?
        subtitleController.attachListener(this);

        // if reattaching to the same presenter (e.g. after orientation change)
        if(presenterInitialized) {
            showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
            viewInterface.showSubtitleLines(new ArrayList<>(subtitleLineVsos));

            if(currentSearchQuery != null && currentSearchQuery.length() > 0)
                viewInterface.showSearchQuery(currentSearchQuery);

            if(subtitleController.isLoadingFile()) viewInterface.showProgressLoadingFile();
            else if(subtitleController.isWritingFile()) viewInterface.showProgressSavingFile();

            return;
        }

        presenterInitialized = true;

        viewInterface.showCurrentQuickSettings(
                subLineShowTimingsPreference.get(),
                subLineShowActorStylePreference.get(),
                simplifyTagsPreference.get());

        if(subtitleController.getCurrentSubtitleFile() != null) {
            showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
            asyncCreateAllVsos(
                    subtitleController.getCurrentSubtitleFile().getSubtitleContent().getSubtitleLines());
        }
        else if(subtitleController.hasStoredSubtitle()) {
            subtitleController.reloadCurrentSubtitleFile();
        }
        else {
            onNewSubtitleRequested();
        }

    }

    @Override
    public void onDetach() {
        subtitleController.detachListener(this);
        viewInterface = null;
    }


    // ------------------------------------------------------------------------- PRESENTER INTERFACE

    @Override
    public void onSearchSubmitted(@NonNull final String text) {
        if(Objects.equals(text, currentSearchQuery)) return;

        currentSearchQuery = text;

        asyncSearch();

        sharedSubtitleLineSettings.setSearchQuery(currentSearchQuery);

        if(viewInterface == null) return;

        viewInterface.updateSubtitleLines();


        // TODO ne zaboravi ciscenje search rezultata nakon loadovanja i slicno, ako ce da bude asinc task
        // onda moras da otkazujes kad ugasi pretragu itd. Moras da prodjes kroz sve da prekontrolises.
    }

    @Override
    public void onEndSearchRequested() {
        currentSearchQuery = null;
        viewInterface.endSearch();
    }

    @Override
    public void onPrevSearchResultRequested() {
        if(currentSearchResultIndex == -1 || searchResults.isEmpty() || currentSearchResultVso == null) return;
        if(searchResults.size() == 1) return;

        clearCurrentSearchResult();

        if(currentSearchResultIndex == 0)
            currentSearchResultIndex = searchResults.size() - 1;
        else
            currentSearchResultIndex--;

        updateCurrentSearchResult();
    }

    // TODO ovo next/prev dira stvari koje mozda cackas sa drugog threada

    @Override
    public void onNextSearchResultRequested() {
        if(currentSearchResultIndex == -1 || searchResults.isEmpty() || currentSearchResultVso == null) return;
        if(searchResults.size() == 1) return;

        clearCurrentSearchResult();

        if(currentSearchResultIndex == searchResults.size() - 1)
            currentSearchResultIndex = 0;
        else
            currentSearchResultIndex++;

        updateCurrentSearchResult();
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
        if(viewInterface == null || subtitleController.isLoadingFile()) return;
        viewInterface.showTranslatorScreen(id);
    }

    @Override
    public void onSubtitleEditedExternally(@NonNull ArrayList<Long> editedLineIds) {
        showSubtitleTitle(subtitleController.getCurrentSubtitleFile()); // to update the "*"

        // TODO: preuzmi sve
        List<SubtitleLine> editedLines = new ArrayList<>();
        for(Long id : editedLineIds) {
            SubtitleLine line = subtitleController.getLineForId(id);
            if(line == null) continue; // defensive wtf // todo loguj
            editedLines.add(line);
        }

        asyncRecreateSomeVsos(editedLines);
    }

    @Override
    public void onNewSubtitleRequested() {
        subtitleController.createNewSubtitleFile();
        SubtitleFile newlyCreatedSubtitleFile = subtitleController.getCurrentSubtitleFile();
        showSubtitleTitle(newlyCreatedSubtitleFile);
        subtitleLineVsos.clear();

        if(viewInterface == null) return;
        viewInterface.showSubtitleLines(new ArrayList<>(subtitleLineVsos));
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


    // ------------------------------------------------------------------------------- REPO CALLBACK

    @Override
    public void onInvalidSubtitleFormatForLoading(@NonNull String subtitleFilename) {
        if(viewInterface == null) return;
        viewInterface.showErrorLoadingSubtitleInvalidFormat(subtitleFilename);
        viewInterface.hideProgress();
    }

    @Override
    public void onFileReadingFailed(@NonNull String subtitleFilename) {
        if(viewInterface == null) return;
        viewInterface.hideProgress();
        viewInterface.showErrorLoadingFailed(subtitleFilename);
    }

    @Override
    public void onSubtitleFileParsed(@NonNull SubtitleFile subtitleFile,
                                     @NonNull List<ParsingError> parsingErrors) {
        // TODO: utvrdi da li su neke fatalne greske i prikazi nekakav dijalog
        for(ParsingError parsingError : parsingErrors) {

        }

        // TODO clean out search query

        if(viewInterface != null) {
            viewInterface.removeAllCurrentSubtitleData();
            showSubtitleTitle(subtitleFile);
            viewInterface.hideProgress();
        }

        asyncCreateAllVsos(subtitleFile.getSubtitleContent().getSubtitleLines());
    }

    @Override
    public void onSubtitleFileReloaded(@NonNull SubtitleFile subtitleFile) {
        showSubtitleTitle(subtitleFile);
        asyncCreateAllVsos(subtitleFile.getSubtitleContent().getSubtitleLines());
    }

    @Override @Nullable
    public CommonSubtitleMVP.ViewInterface getViewInterface() {
        return viewInterface;
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
    private void clearCurrentSearchResult() {
        if(currentSearchResultVso == null) return;

        currentSearchResultVso.setPrimarySearchResult(false);
        if(viewInterface != null) viewInterface.updateSubtitleLine(currentSearchResultVso);

        currentSearchResultVso = null;
    }

    @MainThread
    private void updateCurrentSearchResult() {
        currentSearchResultVso = searchResults.get(currentSearchResultIndex);
        currentSearchResultVso.setPrimarySearchResult(true);

        if(viewInterface == null) return;

        viewInterface.showSearchNumbers(searchResults.size(), currentSearchResultIndex);
        viewInterface.updateSubtitleLine(currentSearchResultVso);
        viewInterface.scrollToLine(currentSearchResultVso);
    }


    // ---------------------------------------------------------------------------- INTERNAL - ASYNC

    private void asyncSearch() {
        String searchQuery = currentSearchQuery;
        if(searchQuery == null) return;

        if(searchFuture != null && !searchFuture.isDone())
            searchFuture.cancel(true);

        searchFuture = singleThreadExecutor.submit(() -> {
            synchronized (vsoSyncObject) {
                // Clear old results
                mainThreader.justExecute(this::clearCurrentSearchResult);

                searchResults.clear();
                for(SubtitleLineVso subtitleLineVso : subtitleLineVsos) {
                    if(subtitleLineVso.getText().contains(searchQuery))
                        searchResults.add(subtitleLineVso);
                }

                // If zero results, clear stuff out and show 0/0 results
                if(searchResults.size() == 0) {
                    currentSearchResultVso = null;
                    currentSearchResultIndex = -1;

                    mainThreader.justExecute(() -> {
                        viewInterface.showSearchNumbers(searchResults.size(), 0);
                    });

                    return;
                }

                currentSearchResultIndex = 0;

                mainThreader.justExecute(this::updateCurrentSearchResult);
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
        });
    }

}
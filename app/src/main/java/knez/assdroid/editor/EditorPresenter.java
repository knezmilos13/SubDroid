package knez.assdroid.editor;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
import knez.assdroid.util.preferences.BooleanPreference;
import knez.assdroid.util.preferences.IntPreference;
import knez.assdroid.util.preferences.StringPreference;
import solid.collections.SolidList;

public class EditorPresenter extends CommonSubtitlePresenter
        implements EditorMVP.PresenterInterface {

    @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
    @NonNull private final StringPreference tagReplacementPreference;
    @NonNull private final IntPreference subLineTextSizePreference;
    @NonNull private final IntPreference subLineOtherSizePreference;
    @NonNull private final BooleanPreference subLineShowTimingsPreference;
    @NonNull private final BooleanPreference subLineShowActorStylePreference;

    private EditorMVP.ViewInterface viewInterface;
    private boolean presenterInitialized = false;

    @NonNull private final Object vsoCreationSyncObject = new Object();

    // TODO: vidi settings, ima ona fora gde je svaki setting svoj objekat, jer ti je dependensi uvek
    // na samo nekim podesavanjima

    @Nullable private CreateVsosTask createVsosTask = null;

//    @NonNull private String[] currentSearchQuery = new String[0];
    @NonNull private List<SubtitleLineVso> allSubtitleLineVsos = new ArrayList<>();
//    @NonNull private SolidList<SubtitleLineVso> filteredSubtitleLineVsos = SolidList.empty();

    public EditorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLineVsoFactory subtitleLineVsoFactory,
            @NonNull FileHandler fileHandler,
            @NonNull StringPreference tagReplacementPreference,
            @NonNull IntPreference subLineTextSizePreference,
            @NonNull IntPreference subLineOtherSizePreference,
            @NonNull BooleanPreference subLineShowTimingsPreference,
            @NonNull BooleanPreference subLineShowActorStylePreference) {
        super(subtitleController, fileHandler);
        this.subtitleLineVsoFactory = subtitleLineVsoFactory;
        this.tagReplacementPreference = tagReplacementPreference;
        this.subLineTextSizePreference = subLineTextSizePreference;
        this.subLineOtherSizePreference = subLineOtherSizePreference;
        this.subLineShowTimingsPreference = subLineShowTimingsPreference;
        this.subLineShowActorStylePreference = subLineShowActorStylePreference;
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
            viewInterface.showSubtitleLines(new ArrayList<>(allSubtitleLineVsos));

            if(subtitleController.isLoadingFile()) viewInterface.showProgressLoadingFile();
            else if(subtitleController.isWritingFile()) viewInterface.showProgressSavingFile();

            return;
        }

        presenterInitialized = true;

//        viewInterface.showCurrentSubtitleLineSettings(subLineTextSizePreference.get(),
//                subLineOtherSizePreference.get());

        if(subtitleController.getCurrentSubtitleFile() != null) {
            showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
            asyncCreateSubtitleLineVsos(new SolidList<>(subtitleController.getCurrentSubtitleFile()
                    .getSubtitleContent().getSubtitleLines()));
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
//        String[] newQuery = CommonTasks.getQueryPartsFromInput(text);
//        if(CommonTasks.areSortedStringArraysEqual(currentSearchQuery, newQuery)) return;
//        currentSearchQuery = newQuery;
//        if(viewInterface == null) return;
    }

    @Override
    public void onFileSelectedForLoad(@NonNull Uri uri) {
        String filename = fileHandler.getFileNameFromUri(uri);
        String subtitleExtension = filename.substring(filename.lastIndexOf(".")+1);

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

        //noinspection unchecked
        new CreateVsosTask( // TODO zabelezi i ovaj, pa ako dodje onaj full, neka otkaze ovaj
                subtitleLineVsoFactory,
                subtitleController.getTagPrettifierForCurrentSubtitle(tagReplacementPreference.get()),
                this::onSelectedLinesConversionToVsosCompleted,
                vsoCreationSyncObject,
                subLineShowTimingsPreference.get(),
                subLineShowActorStylePreference.get(),
                subLineTextSizePreference.get(),
                subLineOtherSizePreference.get())
                .execute(new SolidList<>(editedLines));
    }

    @Override
    public void onNewSubtitleRequested() {
        subtitleController.createNewSubtitleFile();
        SubtitleFile newlyCreatedSubtitleFile = subtitleController.getCurrentSubtitleFile();
        showSubtitleTitle(newlyCreatedSubtitleFile);
        if(newlyCreatedSubtitleFile != null) // defensive & IDE - it should really not be null here
            asyncCreateSubtitleLineVsos(
                    new SolidList<>(newlyCreatedSubtitleFile.getSubtitleContent().getSubtitleLines()));
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

        asyncCreateSubtitleLineVsos(
                new SolidList<>(subtitleFile.getSubtitleContent().getSubtitleLines()));
    }

    @Override
    public void onSubtitleFileReloaded(@NonNull SubtitleFile subtitleFile) {
        showSubtitleTitle(subtitleFile);
        asyncCreateSubtitleLineVsos(
                new SolidList<>(subtitleFile.getSubtitleContent().getSubtitleLines()));
    }

    @Override @Nullable
    public CommonSubtitleMVP.ViewInterface getViewInterface() {
        return viewInterface;
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    private void asyncCreateSubtitleLineVsos(@NonNull SolidList<SubtitleLine> lines) {
        if(createVsosTask != null) createVsosTask.cancel(true);

        createVsosTask = new CreateVsosTask(
                subtitleLineVsoFactory,
                subtitleController.getTagPrettifierForCurrentSubtitle(tagReplacementPreference.get()),
                this::onCreateAllVsosTaskCompleted,
                vsoCreationSyncObject,
                subLineShowTimingsPreference.get(),
                subLineShowActorStylePreference.get(),
                subLineTextSizePreference.get(),
                subLineOtherSizePreference.get());
        //noinspection unchecked
        createVsosTask.execute(lines);
    }

    private void onCreateAllVsosTaskCompleted(List<SubtitleLineVso> result) {
        createVsosTask = null;
        allSubtitleLineVsos = new ArrayList<>(result);

        if(viewInterface == null) return;

        viewInterface.showSubtitleLines(new ArrayList<>(allSubtitleLineVsos));
    }

    private void onSelectedLinesConversionToVsosCompleted(@NonNull List<SubtitleLineVso> editedVsos) {
        for(SubtitleLineVso editedVso : editedVsos) {
            for (int i = 0; i < allSubtitleLineVsos.size(); i++) {
                if (allSubtitleLineVsos.get(i).getId() == editedVso.getId()) {
                    allSubtitleLineVsos.set(i, editedVso);
                    break;
                }
            }
        }

        if(viewInterface == null) return;
        viewInterface.updateSubtitleLines(editedVsos);
    }


    // ------------------------------------------------------------------------------------- CLASSES

    private static class CreateVsosTask
            extends AsyncTask<SolidList<SubtitleLine>, Void, List<SubtitleLineVso>> {

        @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
        @Nullable private final TagPrettifier tagPrettifier;
        @NonNull private final Callback callback;
        @NonNull private final Object syncObject;
        private final int textSizeDp;
        private final int otherSizeDp;
        private final boolean showTimings;
        private final boolean showActorAndStyle;

        CreateVsosTask(@NonNull SubtitleLineVsoFactory subtitleLineVsoFactory,
                       @Nullable TagPrettifier tagPrettifier,
                       @NonNull Callback callback,
                       @NonNull Object syncObject,
                       boolean showTimings,
                       boolean showActorAndStyle,
                       int textSizeDp,
                       int otherSizeDp) {
            this.subtitleLineVsoFactory = subtitleLineVsoFactory;
            this.tagPrettifier = tagPrettifier;
            this.showTimings = showTimings;
            this.showActorAndStyle = showActorAndStyle;
            this.textSizeDp = textSizeDp;
            this.otherSizeDp = otherSizeDp;
            this.callback = callback;
            this.syncObject = syncObject;
        }

        @SuppressWarnings("unchecked") // something about safe varargs
        @Override
        protected final List<SubtitleLineVso> doInBackground(SolidList<SubtitleLine>... params) {
            synchronized (syncObject) {
                return subtitleLineVsoFactory.createSubtitleLineVsos(
                        params[0], tagPrettifier, showTimings, showActorAndStyle, textSizeDp, otherSizeDp);
            }
        }

        @Override
        protected void onPostExecute(List<SubtitleLineVso> result) {
            callback.onVsoFactoryTaskCompleted(result);
        }

        @Override protected void onPreExecute() {}
        @Override protected void onProgressUpdate(Void... values) {}

        interface Callback {
            void onVsoFactoryTaskCompleted(@NonNull List<SubtitleLineVso> result);
        }
    }

}
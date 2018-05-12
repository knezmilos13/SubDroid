package knez.assdroid.editor;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.common.StorageHelper;
import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.common.mvp.CommonSubtitlePresenter;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import solid.collections.SolidList;

public class EditorPresenter extends CommonSubtitlePresenter
        implements EditorMVP.PresenterInterface {

    private static final String STORAGE_KEY_SUBTITLE_LINE_SETTINGS = "subtitle_line_settings";

    private static final boolean SUB_LINE_DEFAULT_SHOW_TIMINGS = true;
    private static final boolean SUB_LINE_DEFAULT_SHOW_STYLE_ACTOR = true;
    private static final boolean SUB_LINE_DEFAULT_SHOW_TAG_CONTENT = false;
    private static final String SUB_LINE_DEFAULT_TAG_REPLACEMENT = "田";
    private static final int SUB_LINE_DEFAULT_SUB_TEXT_SIZE_DP = 15;
    private static final int SUB_LINE_DEFAULT_OTHER_TEXT_SIZE_DP = 12;

    @NonNull private final SubtitleController subtitleController;
    @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
    @NonNull private final StorageHelper storageHelper;

    private EditorMVP.ViewInterface viewInterface;
    private SubtitleLineSettings subtitleLineSettings;
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
            @NonNull StorageHelper storageHelper) {
        super(subtitleController);
        this.subtitleController = subtitleController;
        this.subtitleLineVsoFactory = subtitleLineVsoFactory;
        this.storageHelper = storageHelper;
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
            viewInterface.showSubtitleLines(new SolidList<>(allSubtitleLineVsos));
            if(subtitleController.isLoadingFile()) viewInterface.showProgressLoadingFile();
            return;
        }

        presenterInitialized = true;

        subtitleLineSettings = storageHelper.readJson(
                STORAGE_KEY_SUBTITLE_LINE_SETTINGS, SubtitleLineSettings.class);

        if(subtitleLineSettings == null) {
            subtitleLineSettings = createDefaultSubtitleLineSettings();
            storageHelper.writeJson(STORAGE_KEY_SUBTITLE_LINE_SETTINGS, subtitleLineSettings);
        }

        viewInterface.showCurrentSubtitleLineSettings(subtitleLineSettings);

        if(subtitleController.getCurrentSubtitleFile() != null) {
            showSubtitleTitle(subtitleController.getCurrentSubtitleFile());
            asyncCreateSubtitleLineVsos(subtitleController.getCurrentSubtitleFile()
                    .getSubtitleContent().getSubtitleLines());
        }
        else if(subtitleController.hasStoredSubtitle()) {
            subtitleController.reloadCurrentSubtitleFile();
        }
        else {
            SubtitleFile newlyCreatedSubtitleFile = subtitleController.createNewSubtitleFile();
            showSubtitleTitle(newlyCreatedSubtitleFile);
            asyncCreateSubtitleLineVsos(newlyCreatedSubtitleFile.getSubtitleContent().getSubtitleLines());
        }

    }

    @Override
    public void onDetach() {
        subtitleController.detachListener(this);
        viewInterface = null;
    }


    // --------------------------------------------------------------------------- USER & APP EVENTS

    @Override
    public void onSearchSubmitted(@NonNull final String text) {
//        String[] newQuery = CommonTasks.getQueryPartsFromInput(text);
//        if(CommonTasks.areSortedStringArraysEqual(currentSearchQuery, newQuery)) return;
//        currentSearchQuery = newQuery;
//        if(viewInterface == null) return;
//        TO DO - ovde bi sad zvao filtriranje, a inace je islo preko delayed taska, sto ti ne treba
    }

    @Override
    public void onFileSelectedForLoad(@NonNull Uri uri, @NonNull String filename) {
        String subtitleExtension = filename.substring(filename.lastIndexOf(".")+1);

        if(!subtitleController.canLoadExtension(subtitleExtension)) {
            viewInterface.showErrorLoadingSubtitleInvalidFormat(filename);
            return;
        }

        viewInterface.showProgressLoadingFile();
        subtitleController.parseSubtitle(uri);
    }

    @Override
    public void onSubtitleLineClicked(long id) {
        if(viewInterface == null) return;
        viewInterface.showTranslatorScreen(id);
    }

    @Override
    public void onSubtitleEditedExternally(@NonNull ArrayList<Integer> editedLineNumbers) {
        showSubtitleTitle(subtitleController.getCurrentSubtitleFile()); // to update the "*"

        // TODO: preuzmi sve // TODO ipak neka budu idjevi
        List<SubtitleLine> editedLines = new ArrayList<>();
        for(Integer lineNumber : editedLineNumbers) {
            SubtitleLine line = subtitleController.getLineForNumber(lineNumber);
            if(line == null) continue; // defensive wtf // todo loguj
            editedLines.add(line);
        }

        //noinspection unchecked
        new CreateVsosTask(
                subtitleLineVsoFactory, subtitleLineSettings,
                this::onSelectedLinesConversionToVsosCompleted, vsoCreationSyncObject)
                .execute(new SolidList<>(editedLines));
    }


    // ------------------------------------------------------------------------------- REPO CALLBACK

    @Override
    public void onInvalidSubtitleFormat(@NonNull String subtitleFilename) {
        // TODO prikazi poruku
    }

    @Override
    public void onFileReadingFailed(@NonNull String subtitleFilename) {
        // TODO prikazi poruku
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

        asyncCreateSubtitleLineVsos(subtitleFile.getSubtitleContent().getSubtitleLines());
    }

    @Override
    public void onSubtitleFileReloaded(@NonNull SubtitleFile subtitleFile) {
        showSubtitleTitle(subtitleFile);
        asyncCreateSubtitleLineVsos(subtitleFile.getSubtitleContent().getSubtitleLines());
    }

    @Override @Nullable
    public CommonSubtitleMVP.ViewInterface getViewInterface() {
        return viewInterface;
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    @NonNull
    private SubtitleLineSettings createDefaultSubtitleLineSettings() {
        return new SubtitleLineSettings(
                SUB_LINE_DEFAULT_SHOW_TIMINGS, SUB_LINE_DEFAULT_SHOW_STYLE_ACTOR,
                SUB_LINE_DEFAULT_SHOW_TAG_CONTENT, SUB_LINE_DEFAULT_TAG_REPLACEMENT,
                SUB_LINE_DEFAULT_SUB_TEXT_SIZE_DP, SUB_LINE_DEFAULT_OTHER_TEXT_SIZE_DP);
    }

    private void asyncCreateSubtitleLineVsos(@NonNull List<SubtitleLine> lines) {
        if(createVsosTask != null) createVsosTask.cancel(true);

        createVsosTask = new CreateVsosTask(
                subtitleLineVsoFactory, subtitleLineSettings,
                this::onCreateAllVsosTaskCompleted, vsoCreationSyncObject);
        //noinspection unchecked
        createVsosTask.execute(new SolidList<>(lines));
    }

    private void onCreateAllVsosTaskCompleted(SolidList<SubtitleLineVso> result) { // TODO solid?
        createVsosTask = null;
        allSubtitleLineVsos = new ArrayList<>(result);

        if(viewInterface == null) return;

        viewInterface.showSubtitleLines(new ArrayList<>(allSubtitleLineVsos));
    }

    private void onSelectedLinesConversionToVsosCompleted(
            @NonNull SolidList<SubtitleLineVso> editedVsos) {
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
            extends AsyncTask<SolidList<SubtitleLine>, Void, SolidList<SubtitleLineVso>> {

        @NonNull private final Callback callback;
        @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
        @NonNull private final SubtitleLineSettings subtitleLineSettings;
        @NonNull private final Object syncObject;

        CreateVsosTask(@NonNull SubtitleLineVsoFactory subtitleLineVsoFactory,
                       @NonNull SubtitleLineSettings subtitleLineSettings,
                       @NonNull Callback callback,
                       @NonNull Object syncObject) {
            this.subtitleLineVsoFactory = subtitleLineVsoFactory;
            this.subtitleLineSettings = subtitleLineSettings;
            this.callback = callback;
            this.syncObject = syncObject;
        }

        @SuppressWarnings("unchecked") // something about safe varargs
        @Override
        protected final SolidList<SubtitleLineVso> doInBackground(SolidList<SubtitleLine>... params) {
            synchronized (syncObject) {
                return new SolidList<>(
                        subtitleLineVsoFactory.createSubtitleLineVsos(params[0], subtitleLineSettings));
            }
        }

        @Override
        protected void onPostExecute(SolidList<SubtitleLineVso> result) {
            callback.onVsoFactoryTaskCompleted(result);
        }

        @Override protected void onPreExecute() {}
        @Override protected void onProgressUpdate(Void... values) {}

        interface Callback {
            void onVsoFactoryTaskCompleted(@NonNull SolidList<SubtitleLineVso> result);
        }
    }

}
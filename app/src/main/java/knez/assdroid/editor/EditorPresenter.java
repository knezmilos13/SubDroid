package knez.assdroid.editor;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import knez.assdroid.common.StorageHelper;
import knez.assdroid.common.mvp.CommonSubtitlePresenter;
import knez.assdroid.editor.adapter.SubtitleLineDiffCallback;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.editor.vso.SubtitleLineVsoFactory;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.ParsingError;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.subtitle.data.SubtitleLine;
import solid.collections.SolidList;

public class EditorPresenter extends CommonSubtitlePresenter
        implements EditorMVP.PresenterInterface, SubtitleController.Callback {

    private static final String STORAGE_KEY_SUBTITLE_LINE_SETTINGS = "subtitle_line_settings";

    private static final boolean SUB_LINE_DEFAULT_SHOW_TIMINGS = true;
    private static final boolean SUB_LINE_DEFAULT_SHOW_STYLE_ACTOR = true;
    private static final boolean SUB_LINE_DEFAULT_SHOW_TAG_CONTENT = false;
    private static final String SUB_LINE_DEFAULT_TAG_REPLACEMENT = "田";
    private static final int SUB_LINE_DEFAULT_SUB_TEXT_SIZE_DP = 14;
    private static final int SUB_LINE_DEFAULT_OTHER_TEXT_SIZE_DP = 12;

    @NonNull private final SubtitleController subtitleController;
    @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
    @NonNull private final StorageHelper storageHelper;

    private EditorMVP.ViewInterface viewInterface;
    private SubtitleLineSettings subtitleLineSettings;

    // TODO: mnogo ti je glomazno ovo sve sa filtriranjem. Vidi da li moze to da se offloaduje na adapter
    // bar diff utilovanje mora da moze kroz onaj neki novi recycler ili sta god

    // TODO: vidi settings, ima ona fora gde je svaki setting svoj objekat, jer ti je dependensi uvek
    // na samo nekim podesavanjima

    @Nullable private DiffUtilTask diffUtilTask = null;
    @Nullable private VsoFactoryTask vsoFactoryTask = null;

    @NonNull private String[] currentSearchQuery = new String[0];
    @NonNull private SolidList<SubtitleLineVso> allSubtitleLineVsos = SolidList.empty();
    @NonNull private SolidList<SubtitleLineVso> filteredSubtitleLineVsos = SolidList.empty();

    public EditorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull SubtitleLineVsoFactory subtitleLineVsoFactory,
            @NonNull StorageHelper storageHelper) {
        this.subtitleController = subtitleController;
        this.subtitleLineVsoFactory = subtitleLineVsoFactory;
        this.storageHelper = storageHelper;
    }


    // ---------------------------------------------------------------------------- SETUP & TEARDOWN

    @Override
    public void onAttach(@NonNull EditorMVP.ViewInterface viewInterface) {
        super.onAttach(viewInterface);
        this.viewInterface = viewInterface;

        subtitleLineSettings = storageHelper.readJson(
                STORAGE_KEY_SUBTITLE_LINE_SETTINGS, SubtitleLineSettings.class);

        if(subtitleLineSettings == null) {
            subtitleLineSettings = createDefaultSubtitleLineSettings();
            storageHelper.writeJson(STORAGE_KEY_SUBTITLE_LINE_SETTINGS, subtitleLineSettings);
        }

        viewInterface.showCurrentSubtitleLineSettings(subtitleLineSettings);

        subtitleController.attachListener(this);

        if(subtitleController.getCurrentSubtitleFile() != null) {
            showSubtitleFile(subtitleController.getCurrentSubtitleFile());
        } else if(subtitleController.hasStoredSubtitle()) {
            subtitleController.reloadCurrentSubtitleFile();
        } else {
            SubtitleFile newlyCreatedSubtitleFile = subtitleController.createNewSubtitleFile();
            showSubtitleFile(newlyCreatedSubtitleFile);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(diffUtilTask != null) {
            diffUtilTask.cancel(true);
            diffUtilTask = null;
        }
        if(vsoFactoryTask != null) {
            vsoFactoryTask.cancel(true);
            vsoFactoryTask = null;
        }

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

        subtitleController.parseSubtitle(uri);
    }

    @Override
    public void onFileSelectedForSaving(@NonNull Uri uri, @NonNull String filename) {
        String subtitleExtension = filename.substring(filename.lastIndexOf(".")+1);

        if(!subtitleController.canWriteSubtitle(subtitleExtension)) {
            viewInterface.showErrorWritingSubtitleInvalidFormat(filename);
            return;
        }

        subtitleController.writeSubtitle(uri);
    }

    @Override
    public void onSubtitleLineClicked(long id) {
        if(viewInterface == null) return;
        viewInterface.showTranslatorScreen(id);
    }

    @Override
    public void onShowHelpClicked() {
        if(viewInterface == null) return;
        viewInterface.showHelpScreen();
    }

    @Override
    public void onShowSettingsClicked() {
        if(viewInterface == null) return;
        viewInterface.showSettingsScreen();
    }

    @Override @Nullable
    public String getCurrentSubtitleName() {
        SubtitleFile currentSubtitleFile = subtitleController.getCurrentSubtitleFile();
        if(currentSubtitleFile == null) return null;
        else return currentSubtitleFile.getName();
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
    public void onFileWritingFailed(@NonNull String destFilename) {
        // TODO prikazi poruku
    }

    @Override
    public void onSubtitleFileParsed(@NonNull SubtitleFile subtitleFile,
                                     @NonNull List<ParsingError> parsingErrors) {
        if(viewInterface == null) return;

        // TODO: utvrdi da li su neke fatalne greske i prikazi nekakav dijalog
        for(ParsingError parsingError : parsingErrors) {

        }

        viewInterface.removeAllCurrentSubtitleData();
        // TODO: ocisti linije koje mi sami drzimo ovde

        showSubtitleFile(subtitleFile);
    }

    @Override
    public void onSubtitleFileReloaded(@NonNull SubtitleFile subtitleFile) {
        showSubtitleFile(subtitleFile);
    }

    @Override
    public void onSubtitleFileSaved(@NonNull SubtitleFile subtitleFile) {
        if(viewInterface == null) return;
        showSubtitleTitle(subtitleFile);
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    @NonNull
    private SubtitleLineSettings createDefaultSubtitleLineSettings() {
        return new SubtitleLineSettings(
                SUB_LINE_DEFAULT_SHOW_TIMINGS, SUB_LINE_DEFAULT_SHOW_STYLE_ACTOR,
                SUB_LINE_DEFAULT_SHOW_TAG_CONTENT, SUB_LINE_DEFAULT_TAG_REPLACEMENT,
                SUB_LINE_DEFAULT_SUB_TEXT_SIZE_DP, SUB_LINE_DEFAULT_OTHER_TEXT_SIZE_DP);
    }

    private void showSubtitleFile(@NonNull SubtitleFile subtitleFile) {
        showSubtitleTitle(subtitleFile);

        asyncCreateSubtitleLineVsos(subtitleFile.getSubtitleContent().getSubtitleLines());
    }

    private void showResultsForQuery(@NonNull final String[] queryToShow) {
//        if(diffUtilTask != null) diffUtilTask.cancel(true);

        // TODO: cekaj kako ovo ima logike? ako je query 0 treba da prikazes sve, a sto ne bi morao da diffjes?
        if(queryToShow.length == 0) {
            filteredSubtitleLineVsos = new SolidList<>(allSubtitleLineVsos);
            viewInterface.showSubtitleLines(filteredSubtitleLineVsos);
            return;
        }

        // Note: can't store new results here now, pass them to asyncTask and store them in this
        // presenter only when work is done. Otherwise if a new query cancels the previous one, it
        // will get to work with the wrong number of "old" results.
        SolidList<SubtitleLineVso> newResultVsos =
                new SolidList<>(filterVsos(allSubtitleLineVsos, queryToShow));

        SubtitleLineDiffCallback diffCallback =
                new SubtitleLineDiffCallback(filteredSubtitleLineVsos, newResultVsos);
        diffUtilTask = new DiffUtilTask(
                diffCallback, newResultVsos, EditorPresenter.this::onDiffUtilTaskCompleted);
        diffUtilTask.execute();
    }

    private List<SubtitleLineVso> filterVsos(@NonNull final List<SubtitleLineVso> itemVsos,
                                             @NonNull final String[] searchQuery) {
        List<SubtitleLineVso> filteredVsos = new ArrayList<>();

        outer:
        for(SubtitleLineVso vso : itemVsos) {
            for(String queryPart : searchQuery) {
                if(!vso.getText().toLowerCase().contains(queryPart)) continue outer;
            }

            filteredVsos.add(vso);
        }

        return filteredVsos;
    }

    private void asyncCreateSubtitleLineVsos(@NonNull List<SubtitleLine> lines) {
        if(vsoFactoryTask != null) vsoFactoryTask.cancel(true);

        vsoFactoryTask = new VsoFactoryTask(
                subtitleLineVsoFactory, subtitleLineSettings,
                EditorPresenter.this::onVsoFactoryTaskCompleted);
        vsoFactoryTask.execute(new SolidList<>(lines));
    }

    private void onDiffUtilTaskCompleted(@NonNull final DiffUtil.DiffResult result,
                                         @NonNull final SolidList<SubtitleLineVso> newFilteredResults) {
        diffUtilTask = null;
        filteredSubtitleLineVsos = newFilteredResults;
        if(viewInterface != null) viewInterface.showSubtitleLines(filteredSubtitleLineVsos, result);
    }

    private void onVsoFactoryTaskCompleted(SolidList<SubtitleLineVso> result) {
        vsoFactoryTask = null;
        allSubtitleLineVsos = result;
        showResultsForQuery(currentSearchQuery); // can now apply query to the set of all items
    }


    // ------------------------------------------------------------------------------------- CLASSES

    private static class DiffUtilTask extends AsyncTask<Void, Void, DiffUtil.DiffResult> {

        @NonNull private final WeakReference<Callback> callback;
        @NonNull private final SubtitleLineDiffCallback diffCallback;
        @NonNull private final SolidList<SubtitleLineVso> newFilteredResults;

        DiffUtilTask(@NonNull final SubtitleLineDiffCallback diffCallback,
                     @NonNull final SolidList<SubtitleLineVso> newFilteredResults,
                     @NonNull final Callback callback) {
            this.diffCallback = diffCallback;
            this.newFilteredResults = newFilteredResults;
            this.callback = new WeakReference<>(callback);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(Void... params) {
            return DiffUtil.calculateDiff(diffCallback);
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult result) {
            Callback actualCallback = callback.get();
            if(actualCallback != null)
                actualCallback.onDiffUtilTaskCompleted(result, newFilteredResults);
        }

        @Override protected void onPreExecute() {}
        @Override protected void onProgressUpdate(Void... values) {}

        interface Callback {
            void onDiffUtilTaskCompleted(@NonNull DiffUtil.DiffResult result,
                                         @NonNull SolidList<SubtitleLineVso> filteredResults);
        }
    }

    private static class VsoFactoryTask
            extends AsyncTask<SolidList<SubtitleLine>, Void, SolidList<SubtitleLineVso>> {

        @NonNull private final WeakReference<Callback> callback;
        @NonNull private final SubtitleLineVsoFactory subtitleLineVsoFactory;
        @NonNull private final SubtitleLineSettings subtitleLineSettings;

        VsoFactoryTask(@NonNull SubtitleLineVsoFactory subtitleLineVsoFactory,
                       @NonNull SubtitleLineSettings subtitleLineSettings,
                       @NonNull Callback callback) {
            this.subtitleLineVsoFactory = subtitleLineVsoFactory;
            this.subtitleLineSettings = subtitleLineSettings;
            this.callback = new WeakReference<>(callback);
        }

        @SuppressWarnings("unchecked") // something about safe varargs
        @Override
        protected final SolidList<SubtitleLineVso> doInBackground(SolidList<SubtitleLine>... params) {
            return new SolidList<>(
                    subtitleLineVsoFactory.createSubtitleLineVsos(params[0], subtitleLineSettings));
        }

        @Override
        protected void onPostExecute(SolidList<SubtitleLineVso> result) {
            Callback actualCallback = callback.get();
                if(actualCallback != null) actualCallback.onVsoFactoryTaskCompleted(result);
        }

        @Override protected void onPreExecute() {}
        @Override protected void onProgressUpdate(Void... values) {}

        interface Callback {
            void onVsoFactoryTaskCompleted(@NonNull SolidList<SubtitleLineVso> result);
        }
    }

}
package knez.assdroid.editor;

import android.net.Uri;
import android.support.annotation.NonNull;

import knez.assdroid.common.Navigator;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.util.DelayAsyncTask;

public class EditorPresenter
        implements EditorMVP.PresenterInterface, SubtitleController.Callback {

    @NonNull private final SubtitleController subtitleController;
    @NonNull private final Navigator navigator;
//    @NonNull private final ItemVsoFactory itemVsoFactory;
    private final long typingDelayMillis;

    private EditorMVP.ViewInterface viewInterface;

//    @Nullable private DelayAsyncTask delayAsyncTask = null;
//    @Nullable private DiffUtilTask diffUtilTask = null;
//    @Nullable private VsoFactoryTask vsoFactoryTask = null;

    @NonNull private String[] currentSearchQuery = new String[0];
//    @NonNull private SolidList<ItemVso> allItemVsos = SolidList.empty();
//    @NonNull private SolidList<ItemVso> filteredItemVsos = SolidList.empty();

    public EditorPresenter(
            @NonNull SubtitleController subtitleController,
            @NonNull Navigator navigator,
//            @NonNull ItemVsoFactory itemVsoFactory,
            long typingDelayMillis) {
        this.subtitleController = subtitleController;
        this.navigator = navigator;
//        this.itemVsoFactory = itemVsoFactory;
        this.typingDelayMillis = typingDelayMillis;
    }


    // ---------------------------------------------------------------------------- SETUP & TEARDOWN

    @Override
    public void onAttach(@NonNull EditorMVP.ViewInterface viewInterface) {
        this.viewInterface = viewInterface;

        subtitleController.attachListener(this);

        // TODO: vidi da li treba sta da ucitas; da li imas u bazi nesto i neki editovan fajl u opticaju
    }

    @Override
    public void onDetach() {
//        if(diffUtilTask != null) {
//            diffUtilTask.cancel(true);
//            diffUtilTask = null;
//        }
//        if(vsoFactoryTask != null) {
//            vsoFactoryTask.cancel(true);
//            vsoFactoryTask = null;
//        }
//        if(delayAsyncTask != null) {
//            delayAsyncTask.cancel(true);
//            delayAsyncTask = null;
//        }

        subtitleController.detachListener(this);
        viewInterface = null;
    }


    // --------------------------------------------------------------------------- USER & APP EVENTS

//    @Override
//    public void showItemDetails(int itemId) {
//        navigator.startItemDetailsScreen(itemId);
//    }

    @Override
    public void onSearchSubmitted(@NonNull final String text) {
//        String[] newQuery = CommonTasks.getQueryPartsFromInput(text);

//        if(CommonTasks.areSortedStringArraysEqual(currentSearchQuery, newQuery)) return;

//        currentSearchQuery = newQuery;

//        if(viewInterface == null) return;

//        if(delayAsyncTask != null) delayAsyncTask.cancel(true);
//        delayAsyncTask = new DelayAsyncTask(this, typingDelayMillis);
//        delayAsyncTask.execute();
    }

    @Override
    public void onFileSelectedForLoad(@NonNull Uri data, @NonNull String filename) {
        if(!subtitleController.canLoadSubtitle(filename)) {
            viewInterface.showErrorLoadingSubtitleInvalidFormat(filename);
            return;
        }

        viewInterface.removeAllCurrentSubtitleData();
        // TODO: ocisti linije koje mi sami drzimo ovde

        subtitleController.loadSubtitle(data);

        // TODO: kojim ces redom ovo? odma da pokazes novi naslov... ili da uklonis stari... ili da ostavis
        // stari dok ne zavrsi sa poslom? Mozda ta zadnja opcija je najbolja...
        viewInterface.showTitleForFilename(filename, false);
    }

    @Override
    public void onShowHelpClicked() {
        navigator.showHelpScreen();
    }

    @Override
    public void onShowSettingsClicked() {
        navigator.showSettingsScreen();
    }


    // ------------------------------------------------------------------------------- REPO CALLBACK
//
//    @Override
//    public void onDataDownloaded(List<Item> items, PaginationData paginationData, int priority) {
//        asyncCreateItemVsos(new SolidList<>(items));
//    }

    @Override
    public void onInvalidSubtitleFormat(@NonNull String subtitleFilename) {
        // TODO prikazi poruku
    }

    @Override
    public void onFileReadingFailed(@NonNull String subtitleFilename) {
        // TODO prikazi poruku
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    private void showResultsForQuery(@NonNull final String[] queryToShow) {
//        if(diffUtilTask != null) diffUtilTask.cancel(true);
//
//        if(queryToShow.length == 0) {
//            filteredItemVsos = new SolidList<>(allItemVsos);
//            viewInterface.showItems(filteredItemVsos);
//            return;
//        }
//
//        // Note: can't store new results here now, pass them to asyncTask and store them in this
//        // presenter only when work is done. Otherwise if a new query cancels the previous one, it
//        // will get to work with the wrong number of "old" results.
//        SolidList<ItemVso> newResultVsos = new SolidList<>(filterVsos(allItemVsos, queryToShow));
//
//        ItemDiffCallback diffCallback = new ItemDiffCallback(filteredItemVsos, newResultVsos);
//        diffUtilTask = new DiffUtilTask(
//                diffCallback, newResultVsos, EditorPresenter.this::onDiffUtilTaskCompleted);
//        diffUtilTask.execute();
    }

//    private List<ItemVso> filterVsos(@NonNull final List<ItemVso> itemVsos,
//                                     @NonNull final String[] searchQuery) {
//        List<ItemVso> filteredVsos = new ArrayList<>();
//
//        outer:
//        for(ItemVso vso : itemVsos) {
//            for(String queryPart : searchQuery) {
//                if(!vso.getText().toLowerCase().contains(queryPart)) continue outer;
//            }
//
//            filteredVsos.add(vso);
//        }
//
//        return filteredVsos;
//    }

//    private void asyncCreateItemVsos(SolidList<Item> items) {
//        if(vsoFactoryTask != null) vsoFactoryTask.cancel(true);
//
//        vsoFactoryTask = new VsoFactoryTask(
//                itemVsoFactory, EditorPresenter.this::onVsoFactoryTaskCompleted);
//        vsoFactoryTask.execute(items);
//    }

//    private void onDiffUtilTaskCompleted(@NonNull final DiffUtil.DiffResult result,
//                                         @NonNull final SolidList<ItemVso> newFilteredResults) {
//        diffUtilTask = null;
//        filteredItemVsos = newFilteredResults;
//        if(viewInterface != null) viewInterface.showItems(filteredItemVsos, result);
//    }

//    private void onVsoFactoryTaskCompleted(SolidList<ItemVso> result) {
//        vsoFactoryTask = null;
//        allItemVsos = result;
//        showResultsForQuery(currentSearchQuery); // can now apply query to the set of all items
//    }


    // ------------------------------------------------------------------------------------- CLASSES

//    private static class DiffUtilTask extends AsyncTask<Void, Void, DiffUtil.DiffResult> {
//
//        @NonNull private final WeakReference<Callback> callback;
//        @NonNull private final ItemDiffCallback itemDiffCallback;
//        @NonNull private final SolidList<ItemVso> newFilteredResults;
//
//        DiffUtilTask(@NonNull final ItemDiffCallback itemDiffCallback,
//                     @NonNull final SolidList<ItemVso> newFilteredResults,
//                     @NonNull final Callback callback) {
//            this.itemDiffCallback = itemDiffCallback;
//            this.newFilteredResults = newFilteredResults;
//            this.callback = new WeakReference<>(callback);
//        }
//
//        @Override
//        protected DiffUtil.DiffResult doInBackground(Void... params) {
//            return DiffUtil.calculateDiff(itemDiffCallback);
//        }
//
//        @Override
//        protected void onPostExecute(DiffUtil.DiffResult result) {
//            Callback actualCallback = callback.get();
//            if(actualCallback != null)
//                actualCallback.onDiffUtilTaskCompleted(result, newFilteredResults);
//        }
//
//        @Override protected void onPreExecute() {}
//        @Override protected void onProgressUpdate(Void... values) {}
//
//        interface Callback {
//            void onDiffUtilTaskCompleted(@NonNull DiffUtil.DiffResult result,
//                                         @NonNull SolidList<ItemVso> filteredResults);
//        }
//    }
//
//    private static class VsoFactoryTask
//            extends AsyncTask<SolidList<Item>, Void, SolidList<ItemVso>> {
//
//        @NonNull private final WeakReference<Callback> callback;
//        @NonNull private final ItemVsoFactory itemVsoFactory;
//
//        VsoFactoryTask(@NonNull ItemVsoFactory itemVsoFactory, @NonNull Callback callback) {
//            this.itemVsoFactory = itemVsoFactory;
//            this.callback = new WeakReference<>(callback);
//        }
//
//        @SuppressWarnings("unchecked") // something about safe varargs
//        @Override
//        protected final SolidList<ItemVso> doInBackground(SolidList<Item>... params) {
//            return new SolidList<>(itemVsoFactory.createItemVsos(params[0]));
//        }
//
//        @Override
//        protected void onPostExecute(SolidList<ItemVso> result) {
//            Callback actualCallback = callback.get();
//                if(actualCallback != null) actualCallback.onVsoFactoryTaskCompleted(result);
//        }
//
//        @Override protected void onPreExecute() {}
//        @Override protected void onProgressUpdate(Void... values) {}
//
//        interface Callback {
//            void onVsoFactoryTaskCompleted(@NonNull SolidList<ItemVso> result);
//        }
//    }

}
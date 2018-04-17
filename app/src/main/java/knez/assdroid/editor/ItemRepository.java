package knez.assdroid.editor;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.common.AbstractRepo;
import knez.assdroid.util.Threader;
import timber.log.Timber;

// TODO u osnovi je ovo subtitle handler tako da spoji
public class ItemRepository extends AbstractRepo {

//    @NonNull private final ApiTaskQueue apiTaskQueue;
//    @NonNull private final Client client;
//    @NonNull private final GetItemsTaskFactory getItemsTaskFactory;
//    @NonNull private final AuthHandler authHandler;
    @NonNull private final Timber.Tree logger;
    @NonNull private final Threader mainThreader;

    @NonNull private final List<Callback> callbacks = new ArrayList<>();

    public ItemRepository(/*@NonNull ApiTaskQueue apiTaskQueue,
                          @NonNull Client client,
                          @NonNull GetItemsTaskFactory getItemsTaskFactory,
                          @NonNull AuthHandler authHandler,*/
                          @NonNull Timber.Tree logger,
                          @NonNull Threader mainThreader) {
//        this.apiTaskQueue = apiTaskQueue;
//        this.client = client;
//        this.getItemsTaskFactory = getItemsTaskFactory;
//        this.authHandler = authHandler;
        this.logger = logger;
        this.mainThreader = mainThreader;
    }

    public void attachListener(Callback callback) {
        if(callbacks.contains(callback)) return;
        callbacks.add(callback);
    }

    public void detachListener(Callback callback) {
        callbacks.remove(callback);
    }
//
//    // TODO: parametar da li zelis kesirano, i posebne metode za posebne slucajeve?
//    public void requestItems(int priorityLevel, int offset, int limit) {
//        // TODO ako ima u kesu, daj iz kesa?
//
//        // If there is a request with a same or greater priority level, give up
//        if(isDownloadingItems(priorityLevel)) return;
//
//        // If this is the lowest priority, you won't be cancelling anything, so that's that
//        if(priorityLevel != GetItemsTask.PRIORITY_LOW) {
//            // If it's mid or top, cancel existing lower level requests
//            List<ApiTask> requests = apiTaskQueue.getTasksForOperation(GetItemsTask.OP_NAME);
//
//            for (ApiTask existingRequest : requests) {
//                if (!(existingRequest instanceof GetItemsTask)) continue; // wtf defense
//                if (((GetItemsTask) existingRequest).getPriorityLevel() < priorityLevel) {
//                    existingRequest.cancelRequest();
//                }
//            }
//        }
//
//        GetItemsTask getItemsTask = getItemsTaskFactory.create(this, priorityLevel, limit, offset);
//
//        if (!client.isAuthTokenFresh()) {
//            logger.w("API key invalid for task instance %s", toString());
//            onAuthFailed(getItemsTask);
//            return;
//        }
//
//        apiTaskQueue.addTask(getItemsTask);
//
//        getItemsTask.execute();
//    }
//
//
//    // --------------------------------------------------------------------------- API COMMUNICATION
//
//    // TODO errors
//    @WorkerThread
//    public void onConnectionError(GetItemsTask apiTask) {
//        Timber.e("WTF2");
//    }
//
//    @WorkerThread
//    public void onApiError(ApiError apiError, GetItemsTask apiTask) {
//        Timber.e("WTF");
//    }
//
//    @WorkerThread
//    public void onItemsDownloaded(List<Item> items, PaginationData paginationData,
//                                  int priority, GetItemsTask getItemsTask) {
//        fireCallbacks(callbacks,
//                callback -> callback.onDataDownloaded(items, paginationData, priority),
//                mainThreader);
//    }
//
//    @WorkerThread
//    public void onAuthFailed(GetItemsTask getItemsTask) {
//        authHandler.onApiTaskAuthFailed(getItemsTask);
//    }
//
//
//    // ------------------------------------------------------------------------------- QUEUE CONTROL
//
//    /** Returns TRUE if there is a currently executing request with same or greater priority */
//    public boolean isDownloadingItems(final int priorityLevel) {
//        synchronized (apiTaskQueue) {
//            List<ApiTask> runningRequests = apiTaskQueue.getTasksForOperation(GetItemsTask.OP_NAME);
//            for(ApiTask request : runningRequests) {
//                if (request instanceof GetItemsTask &&
//                        ((GetItemsTask) request).getPriorityLevel() >= priorityLevel)
//                    return true;
//            }
//            return false;
//        }
//    }
//
//    public void cancelItemDownloads(int priorityLevel) {
//        synchronized (apiTaskQueue) {
//            List<ApiTask> requests = apiTaskQueue.getTasksForOperation(GetItemsTask.OP_NAME);
//            for(ApiTask request : requests) {
//                if (request instanceof GetItemsTask &&
//                        ((GetItemsTask) request).getPriorityLevel() == priorityLevel)
//                    request.cancelRequest();
//            }
//        }
//    }
//
//
//    // ------------------------------------------------------------------------------------- CLASSES
//
    @UiThread
    public interface Callback {
        // TODO mozda konkretniji callback? Also sta ces ako jedan ekran ima npr listanje a drugi
        // kreiranje i onda obe callback metode stavis i oba ekran moraju da implementiraju ih?
//        void onDataDownloaded(List<Item> items, PaginationData paginationData, int priority);
    }

}

package knez.assdroid.common.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import knez.assdroid.common.data.Identifiable;
import knez.assdroid.util.adapter.Filter;

public class IdentifiableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    @NonNull private List<Identifiable> allItems;
    @NonNull private List<Identifiable> shownItems;

    @Nullable private Filter filter;

    @NonNull private final Map<Class, AdapterPack> mappedClassesToPacks = new HashMap<>();
    @NonNull private final SparseArray<AdapterPack> mappedViewTypesToPacks = new SparseArray<>();
    @Nonnull private final Map<AdapterPack, Integer> mappedPacksToViewTypes = new HashMap<>();

    private int adapterPackTypeCounter = 1;

    public IdentifiableAdapter(Context context) {
        this.context = context;

        allItems = new ArrayList<>();
        shownItems = new ArrayList<>();

        setHasStableIds(true); //NOTE: Use DiffUtils
    }


    // ------------------------------------------------------------------------------- ADAPTER PACKS

    public void addAdapterPack(@NonNull final AdapterPack adapterPack) {
        if(mappedClassesToPacks.containsKey(adapterPack.getEntityClass()))
            throw new RuntimeException("Added two packs for the same entity class: "
                    + adapterPack.getEntityClass());

        mappedClassesToPacks.put(adapterPack.getEntityClass(), adapterPack);
        mappedViewTypesToPacks.put(adapterPackTypeCounter, adapterPack);
        adapterPackTypeCounter = adapterPackTypeCounter + 1;

        // Inverted map
        int size = mappedViewTypesToPacks.size();
        for(int i = 0; i < size; i++) {
            int key = mappedViewTypesToPacks.keyAt(i);
            AdapterPack value = mappedViewTypesToPacks.get(key);
            mappedPacksToViewTypes.put(value, key);
        }
    }

    public boolean hasPacks() {
        return mappedClassesToPacks.size() > 0;
    }

    public void removePacks() {
        mappedClassesToPacks.clear();
        mappedViewTypesToPacks.clear();
        mappedPacksToViewTypes.clear();
    }


    // -------------------------------------------------------- VIEW HOLDERS && ITEM TYPE && BINDING

    @Override
    public long getItemId(int position) {
        return allItems.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable entity = allItems.get(position);
        AdapterPack ap = getPackForClass(entity.getClass());
        return mappedPacksToViewTypes.get(ap);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mappedViewTypesToPacks.get(viewType).getLayoutItem(context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Identifiable currentEntity = allItems.get(position);
        getPackForClass(currentEntity.getClass()).showDataInLayoutItem(holder, currentEntity);
    }

    /**
     * Finds pack that corresponds to given class, or its closest superclass
     * @throws IllegalStateException - if no pack found for given class or any of its superclasses
     */
    @NonNull private AdapterPack getPackForClass(@NonNull Class clazz) {
        AdapterPack ap;
        Class classToIterate = clazz;
        while(classToIterate != null) {
            ap = mappedClassesToPacks.get(classToIterate);
            if(ap != null) return ap;

            classToIterate = classToIterate.getSuperclass();
        }

        throw new IllegalStateException("Couldn't find adapter pack for class " + clazz); // :(
    }


    // --------------------------------------------------------------------- GENERIC DATA OPERATIONS

    @NonNull public Identifiable getItem(final int position) {
        return shownItems.get(position);
    }

    @Override
    public int getItemCount() {
        return shownItems.size();
    }

    /** If filtering is turned on, returns the number of all items, unlike getItemCount */
    public int getAllItemsCount() { return allItems.size(); }

    @NonNull public List<Identifiable> getAllItems() { return allItems; }

    public void setItems(@NonNull final List<? extends Identifiable> items) {
        allItems.clear();
        allItems.addAll(items);

        shownItems = filterItems(allItems);

        notifyDataSetChanged();
    }

    public void setItemsDontNotify(@NonNull final List<? extends Identifiable> items) {
        allItems.clear();
        allItems.addAll(items);

        shownItems = filterItems(allItems);
    }

    public void addItems(@NonNull final List<? extends Identifiable> newItems) {
        if(newItems.size() == 0) return;

        allItems.addAll(newItems);

        int numOldShownItems = shownItems.size();
        shownItems.addAll(filterItems(newItems));
        notifyItemRangeChanged(numOldShownItems, newItems.size());
    }

    public int getItemPositionForId(final int id) {
        for (int i = 0; i < shownItems.size(); i++) {
            if(shownItems.get(i).getId() == id) return i;
        }
        return -1;
    }

    /** Returns TRUE if the item with the given id exists among all items (both filtered and not) */
    public boolean hasItemWithId(final int id) {
        for (int i = 0; i < allItems.size(); i++) {
            if(allItems.get(i).getId() == id) return true;
        }
        return false;
    }

    @Nullable public Identifiable getItemForId(final int itemId) {
        for(Identifiable existingItem : allItems) {
            if(existingItem.getId() == itemId) return existingItem;
        }
        return null;
    }

    public void clear() {
        if(allItems.size() == 0) return;

        allItems.clear();

        int numShownItems = shownItems.size();
        if(numShownItems == 0) return;

        shownItems.clear();
        notifyItemRangeRemoved(0, numShownItems);
    }


    // ----------------------------------------------------------------------------------- FILTERING

    public void setFilter(@Nullable final Filter filter) {
        if(filter == null) {
            removeFilter();
            return;
        }

        if(this.filter != null && this.filter.equals(filter)) return;

        this.filter = filter;
        setItems(allItems);
    }

    public void removeFilter() {
        if(filter == null) return;

        filter = null;
        setItems(allItems);
    }

    /** Returns a new list containing items that pass the filter */
    @NonNull private List<Identifiable> filterItems(
            @NonNull final List<? extends Identifiable> items) {
        if (filter == null) return new ArrayList<>(items);

        List<Identifiable> filteredItems = new ArrayList<>(items.size());
        for (Identifiable item : items) {
            if(filter.filter(item)) filteredItems.add(item);
        }
        return filteredItems;
    }

}
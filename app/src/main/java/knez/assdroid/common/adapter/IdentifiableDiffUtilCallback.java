package knez.assdroid.common.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.common.data.Identifiable;

public abstract class IdentifiableDiffUtilCallback extends DiffUtil.Callback {

    List<Identifiable> oldItems;
    List<Identifiable> newItems;

    IdentifiableDiffUtilCallback() { /* set data via interface*/ }

    public void setOldData(@NonNull List<Identifiable> oldData) {
        this.oldItems = new ArrayList<>();
        this.oldItems.addAll(oldData);
    }

    public void setNewData(@NonNull List<Identifiable> newData) {
        this.newItems = new ArrayList<>();
        this.newItems.addAll(newData);
    }

    @Override
    public int getOldListSize() {
        return oldItems.size();
    }
    @Override
    public int getNewListSize() {
        return newItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItems.get(oldItemPosition).equals(newItems.get(newItemPosition));
    }

}

package knez.assdroid.common.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import knez.assdroid.common.data.Identifiable;

public interface AdapterPack {
    @NonNull RecyclerView.ViewHolder getLayoutItem(@NonNull Context context);

    void showDataInLayoutItem(
            @NonNull RecyclerView.ViewHolder holder,
            @NonNull Identifiable currentEntity);

    @NonNull Class getEntityClass();
}

package knez.assdroid.common.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import knez.assdroid.common.data.Identifiable;

public interface AdapterPack {
    @NonNull RecyclerView.ViewHolder getLayoutItem(@NonNull Context context);

    void showDataInLayoutItem(
            @NonNull RecyclerView.ViewHolder holder,
            @NonNull Identifiable currentEntity);

    @NonNull Class getEntityClass();
}

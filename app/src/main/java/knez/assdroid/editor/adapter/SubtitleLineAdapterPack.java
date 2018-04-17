package knez.assdroid.editor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import knez.assdroid.common.adapter.AdapterPack;
import knez.assdroid.common.data.Identifiable;
import knez.assdroid.editor.gui.SubtitleLineLayoutItem;
import knez.assdroid.editor.vso.SubtitleLineVso;

public class SubtitleLineAdapterPack implements AdapterPack {

    private SubtitleLineLayoutItem.Callback listener;

    public SubtitleLineAdapterPack(@Nullable SubtitleLineLayoutItem.Callback listener) {
        this.listener = listener;
    }

    @Override @NonNull
    public RecyclerView.ViewHolder getLayoutItem(@NonNull Context context) {
        return new ViewHolderItemLayoutItem(new SubtitleLineLayoutItem(context), listener);
    }

    @Override
    public void showDataInLayoutItem(
            @NonNull RecyclerView.ViewHolder holder,
            @NonNull Identifiable currentEntity) {
        ((ViewHolderItemLayoutItem)holder).itemLayoutItem.showItem((SubtitleLineVso) currentEntity);
    }

    @Override
    @NonNull public Class getEntityClass() {
        return SubtitleLineVso.class;
    }

    private static class ViewHolderItemLayoutItem extends RecyclerView.ViewHolder {
        @NonNull private final SubtitleLineLayoutItem itemLayoutItem;
        private ViewHolderItemLayoutItem(
                @NonNull final SubtitleLineLayoutItem v,
                @NonNull final SubtitleLineLayoutItem.Callback listener) {
            super(v);
            itemLayoutItem = v;
            itemLayoutItem.setListener(listener);
        }
    }

}
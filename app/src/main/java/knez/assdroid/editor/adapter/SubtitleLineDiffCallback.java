package knez.assdroid.editor.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import knez.assdroid.editor.vso.SubtitleLineVso;

public class SubtitleLineDiffCallback extends DiffUtil.Callback {

    @NonNull private final List<SubtitleLineVso> oldList;
    @NonNull private final List<SubtitleLineVso> newList;

    public SubtitleLineDiffCallback(@NonNull List<SubtitleLineVso> oldList,
                                    @NonNull List<SubtitleLineVso> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
        SubtitleLineVso oldItem = oldList.get(oldItemPosition);
        SubtitleLineVso newItem = newList.get(newItemPosition);
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        SubtitleLineVso oldItem = oldList.get(oldItemPosition);
        SubtitleLineVso newItem = newList.get(newItemPosition);
        return oldItem.isIdenticalTo(newItem);
    }

}

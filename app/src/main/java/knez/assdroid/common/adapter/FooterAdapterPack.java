package knez.assdroid.common.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

// TODO: treba li mi ova klasa?

//public class FooterAdapterPack implements AdapterPack {
//
//    public FooterAdapterPack() { }
//
//    @NonNull @Override
//    public RecyclerView.ViewHolder getLayoutItem(@NonNull Context context) {
//        FooterLayoutItem view = new FooterLayoutItem(context);
//        ViewGroup.LayoutParams wlp = new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        view.setLayoutParams(wlp);
//        return new FooterViewHolder(view);
//    }
//
//    @Override
//    public void showDataInLayoutItem(@NonNull RecyclerView.ViewHolder holder,
//                                     @NonNull Identifiable currentEntity) {
//        // Note: no data in footer
//    }
//
//    @NonNull @Override
//    public Class getEntityClass() {
//        return Footer.class;
//    }
//
//    private class FooterViewHolder extends RecyclerView.ViewHolder {
//        final FooterLayoutItem footerItem;
//        private FooterViewHolder(FooterLayoutItem footerItem) {
//            super(footerItem);
//            this.footerItem = footerItem;
//        }
//    }
//
//}

package knez.assdroid.editor;

import android.support.annotation.NonNull;

interface EditorMVP {

    interface ViewInterface {
        // TODO
//        void showItems(@NonNull SolidList<ItemVso> filteredItemVsos);
//        void showItems(@NonNull SolidList<ItemVso> filteredItemVsos,
//                       @NonNull DiffUtil.DiffResult result);
    }

    interface PresenterInterface {
        void onAttach(@NonNull EditorMVP.ViewInterface viewInterface);
        void onDetach();
        void showItemDetails(int itemId);
        void onSearchSubmitted(@NonNull final String text);
    }

}

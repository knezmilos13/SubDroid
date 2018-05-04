package knez.assdroid.common.mvp;

import android.support.annotation.NonNull;

public interface CommonSubtitleMVP {

    interface ViewInterface {
        void showTitleUntitled(boolean currentSubtitleEdited);
        void showTitleForName(@NonNull String currentSubtitleFilename,
                              boolean currentSubtitleEdited);
    }

    interface PresenterInterface { // TODO
        void onAttach(@NonNull CommonSubtitleMVP.ViewInterface viewInterface);
        void onDetach();
    }

}

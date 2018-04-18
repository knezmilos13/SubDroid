package knez.assdroid.editor;

import android.net.Uri;
import android.support.annotation.NonNull;

interface EditorMVP {

    interface ViewInterface {
        void showTitleUntitled();
        void removeAllCurrentSubtitleData();
        void showTitleForFilename(@NonNull String currentSubtitleFilename,
                                  boolean currentSubtitleEdited);
        void showErrorLoadingSubtitleInvalidFormat(@NonNull String filename);
    }

    interface PresenterInterface {
        void onAttach(@NonNull EditorMVP.ViewInterface viewInterface);
        void onDetach();
        void onSearchSubmitted(@NonNull String text);
        void onFileSelectedForLoad(@NonNull Uri data, @NonNull String filename);
        void onShowHelpClicked();
        void onShowSettingsClicked();
    }

}

package knez.assdroid.common.mvp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface CommonSubtitleMVP {

    interface ViewInterface {
        void showTitleUntitled(boolean currentSubtitleEdited);
        void showTitleForName(@NonNull String currentSubtitleFilename,
                              boolean currentSubtitleEdited);
        void showSettingsScreen();
        void showHelpScreen();
        void showErrorWritingSubtitleInvalidFormat(@NonNull String filename);
    }

    interface PresenterInterface {
        void onAttach(@NonNull CommonSubtitleMVP.ViewInterface viewInterface);
        void onDetach();
        void onShowHelpClicked();
        void onShowSettingsClicked();
        @Nullable String getCurrentSubtitleName();
        void onFileSelectedForSaving(@NonNull Uri uri, @NonNull String filename);
    }

}

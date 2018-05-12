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
        void showProgressSavingFile();
        void hideProgress();
        void showFileSaveSelector();
    }

    interface PresenterInterface {
        void onShowHelpClicked();
        void onShowSettingsClicked();
        @Nullable String getCurrentSubtitleName();
        void onFileSelectedForSaveAs(@NonNull Uri uri, @NonNull String filename);
        void onSaveClicked();
    }

}

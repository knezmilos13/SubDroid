package knez.assdroid.common.mvp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;

public interface CommonSubtitleMVP {

    interface ViewInterface {
        void showTitleUntitled(boolean currentSubtitleEdited);
        void showTitleForName(@NonNull String name, boolean currentSubtitleEdited);

        void showSettingsScreen();
        void showHelpScreen();

        void showProgressSavingFile();
        void hideProgress();

        void showFileSaveSelector();

        void showErrorWritingSubtitleInvalidFormat(@NonNull String filename);
        void showErrorCantSaveMissingFile();
        void showErrorWritingFailed(@NonNull String destFilename);
    }

    interface PresenterInterface {
        void onShowHelpClicked();
        void onShowSettingsClicked();

        @Nullable String getCurrentSubtitleName();

        void onFileSelectedForSaveAs(@NonNull Uri uri);
        void onSaveClicked();

        void onSettingsChanged(@NonNull HashSet<String> changedSettings);
    }

}

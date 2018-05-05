package knez.assdroid.editor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import knez.assdroid.common.mvp.CommonSubtitleActivity;
import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.editor.vso.SubtitleLineVso;
import solid.collections.SolidList;

public interface EditorMVP {

    interface ViewInterface extends CommonSubtitleMVP.ViewInterface {
        void removeAllCurrentSubtitleData();
        void showErrorLoadingSubtitleInvalidFormat(@NonNull String filename);
        void showSubtitleLines(@NonNull SolidList<SubtitleLineVso> subtitleLineVsos);
        void showSubtitleLines(@NonNull SolidList<SubtitleLineVso> subtitleLineVsos,
                               @NonNull DiffUtil.DiffResult result);

        /** Should activate/deactivate appropriate controls in the UI. The actual presentation of
         *  subtitle lines according to settings is not done here. */
        void showCurrentSubtitleLineSettings(@NonNull SubtitleLineSettings subtitleLineSettings);

        void showErrorWritingSubtitleInvalidFormat(@NonNull String filename);

        void showSettingsScreen();
        void showHelpScreen();
        void showTranslatorScreen(long lineId);
    }

    interface PresenterInterface {
        void onAttach(@NonNull EditorMVP.ViewInterface viewInterface);
        void onDetach();
        void onSearchSubmitted(@NonNull String text);
        void onFileSelectedForLoad(@NonNull Uri uri, @NonNull String filename);
        void onShowHelpClicked();
        void onShowSettingsClicked();
        @Nullable String getCurrentSubtitleName();
        void onFileSelectedForSaving(@NonNull Uri uri, @NonNull String filename);
        void onSubtitleLineClicked(long id);
    }

}

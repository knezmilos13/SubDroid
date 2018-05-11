package knez.assdroid.editor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.editor.vso.SubtitleLineVso;
import solid.collections.SolidList;

public interface EditorMVP {

    interface ViewInterface extends CommonSubtitleMVP.ViewInterface {
        void removeAllCurrentSubtitleData();
        void showErrorLoadingSubtitleInvalidFormat(@NonNull String filename);
        void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos);
        void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos,
                               @NonNull DiffUtil.DiffResult result);

        /** Should activate/deactivate appropriate controls in the UI. The actual presentation of
         *  subtitle lines according to settings is not done here. */
        void showCurrentSubtitleLineSettings(@NonNull SubtitleLineSettings subtitleLineSettings);

        void showTranslatorScreen(long lineId);
        void updateSubtitleLines(@NonNull SolidList<SubtitleLineVso> editedVsos);
    }

    interface PresenterInterface extends CommonSubtitleMVP.PresenterInterface {
        void onAttach(@NonNull EditorMVP.ViewInterface viewInterface);
        void onDetach();
        void onSearchSubmitted(@NonNull String text);
        void onFileSelectedForLoad(@NonNull Uri uri, @NonNull String filename);
        void onSubtitleLineClicked(long id);
        void onSubtitleEditedExternally(@NonNull ArrayList<Integer> editedLineNumbers);
    }

}

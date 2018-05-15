package knez.assdroid.editor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.editor.vso.SubtitleLineVso;

public interface EditorMVP {

    interface ViewInterface extends CommonSubtitleMVP.ViewInterface {
        void removeAllCurrentSubtitleData();

        void showErrorLoadingSubtitleInvalidFormat(@NonNull String filename);
        void showErrorLoadingFailed(@NonNull String subtitleFilename);

        void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos);
        void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos,
                               @NonNull DiffUtil.DiffResult result);
        void updateSubtitleLines(@NonNull List<SubtitleLineVso> editedVsos);

        void showTranslatorScreen(long lineId);

        void showProgressLoadingFile();
    }

    interface PresenterInterface extends CommonSubtitleMVP.PresenterInterface {
        void onAttach(@NonNull EditorMVP.ViewInterface viewInterface);
        void onDetach();

        void onSearchSubmitted(@NonNull String text);

        void onFileSelectedForLoad(@NonNull Uri uri);

        void onSubtitleLineClicked(long id);

        void onSubtitleEditedExternally(@NonNull ArrayList<Long> editedLineIds);

        void onNewSubtitleRequested();
    }

}

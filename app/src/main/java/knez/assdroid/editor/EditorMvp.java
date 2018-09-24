package knez.assdroid.editor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.common.mvp.CommonSubtitleMvp;
import knez.assdroid.editor.vso.SubtitleLineVso;

public interface EditorMvp {

    interface ViewInterface extends CommonSubtitleMvp.ViewInterface {
        void showErrorLoadingSubtitleInvalidFormat(@NonNull String filename);
        void showErrorLoadingFailed(@NonNull String subtitleFilename);

        void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos);
        void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos,
                               @NonNull DiffUtil.DiffResult result);
        void updateSubtitleLines(@NonNull List<SubtitleLineVso> editedVsos);
        void updateSubtitleLines();
        void removeAllSubtitleLines();

        void showTranslatorScreen(long lineId);

        void showProgressLoadingFile();

        void showCurrentQuickSettings(
                boolean showTimings, boolean showActorStyle, boolean simplifyTags);

        void closeSearchSection();

        void showSearchSectionWithQuery(@NonNull String currentSearchQuery);

        void showSearchNumbers(int size, int currentItem);
        void hideSearchNumbers();

        void updateSubtitleLine(@NonNull SubtitleLineVso subtitleLineVso);

        void scrollToLine(@NonNull SubtitleLineVso subtitleLineVso);

        int getFirstShownLineNumber();
    }

    interface PresenterInterface extends CommonSubtitleMvp.PresenterInterface {
        void onAttach(@NonNull EditorMvp.ViewInterface viewInterface);
        void onDetach();

        void onSearchSubmitted(@NonNull String text);

        void onFileSelectedForLoad(@NonNull Uri uri);

        void onSubtitleLineClicked(long id);

        void onSubtitleEditedExternally(@NonNull ArrayList<Long> editedLineIds);

        void onNewSubtitleRequested();

        void onShowTimingsSettingChanged(boolean isChecked);
        void onShowActorStyleSettingChanged(boolean isChecked);
        void onSimplifyTagsSettingChanged(boolean isChecked);

        void onEndSearchRequested();
        void onPrevSearchResultRequested();
        void onNextSearchResultRequested();
    }

}

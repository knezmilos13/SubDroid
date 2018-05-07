package knez.assdroid.translator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import knez.assdroid.common.mvp.CommonSubtitleMVP;

public interface TranslatorMVP {

    interface ViewInterface extends CommonSubtitleMVP.ViewInterface {
        void closeScreen();
        void showSubtitleTexts(@NonNull String currentLineText, @Nullable String previousLineText,
                               @Nullable String nextLineText);
        void resetInputField(@NonNull String hint);
        void setInputText(@NonNull String text);
        @NonNull String getTranslationText();
        void showCurrentLineEdited(boolean currentLineEdited);
    }

    interface PresenterInterface {
        void onAttach(@NonNull ViewInterface viewInterface, long lineId, boolean hadChanges);
        void onDetach();
        void onPrevLineRequested();
        void onNextLineRequested();
        void onCommitRequested();
        void onCommitAndNextRequested();
        void onCopyCurrentLineToInputRequested();
        long getCurrentLineId();
        boolean hasHadChangesToSubtitleMade();
        void onTextChanged(@NonNull String text);
    }

}

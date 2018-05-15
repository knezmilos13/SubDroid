package knez.assdroid.translator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import knez.assdroid.common.mvp.CommonSubtitleMVP;

public interface TranslatorMVP {

    interface ViewInterface extends CommonSubtitleMVP.ViewInterface {
        void closeScreenNoSubtitle();
        void showSubtitleTexts(@NonNull String currentLineText, @Nullable String previousLineText,
                               @Nullable String nextLineText);
        void resetInputField(@NonNull String hint);
        void setInputText(@NonNull String text);
        @NonNull String getTranslationText();
        void showCurrentLineEdited(boolean currentLineEdited);
    }

    interface PresenterInterface extends CommonSubtitleMVP.PresenterInterface {
        void onAttach(@NonNull ViewInterface viewInterface);
        void onAttach(@NonNull ViewInterface viewInterface, long lineId);
        void onAttach(@NonNull ViewInterface viewInterface, @NonNull InternalState internalState);
        void onDetach();
        void onPrevLineRequested();
        void onNextLineRequested();
        void onCommitRequested();
        void onCommitAndNextRequested();
        void onCopyCurrentLineToInputRequested();
        int getCurrentLineNumber();
        Set<Long> getEditedLineIds();
        void onTextChanged(@NonNull String text);
        @NonNull InternalState getInternalState();
    }

    class InternalState implements Serializable {
        private final boolean currentLineHadUncommittedChanges;
        private final long currentLineId;
        private final HashSet<Long> editedLineIds;

        public InternalState(boolean currentLineHadUncommittedChanges,
                             long currentLineId, HashSet<Long> editedLineIds) {
            this.currentLineHadUncommittedChanges = currentLineHadUncommittedChanges;
            this.currentLineId = currentLineId;
            this.editedLineIds = editedLineIds;
        }
        public HashSet<Long> getEditedLineIds() { return editedLineIds; }
        public long getCurrentLineId() { return currentLineId; }
        public boolean isCurrentLineHadUncommittedChanges() { return currentLineHadUncommittedChanges; }
    }

}

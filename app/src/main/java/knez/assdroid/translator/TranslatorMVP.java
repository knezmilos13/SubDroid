package knez.assdroid.translator;

import android.support.annotation.NonNull;

public interface TranslatorMVP {

    interface ViewInterface {
    }

    interface PresenterInterface {
        void onAttach(@NonNull ViewInterface viewInterface, int lineId, boolean hadChanges);
        void onDetach();
        void onPrevLineRequested();
        void onNextLineRequested();
        void onCommitRequested();
        void onCommitAndNextRequested();
        void onCopyCurrentLineToInputRequested();
        int getCurrentLineId();
        boolean hasHadChangesToSubtitleMade();
    }

}

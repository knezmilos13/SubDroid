package knez.assdroid.translator;

import android.support.annotation.NonNull;

import knez.assdroid.common.mvp.CommonSubtitleMVP;

public interface TranslatorMVP {

    interface ViewInterface extends CommonSubtitleMVP.ViewInterface {
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

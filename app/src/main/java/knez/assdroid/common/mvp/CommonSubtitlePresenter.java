package knez.assdroid.common.mvp;

import android.support.annotation.NonNull;

import knez.assdroid.subtitle.data.SubtitleFile;

public class CommonSubtitlePresenter implements CommonSubtitleMVP.PresenterInterface {

    private CommonSubtitleMVP.ViewInterface viewInterface;

    @Override
    public void onAttach(@NonNull CommonSubtitleMVP.ViewInterface viewInterface) {
        this.viewInterface = viewInterface;
    }

    @Override
    public void onDetach() {
        viewInterface = null;
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    protected void showSubtitleTitle(@NonNull SubtitleFile subtitleFile) {
        if(viewInterface == null) return;

        if(subtitleFile.getName() != null)
            viewInterface.showTitleForName(
                    subtitleFile.getName(), subtitleFile.isEdited());
        else
            viewInterface.showTitleUntitled(subtitleFile.isEdited());
    }

}
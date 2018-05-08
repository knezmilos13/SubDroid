package knez.assdroid.common.mvp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleFile;

public class CommonSubtitlePresenter implements CommonSubtitleMVP.PresenterInterface {

    @NonNull private final SubtitleController subtitleController;

    private CommonSubtitleMVP.ViewInterface viewInterface;

    public CommonSubtitlePresenter(@NonNull SubtitleController subtitleController) {
        this.subtitleController = subtitleController;
    }

    @Override
    public void onAttach(@NonNull CommonSubtitleMVP.ViewInterface viewInterface) {
        this.viewInterface = viewInterface;
    }

    @Override
    public void onDetach() {
        viewInterface = null;
    }


    // ------------------------------------------------------------------------- PRESENTER INTERFACE

    @Override
    public void onShowSettingsClicked() {
        if(viewInterface == null) return;
        viewInterface.showSettingsScreen();
    }

    @Override
    public void onShowHelpClicked() {
        if(viewInterface == null) return;
        viewInterface.showHelpScreen();
    }

    @Override @Nullable
    public String getCurrentSubtitleName() {
        SubtitleFile currentSubtitleFile = subtitleController.getCurrentSubtitleFile();
        if(currentSubtitleFile == null) return null;
        else return currentSubtitleFile.getName();
    }

    @Override
    public void onFileSelectedForSaving(@NonNull Uri uri, @NonNull String filename) {
        String subtitleExtension = filename.substring(filename.lastIndexOf(".")+1);

        if(!subtitleController.canWriteSubtitle(subtitleExtension)) {
            viewInterface.showErrorWritingSubtitleInvalidFormat(filename);
            return;
        }

        subtitleController.writeSubtitle(uri);
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    protected void showSubtitleTitle(@Nullable SubtitleFile subtitleFile) {
        if(viewInterface == null) return;
        if(subtitleFile == null) return;

        if(subtitleFile.getName() != null)
            viewInterface.showTitleForName(
                    subtitleFile.getName(), subtitleFile.isEdited());
        else
            viewInterface.showTitleUntitled(subtitleFile.isEdited());
    }

}
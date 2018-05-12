package knez.assdroid.common.mvp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleFile;

public abstract class CommonSubtitlePresenter
        implements CommonSubtitleMVP.PresenterInterface, SubtitleController.Callback {

    @NonNull private final SubtitleController subtitleController;

    public CommonSubtitlePresenter(@NonNull SubtitleController subtitleController) {
        this.subtitleController = subtitleController;
    }

    @Nullable public abstract CommonSubtitleMVP.ViewInterface getViewInterface();


    // ------------------------------------------------------------------------- PRESENTER INTERFACE

    @Override
    public void onShowSettingsClicked() {
        if(getViewInterface() == null) return;
        getViewInterface().showSettingsScreen();
    }

    @Override
    public void onShowHelpClicked() {
        if(getViewInterface() == null) return;
        getViewInterface().showHelpScreen();
    }

    @Override @Nullable
    public String getCurrentSubtitleName() {
        SubtitleFile currentSubtitleFile = subtitleController.getCurrentSubtitleFile();
        if(currentSubtitleFile == null) return null;
        else return currentSubtitleFile.getName();
    }

    @Override
    public void onFileSelectedForSaveAs(@NonNull Uri uri, @NonNull String filename) {
        String subtitleExtension = filename.substring(filename.lastIndexOf(".")+1);

        if(!subtitleController.canWriteSubtitle(subtitleExtension)) {
            if(getViewInterface() != null)
                getViewInterface().showErrorWritingSubtitleInvalidFormat(filename);
            return;
        }

        getViewInterface().showProgressSavingFile();
        subtitleController.writeSubtitle(uri);
    }

    @Override
    public void onSaveClicked() {
        SubtitleFile currentSubtitleFile = subtitleController.getCurrentSubtitleFile();
        if(currentSubtitleFile == null) {
            // TODO neki error - nema fajla
            return;
        }

        // No name or extension? Must be a newly created file. Ask user to choose a location (save as)
        if(currentSubtitleFile.getName() == null || currentSubtitleFile.getExtension() == null
                || currentSubtitleFile.getUriPath() == null) {
            if(getViewInterface() != null) getViewInterface().showFileSaveSelector();
            return;
        }

        if(!subtitleController.canWriteSubtitle(currentSubtitleFile.getExtension())) {
            if(getViewInterface() != null)
                getViewInterface().showErrorWritingSubtitleInvalidFormat(
                        currentSubtitleFile.getName() + "." + currentSubtitleFile.getExtension());
            return;
        }

        if(getViewInterface() != null) getViewInterface().showProgressSavingFile();
        subtitleController.writeSubtitle(currentSubtitleFile.getUriPath());
    }


    // ------------------------------------------------------------------------------- REPO CALLBACK

    @Override
    public void onFileWritingFailed(@NonNull String destFilename) {
        // TODO show message
        if(getViewInterface() == null) return;
        getViewInterface().hideProgress();
    }

    @Override
    public void onSubtitleFileSaved(@NonNull SubtitleFile subtitleFile) {
        if(getViewInterface() == null) return;
        showSubtitleTitle(subtitleFile);
        getViewInterface().hideProgress();
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    protected void showSubtitleTitle(@Nullable SubtitleFile subtitleFile) {
        if(getViewInterface() == null) return;
        if(subtitleFile == null) return;

        if(subtitleFile.getName() != null)
            getViewInterface().showTitleForName(
                    subtitleFile.getName(), subtitleFile.isEdited());
        else
            getViewInterface().showTitleUntitled(subtitleFile.isEdited());
    }

}
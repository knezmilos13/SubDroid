package knez.assdroid.common.mvp;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;

import knez.assdroid.common.SharedPreferenceKey;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.apache.FilenameUtils;

public abstract class CommonSubtitlePresenter
        implements CommonSubtitleMvp.PresenterInterface, SubtitleController.Callback {

    @NonNull protected final SubtitleController subtitleController;
    @NonNull protected final FileHandler fileHandler;

    public CommonSubtitlePresenter(@NonNull SubtitleController subtitleController,
                                   @NonNull FileHandler fileHandler) {
        this.subtitleController = subtitleController;
        this.fileHandler = fileHandler;
    }

    @Nullable public abstract CommonSubtitleMvp.ViewInterface getViewInterface();


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
    public void onFileSelectedForSaveAs(@NonNull Uri uri) {
        String filename = fileHandler.getFileNameFromUri(uri);
        String subtitleExtension = FilenameUtils.getExtension(filename);

        if(subtitleExtension == null || !subtitleController.canWriteSubtitle(subtitleExtension)) {
            if(getViewInterface() != null)
                getViewInterface().showErrorWritingSubtitleInvalidFormat(filename);
            return;
        }

        fileHandler.takePermissionForUri(uri);

        if(getViewInterface() != null) getViewInterface().showProgressSavingFile();
        subtitleController.writeSubtitle(uri);
    }

    @Override
    public void onSaveClicked() {
        SubtitleFile currentSubtitleFile = subtitleController.getCurrentSubtitleFile();
        if(currentSubtitleFile == null) {
            CommonSubtitleMvp.ViewInterface viewInterface = getViewInterface();
            if(viewInterface != null) viewInterface.showErrorCantSaveMissingFile();
            return;
        }

        // No name or extension? Must be a newly created file. Ask user to choose a location (save as)
        if(currentSubtitleFile.getName() == null || currentSubtitleFile.getExtension() == null
                || currentSubtitleFile.getUriPath() == null) {
            if(getViewInterface() != null) getViewInterface().showFileSaveSelector();
            return;
        }

        if(!fileHandler.hasPermissionsToOpenUri(currentSubtitleFile.getUriPath())) {
            if(getViewInterface() != null) getViewInterface().showFileSaveSelector();
            return;
        }

        if(!subtitleController.canWriteSubtitle(currentSubtitleFile.getExtension())) {
            if(getViewInterface() != null)
                getViewInterface().showErrorWritingSubtitleInvalidFormat(
                        currentSubtitleFile.getName() + "." + currentSubtitleFile.getExtension());
            return;
        }

        // This mostly just refreshes the permission (which you got when loading file)
        fileHandler.takePermissionForUri(currentSubtitleFile.getUriPath());

        if(getViewInterface() != null) getViewInterface().showProgressSavingFile();
        subtitleController.writeSubtitle(currentSubtitleFile.getUriPath());
    }

    @Override
    public void onSettingsChanged(@NonNull HashSet<String> changedSettings) {
        if(changedSettings.contains(SharedPreferenceKey.THEME)) {
            if(getViewInterface() != null) getViewInterface().updateTheme();
            // Note: view will have to access current theme on its own because it must be done
            // before presenter is attached. So we pass nothing to view above.
        }
    }


    // ------------------------------------------------------------------------------- REPO CALLBACK

    @Override
    public void onFileWritingFailed(@NonNull String destFilename) {
        if(getViewInterface() == null) return;
        getViewInterface().showErrorWritingFailed(destFilename);
        getViewInterface().hideProgress();
    }

    @Override
    public void onSubtitleFileSaved(@NonNull SubtitleFile subtitleFile) {
        if(getViewInterface() == null) return;
        showSubtitleTitle(subtitleFile);
        getViewInterface().hideProgress();
    }

    @Override
    public void onInvalidSubtitleFormatForWriting(@NonNull String subtitleFilename) {
        if(getViewInterface() == null) return;
        getViewInterface().showErrorWritingSubtitleInvalidFormat(subtitleFilename);
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
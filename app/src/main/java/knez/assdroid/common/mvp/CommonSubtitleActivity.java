package knez.assdroid.common.mvp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.HashSet;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.android.AndroidInjection;
import knez.assdroid.R;
import knez.assdroid.help.KategorijeHelpaAktivnost;
import knez.assdroid.settings.SettingsActivity;
import knez.assdroid.util.gui.FadeAnimationHelper;
import timber.log.Timber;

public abstract class CommonSubtitleActivity extends CommonThemeableActivity
        implements CommonSubtitleMVP.ViewInterface {

    private static final int REQUEST_CODE_SETTINGS_ACTIVITY = 501;
    private static final int REQUEST_CODE_SAVE_SUBTITLE = 1235;

    @BindView(R.id.subtitle_processing_progress) protected View progressBar;
    @BindView(R.id.subtitle_processing_text) protected TextView progressLabel;

    @Inject protected Timber.Tree logger;

    protected abstract CommonSubtitleMVP.PresenterInterface getPresenter();


    // ------------------------------------------------------------------------ USER & SYSTEM EVENTS

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                getPresenter().onShowSettingsClicked();
                break;
            case R.id.menu_item_save:
                getPresenter().onSaveClicked();
                break;
            case R.id.menu_item_save_as:
                showFileSaveSelector();
                break;
            case R.id.menu_item_help:
                getPresenter().onShowHelpClicked();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_SAVE_SUBTITLE) {
            if (intent == null) return;

            Uri uri = intent.getData();
            if (uri == null) return;

            getPresenter().onFileSelectedForSaveAs(uri);
            return;
        }
        else if(requestCode == REQUEST_CODE_SETTINGS_ACTIVITY) {
            if (intent == null) return;
            HashSet<String> changedSettings
                    = (HashSet<String>) intent.getSerializableExtra(SettingsActivity.OUTPUT_CHANGED_SETTINGS);
            if(changedSettings.size() > 0)
                getPresenter().onSettingsChanged(changedSettings);
            return;
        }
    }


    // ------------------------------------------------------------------------------ VIEW INTERFACE

    @Override
    public void showTitleUntitled(boolean currentSubtitleEdited) {
	    ActionBar actionBar = getSupportActionBar();
	    if(actionBar == null) {
	        logger.w("Action bar missing! Not supposed to happen!");
	        return;
        }

        actionBar.setTitle(currentSubtitleEdited?
                R.string.common_strings_untitled_edited : R.string.common_strings_untitled);
    }

    @Override
    public void showTitleForName(@NonNull String name, boolean currentSubtitleEdited) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) {
            logger.w("Action bar missing! Not supposed to happen!");
            return;
        }

        if(currentSubtitleEdited)
            actionBar.setTitle(getString(R.string.common_strings_title_edited, name));
        else
            actionBar.setTitle(name);
    }

    @Override
    public void showSettingsScreen() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, REQUEST_CODE_SETTINGS_ACTIVITY);
    }

    @Override
    public void showHelpScreen() {
        Intent helpIntent = new Intent(this, KategorijeHelpaAktivnost.class); // TODO
        startActivity(helpIntent);
    }

    @Override
    public void showFileSaveSelector() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        String currentName = getPresenter().getCurrentSubtitleName();
        if(currentName == null) currentName = getString(R.string.common_strings_untitled);
        currentName += ".ass";

        intent.putExtra(Intent.EXTRA_TITLE, currentName);

        startActivityForResult(intent, REQUEST_CODE_SAVE_SUBTITLE);
    }

    @Override
    public void showProgressSavingFile() {
        progressLabel.setText(R.string.common_saving_file);
        FadeAnimationHelper.fadeView(true, progressBar, false);
    }

    @Override
    public void showErrorWritingSubtitleInvalidFormat(@NonNull String filename) {
        Toast.makeText(this, getString(R.string.common_error_writing_invalid_format, filename),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void showErrorCantSaveMissingFile() {
        Toast.makeText(this, R.string.common_error_writing_fail_missing_file, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showErrorWritingFailed(@NonNull String filename) {
        Toast.makeText(this, getString(R.string.common_error_writing_failed, filename),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateTheme() {
        ProcessPhoenix.triggerRebirth(this);
    }

}

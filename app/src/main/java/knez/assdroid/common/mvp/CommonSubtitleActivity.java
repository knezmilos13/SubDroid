package knez.assdroid.common.mvp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import knez.assdroid.App;
import knez.assdroid.R;
import knez.assdroid.help.KategorijeHelpaAktivnost;
import knez.assdroid.podesavanja.KategorijePodesavanjaAktivnost;
import knez.assdroid.util.AndroidUtil;
import timber.log.Timber;

public abstract class CommonSubtitleActivity extends AppCompatActivity
        implements CommonSubtitleMVP.ViewInterface {

    private static final int REQUEST_CODE_SETTINGS_ACTIVITY = 501;
    private static final int REQUEST_CODE_SAVE_SUBTITLE = 1235;

    protected Timber.Tree logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = App.getAppComponent().getLogger();
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//			case REQUEST_CODE_SETTINGS_ACTIVITY:
//				boolean izmenjen = primeniPerzistentnaPodesavanjaNaAdapter();
//				if(izmenjen) prevodAdapter.notifyDataSetChanged();
//				primeniPerzistentnaPodesavanjaNaKontrole();
//				primeniFullscreen(panelView.isFullscreenOn());
//				break;
// TODO

        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_SAVE_SUBTITLE) {
            if (data == null) return;

            Uri uri = data.getData();
            if (uri == null) return;

            String filename = AndroidUtil.getFileNameFromUri(this, uri);
            getPresenter().onFileSelectedForSaveAs(uri, filename);
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
    public void showTitleForName(@NonNull String currentSubtitleFilename,
                                 boolean currentSubtitleEdited) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) {
            logger.w("Action bar missing! Not supposed to happen!");
            return;
        }

        if(currentSubtitleEdited)
            actionBar.setTitle(getString(R.string.common_strings_title_edited, currentSubtitleFilename));
        else
            actionBar.setTitle(currentSubtitleFilename);
    }

    @Override
    public void showSettingsScreen() {
        Intent settingsIntent = new Intent(this, KategorijePodesavanjaAktivnost.class); // TODO
        startActivityForResult(settingsIntent, REQUEST_CODE_SETTINGS_ACTIVITY);
    }

    @Override
    public void showHelpScreen() {
        Intent helpIntent = new Intent(this, KategorijeHelpaAktivnost.class); // TODO
        startActivity(helpIntent);
    }

    @Override
    public void showErrorWritingSubtitleInvalidFormat(@NonNull String filename) {
        // TODO
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

}

package knez.assdroid.common.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import knez.assdroid.App;
import knez.assdroid.R;
import timber.log.Timber;

public abstract class CommonSubtitleActivity extends AppCompatActivity
        implements CommonSubtitleMVP.ViewInterface {

    protected Timber.Tree logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = App.getAppComponent().getLogger();
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

}

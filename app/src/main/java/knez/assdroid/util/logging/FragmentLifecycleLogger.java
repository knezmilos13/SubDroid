package knez.assdroid.util.logging;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import timber.log.Timber;

public class FragmentLifecycleLogger extends FragmentManager.FragmentLifecycleCallbacks {

    @Override
    public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
        Timber.v("Fragment %s attached", f);
    }

    @Override
    public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
        Timber.v("Fragment %s created", f);
    }

    @Override
    public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
        Timber.v("Fragment %s destroyed", f);
    }

    @Override
    public void onFragmentDetached(FragmentManager fm, Fragment f) {
        Timber.v("Fragment %s detached", f);
    }

    @Override
    public void onFragmentPaused(FragmentManager fm, Fragment f) {
        Timber.v("Fragment %s paused", f);
    }

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        Timber.v("Fragment %s resumed", f);
    }

    @Override
    public void onFragmentSaveInstanceState(FragmentManager fm, Fragment f, Bundle outState) {
        Timber.v("Fragment %s saving instance state", f);
    }

    @Override
    public void onFragmentStarted(FragmentManager fm, Fragment f) {
        Timber.v("Fragment %s started", f);
    }

    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        Timber.v("Fragment %s stopped", f);
    }

    @Override
    public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
        Timber.v("Fragment %s view created", f);
    }

    @Override
    public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
        Timber.v("Fragment %s view destroyed", f);
    }

}

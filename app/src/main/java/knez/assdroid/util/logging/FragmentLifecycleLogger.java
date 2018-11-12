package knez.assdroid.util.logging;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.View;

import timber.log.Timber;

public class FragmentLifecycleLogger extends FragmentManager.FragmentLifecycleCallbacks {

    @Override
    public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
        Timber.v("Fragment %s attached", f);
    }

    @Override
    public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, Bundle savedInstanceState) {
        Timber.v("Fragment %s created", f);
    }

    @Override
    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Timber.v("Fragment %s destroyed", f);
    }

    @Override
    public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Timber.v("Fragment %s detached", f);
    }

    @Override
    public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Timber.v("Fragment %s paused", f);
    }

    @Override
    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Timber.v("Fragment %s resumed", f);
    }

    @Override
    public void onFragmentSaveInstanceState(
            @NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Bundle outState) {
        Timber.v("Fragment %s saving instance state", f);
    }

    @Override
    public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Timber.v("Fragment %s started", f);
    }

    @Override
    public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Timber.v("Fragment %s stopped", f);
    }

    @Override
    public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f,
                                      @NonNull View v, Bundle savedInstanceState) {
        Timber.v("Fragment %s view created", f);
    }

    @Override
    public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Timber.v("Fragment %s view destroyed", f);
    }

}

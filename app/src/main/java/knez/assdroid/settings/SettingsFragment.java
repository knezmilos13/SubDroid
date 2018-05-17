package knez.assdroid.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import knez.assdroid.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
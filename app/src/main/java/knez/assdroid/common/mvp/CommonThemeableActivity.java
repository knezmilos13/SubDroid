package knez.assdroid.common.mvp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjection;
import knez.assdroid.Constants;
import knez.assdroid.R;
import knez.assdroid.util.preferences.StringPreference;

public abstract class CommonThemeableActivity extends AppCompatActivity {

    @Inject @Named("theme") protected StringPreference currentThemePreference;
    private String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        currentTheme = currentThemePreference.get();

        setTheme(currentThemePreference.get().equals(Constants.THEME_LIGHT)?
                R.style.SubDroid_Theme_Light : R.style.SubDroid_Theme_Dark);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // When you go into settings and then return
        if(!currentThemePreference.get().equals(currentTheme))
            recreate();
    }

}

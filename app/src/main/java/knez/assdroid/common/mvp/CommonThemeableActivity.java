package knez.assdroid.common.mvp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import knez.assdroid.App;
import knez.assdroid.Constants;
import knez.assdroid.R;

public abstract class CommonThemeableActivity extends AppCompatActivity {

    protected String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentTheme = getCurrentThemeFromPreferences();
        setTheme(currentTheme.equals(Constants.THEME_LIGHT)?
                R.style.SubDroid_Theme_Light : R.style.SubDroid_Theme_Dark);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // When you go into settings and then return
        if(!currentTheme.equals(getCurrentThemeFromPreferences()))
            recreate();
    }

    private String getCurrentThemeFromPreferences() {
        return App.getAppComponent().getThemePreference().get();
    }

}

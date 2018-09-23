package knez.assdroid.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import knez.assdroid.R;
import knez.assdroid.common.mvp.CommonThemeableActivity;

public class SettingsActivity extends CommonThemeableActivity {

    public static final String OUTPUT_CHANGED_SETTINGS = "changed_settings";

    @BindView(R.id.toolbar) protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        SettingsFragment f =
                (SettingsFragment) getFragmentManager().findFragmentById(R.id.settings_content);

        Intent output = new Intent();
        output.putExtra(OUTPUT_CHANGED_SETTINGS, f.getChangedSettings());
        setResult(RESULT_OK, output);
        finish();
    }

}

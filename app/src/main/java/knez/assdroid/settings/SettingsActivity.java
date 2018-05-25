package knez.assdroid.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String OUTPUT_CHANGED_SETTINGS = "changed_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        SettingsFragment f =
                (SettingsFragment) getFragmentManager().findFragmentById(android.R.id.content);

        Intent output = new Intent();
        output.putExtra(OUTPUT_CHANGED_SETTINGS, f.getChangedSettings());
        setResult(RESULT_OK, output);
        finish();
    }

}

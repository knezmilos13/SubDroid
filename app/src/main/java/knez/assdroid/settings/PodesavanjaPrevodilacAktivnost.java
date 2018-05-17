package knez.assdroid.settings;

import knez.assdroid.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PodesavanjaPrevodilacAktivnost extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.podesavanja_prevodilac);
	}
}

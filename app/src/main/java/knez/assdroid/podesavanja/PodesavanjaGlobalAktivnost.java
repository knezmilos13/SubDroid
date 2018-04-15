package knez.assdroid.podesavanja;

import knez.assdroid.R;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class PodesavanjaGlobalAktivnost extends PreferenceActivity {
	
	private Kontroler kontroler;
	private ListPreference prefFullscreenTip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.podesavanja_global);
		
	    kontroler = new Kontroler();

		pokupiPreference();
		pripremiSummarije();
		dodajListenere();
	}

	private void pokupiPreference() {
		prefFullscreenTip = (ListPreference) findPreference(PodesavanjaGlobalUtil.GLOBAL_FULLSCREEN_TIP);
	}

	private void dodajListenere() {
		prefFullscreenTip.setOnPreferenceChangeListener(kontroler);
	}

	private void pripremiSummarije() {		
		namestiSummaryTrenutnaVrednost(prefFullscreenTip, PodesavanjaGlobalUtil.getFullscreenTip());
	}
	
	private void namestiSummaryTrenutnaVrednost(Preference kome, String vrednost) {
		String summary = getResources().getString(R.string.podesavanja_global_trenutno_podesavanje) + " " + vrednost;
		kome.setSummary(summary);
	}

	private class Kontroler implements OnPreferenceChangeListener {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String svic = preference.getKey();
			if(svic.equals(PodesavanjaGlobalUtil.GLOBAL_FULLSCREEN_TIP)) {
				namestiSummaryTrenutnaVrednost(prefFullscreenTip, (String)newValue);
			} 
			return true;
		}
	}
}

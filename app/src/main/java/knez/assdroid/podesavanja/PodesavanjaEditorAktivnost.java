package knez.assdroid.podesavanja;

import knez.assdroid.R;
import knez.assdroid.util.gui.ResetEditTextPreference;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class PodesavanjaEditorAktivnost extends PreferenceActivity {

	private ResetEditTextPreference prefTagZnak;
	private ListPreference prefFontTekst, prefFontOstalo;
	private Kontroler kontroler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.podesavanja_editor);
		
		kontroler = new Kontroler();

		pokupiPreference();
		pripremiSummarije();
		dodajListenere();
	}

	private void pokupiPreference() {
		prefTagZnak = (ResetEditTextPreference) findPreference(PodesavanjaEditorUtil.EDITOR_MINIMIZED_TAG_CHAR);
		prefFontTekst = (ListPreference) findPreference(PodesavanjaEditorUtil.EDITOR_FONT_TEXT);
		prefFontOstalo = (ListPreference) findPreference(PodesavanjaEditorUtil.EDITOR_FONT_OSTALO);
	}

	private void dodajListenere() {
		prefTagZnak.setOnPreferenceChangeListener(kontroler);
		prefFontOstalo.setOnPreferenceChangeListener(kontroler);
		prefFontTekst.setOnPreferenceChangeListener(kontroler);
	}

	private void pripremiSummarije() {		
		namestiPrefTagSummary(PodesavanjaEditorUtil.getMinimizedCharTag());
		namestiSummaryTrenutnaVrednost(prefFontTekst, Integer.toString(PodesavanjaEditorUtil.getTextFontSize()));
		namestiSummaryTrenutnaVrednost(prefFontOstalo, Integer.toString(PodesavanjaEditorUtil.getOstaloFontSize()));
	}
	
	private void namestiPrefTagSummary(String znak) {
		String summary = getResources().getString(R.string.podesavanja_editor_znak_zamena_taga_summary);
		summary += ". " + getResources().getString(R.string.podesavanja_global_trenutno_podesavanje)+ " ";
		summary += znak;
		prefTagZnak.setSummary(summary);
	}
	private void namestiSummaryTrenutnaVrednost(Preference kome, String vrednost) {
		String summary = getResources().getString(R.string.podesavanja_global_trenutno_podesavanje) + " " + vrednost;
		kome.setSummary(summary);
	}

	private class Kontroler implements OnPreferenceChangeListener {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String svic = preference.getKey();
			if(svic.equals(PodesavanjaEditorUtil.EDITOR_MINIMIZED_TAG_CHAR)) {
				namestiPrefTagSummary((String)newValue);
			} else if(svic.equals(PodesavanjaEditorUtil.EDITOR_FONT_TEXT)) {
				namestiSummaryTrenutnaVrednost(prefFontTekst, (String)newValue);
			} else if(svic.equals(PodesavanjaEditorUtil.EDITOR_FONT_OSTALO)) {
				namestiSummaryTrenutnaVrednost(prefFontOstalo, (String)newValue);
			}
			return true;
		}
	}
}

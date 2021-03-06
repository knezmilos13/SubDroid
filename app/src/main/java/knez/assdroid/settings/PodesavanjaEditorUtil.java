package knez.assdroid.settings;

import knez.assdroid.R;
import knez.assdroid.App;
import android.content.Context;
import android.preference.PreferenceManager;

/** Za laksi/brzi pristup preferencama koje su vezane za preference activity */
public class PodesavanjaEditorUtil {
	
	public static final String EDITOR_MINIMIZED_TAG_CHAR = "editor_znak_minimizirani_tagovi";
	public static final String EDITOR_FONT_TEXT = "editor_font_text";
	public static final String EDITOR_FONT_OSTALO = "editor_font_ostalo";	
	
	/** Vraca znak koji ce se koristiti umesto tagova {}, tj. kada su "minimizirani" */
	public static String getMinimizedCharTag() {
		Context apkont = App.getAppComponent().getContext();
		return PreferenceManager.getDefaultSharedPreferences(apkont).getString(EDITOR_MINIMIZED_TAG_CHAR, 
			apkont.getResources().getString(R.string.settings_editor_tag_replacement_default_value));
	}
	
	/** Vraca velicinu fonta za tekst (u pikselima) */
	public static int getTextFontSize() {
		Context apkont = App.getAppComponent().getContext();
		String sVelicina = PreferenceManager.getDefaultSharedPreferences(apkont).getString(EDITOR_FONT_TEXT, null);
		
		int velicina;
		if(sVelicina == null) {
			velicina = Math.round(apkont.getResources().getDimension(R.dimen.subtitle_line_demo_text_font_size));
		} else {
			velicina = Integer.parseInt(sVelicina);
		}
		return velicina;
	}
	
	/** Vraca velicinu fonta za ostalo, sto nije tekst prevoda (u pikselima) */
	public static int getOstaloFontSize() {
		Context apkont = App.getAppComponent().getContext();
		String sVelicina = PreferenceManager.getDefaultSharedPreferences(apkont).getString(EDITOR_FONT_OSTALO, null);
		
		int velicina;
		if(sVelicina == null) {
			velicina = Math.round(apkont.getResources().getDimension(R.dimen.subtitle_line_demo_other_font_size));
		} else {
			velicina = Integer.parseInt(sVelicina);
		}
		return velicina;
	}

}

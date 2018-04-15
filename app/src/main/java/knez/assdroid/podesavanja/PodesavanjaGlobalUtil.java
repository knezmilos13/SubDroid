package knez.assdroid.podesavanja;

import knez.assdroid.R;
import knez.assdroid.util.Aplikacija;
import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/** Za laksi/brzi pristup preferencama koje su vezane za preference activity */
public class PodesavanjaGlobalUtil {

	public static final String GLOBAL_FULLSCREEN_TIP = "tip_fullscreena";

	/** Da li u fullscreenu treba da se sakrije title bar */
	public static boolean isHideTitleBar() {
		Context apkont = Aplikacija.dajKontekst();
		Resources r = apkont.getResources();
		String podesavanje = PreferenceManager.getDefaultSharedPreferences(apkont)
				.getString(GLOBAL_FULLSCREEN_TIP, r.getString(R.string.podesavanja_global_fullscreen_tip_default));
		return podesavanje.equals(r.getString(R.string.podesavanja_global_fullscreen_tip_title_bar))
				|| podesavanje.equals(r.getString(R.string.podesavanja_global_fullscreen_tip_both));
	}
	
	/** Da li u fullscreenu treba da se sakrije status bar */
	public static boolean isHideStatusBar() {
		Context apkont = Aplikacija.dajKontekst();
		Resources r = apkont.getResources();
		String podesavanje = PreferenceManager.getDefaultSharedPreferences(apkont)
				.getString(GLOBAL_FULLSCREEN_TIP, r.getString(R.string.podesavanja_global_fullscreen_tip_default));
		return podesavanje.equals(r.getString(R.string.podesavanja_global_fullscreen_tip_status_bar))
				|| podesavanje.equals(r.getString(R.string.podesavanja_global_fullscreen_tip_both));
	}
	
	/** Vraca trenutno podesavanje nacina funkcionisanja fullscreen-a */
	public static String getFullscreenTip() {
		Context apkont = Aplikacija.dajKontekst();
		return PreferenceManager.getDefaultSharedPreferences(apkont)
				.getString(GLOBAL_FULLSCREEN_TIP, 
						apkont.getResources().getString(R.string.podesavanja_global_fullscreen_tip_default));
	}

}

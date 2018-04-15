package knez.assdroid.podesavanja;

import knez.assdroid.R;
import knez.assdroid.util.Aplikacija;
import android.content.Context;
import android.preference.PreferenceManager;

/** Za laksi/brzi pristup preferencama koje su vezane za preference activity */
public class PodesavanjaPrevodilacUtil {

	private static final String TRANSLATOR_USE_HINT = "translator_use_hint";
	private static final String TRANSLATOR_COPY_LINE = "translator_always_copy_original";
	private static final String TRANSLATOR_EMPTY_COMMIT = "translator_commiting_empty_line";

	/** Da li se u polju za unos prevoda ispisuje hint sa sadrzajem originala koji se prevodi. */
	public static boolean isPrevodilacHintOn() {
		Context apkont = Aplikacija.dajKontekst();
		return PreferenceManager.getDefaultSharedPreferences(apkont)
				.getBoolean(TRANSLATOR_USE_HINT, 
						apkont.getResources().getBoolean(R.bool.podesavanja_translator_use_hint_default));
	}

	/** Da li se svaka linija automatski ispisuje u tekstualno polje */
	public static boolean isAlwaysCopyOn() {
		Context apkont = Aplikacija.dajKontekst();
		return PreferenceManager.getDefaultSharedPreferences(apkont)
				.getBoolean(TRANSLATOR_COPY_LINE, 
						apkont.getResources().getBoolean(R.bool.podesavanja_translator_copy_line_default));
	}

	/** Da li commit zadrazava originalnu liniju. */
	public static boolean isCommitKeepOriginalOn() {
		Context apkont = Aplikacija.dajKontekst();
		return PreferenceManager.getDefaultSharedPreferences(apkont)
				.getBoolean(TRANSLATOR_EMPTY_COMMIT, 
						apkont.getResources().getBoolean(R.bool.podesavanja_translator_empty_commit_default));
	}

}

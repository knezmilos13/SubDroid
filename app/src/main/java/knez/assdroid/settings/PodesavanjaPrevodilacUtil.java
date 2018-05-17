package knez.assdroid.settings;

import knez.assdroid.App;
import android.content.Context;
import android.preference.PreferenceManager;

/** Za laksi/brzi pristup preferencama koje su vezane za preference activity */
public class PodesavanjaPrevodilacUtil {

	private static final String TRANSLATOR_USE_HINT = "translator_use_hint";
	private static final String TRANSLATOR_COPY_LINE = "translator_always_copy_original";
	private static final String TRANSLATOR_EMPTY_COMMIT = "translator_commiting_empty_line";

	/** Da li se u polju za unos prevoda ispisuje hint sa sadrzajem originala koji se prevodi. */
	public static boolean isPrevodilacHintOn() {
		Context apkont = App.getAppComponent().getContext();
		return PreferenceManager.getDefaultSharedPreferences(apkont)
				.getBoolean(TRANSLATOR_USE_HINT, true);
	}

	/** Da li se svaka linija automatski ispisuje u tekstualno polje */
	public static boolean isAlwaysCopyOn() {
		Context apkont = App.getAppComponent().getContext();
		return PreferenceManager.getDefaultSharedPreferences(apkont)
				.getBoolean(TRANSLATOR_COPY_LINE, false);
	}

	/** Da li commit zadrazava originalnu liniju. */
	public static boolean isCommitKeepOriginalOn() {
		Context apkont = App.getAppComponent().getContext();
		return PreferenceManager.getDefaultSharedPreferences(apkont)
				.getBoolean(TRANSLATOR_EMPTY_COMMIT, false);
	}

}

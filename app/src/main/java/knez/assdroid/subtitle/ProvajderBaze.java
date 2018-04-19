package knez.assdroid.subtitle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProvajderBaze extends SQLiteOpenHelper {
	private static final int VERZIJA_BAZE = 2;
	public static final String IME_BAZE = "assDroid.db";
	private Context kontekst;
	
	private static final String[] UPITI_CREATE_TABELE = { RedPrevoda.SEMA, RedStila.SEMA, RedZaglavlja.SEMA };

	public ProvajderBaze(Context kontekst) { 
		super(kontekst, IME_BAZE, null, VERZIJA_BAZE);
		this.kontekst = kontekst;
	}

	@Override
	public void onCreate(SQLiteDatabase db) { 
		try {
			for(String upit : UPITI_CREATE_TABELE) {
				db.execSQL(upit);
			}	
		} catch (Exception e) {
			e.printStackTrace();
//			Loger.log(e);
			kontekst.deleteDatabase(IME_BAZE);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		kontekst.deleteDatabase(IME_BAZE);
	}
}
package knez.assdroid.logika;

import android.content.ContentValues;
import android.database.Cursor;

public class RedZaglavlja {
	
	public Integer id = null;
	public String sadrzaj = null;
	
	public RedZaglavlja() {}
	public RedZaglavlja(String sadrzaj) {
		this.sadrzaj = sadrzaj;
	}
	public RedZaglavlja(int id, String sadrzaj) {
		this.id = id;
		this.sadrzaj = sadrzaj;
	}

	public static final String IME_TABELE = "red_zaglavlja";
	public static final String K_ID = "_id";
	public static final String K_SADRZAJ = "sadrzaj";
	public static final String SEMA = 
			"CREATE TABLE " + IME_TABELE + " (" 
			+ K_ID + " INTEGER PRIMARY KEY,"
			+ K_SADRZAJ + " TEXT);";

	public ContentValues dajVrednostiZaBazu() {
		ContentValues cv = new ContentValues();
		cv.put(K_SADRZAJ, sadrzaj);
		return cv;
	}
	
	public static RedZaglavlja kreirajIzKursora(Cursor kurs) {
		if(!indeksiSetovani) setujIndekse(kurs);
		RedZaglavlja red = new RedZaglavlja();
		red.id = kurs.getInt(indexID);
		red.sadrzaj = kurs.getString(indexSadrzaj);
		return red;
	}

	private static int indexID, indexSadrzaj;
	private static boolean indeksiSetovani = false;
	public static void setujIndekse(Cursor kursor) {
		indexID = kursor.getColumnIndex(K_ID);
		indexSadrzaj = kursor.getColumnIndex(K_SADRZAJ);
		indeksiSetovani = true;
	}
	
	public static String kreirajStringIzKursora(Cursor kurs) {
		if(!indeksiSetovani) setujIndekse(kurs);
		return kurs.getString(indexSadrzaj);
	}
}

package knez.assdroid.logika;

import android.content.ContentValues;
import android.database.Cursor;

// TODO: ovo u neki data odeljak i prevedi ga
public class RedPrevoda {
	
	public Integer id = null;
	public int lineNumber, layer, marginL, marginV, marginR;
	public long start, end;
	public String style, actorName, effect, text;
	public boolean komentar = false;
	
	public RedPrevoda() {}
	
	public ContentValues dajVrednostiZaBazu() {
		ContentValues cv = new ContentValues();
		cv.put(K_LINE, lineNumber);
		cv.put(K_LAYER, layer);
		cv.put(K_MARGIN_L, marginL);
		cv.put(K_MARGIN_V, marginV);
		cv.put(K_MARGIN_R, marginR);
		cv.put(K_START, start);
		cv.put(K_END, end);
		cv.put(K_STYLE, style);
		cv.put(K_ACTOR_NAME, actorName);
		cv.put(K_EFFECT, effect);
		cv.put(K_TEXT, text);
		cv.put(K_KOMENTAR, komentar? 1 : 0);
		return cv;
	}

	public static final String IME_TABELE = "red_prevoda";
	public static final String K_ID = "_id";
	public static final String K_LINE = "line_number";
	public static final String K_LAYER = "layer";
	public static final String K_MARGIN_L = "margin_left";
	public static final String K_MARGIN_V = "margin_vertical";
	public static final String K_MARGIN_R = "margin_right";
	public static final String K_START = "start_time";
	public static final String K_END = "end_time";
	public static final String K_STYLE = "style";
	public static final String K_ACTOR_NAME = "actor_name";
	public static final String K_EFFECT = "effect";
	public static final String K_TEXT = "text";
	public static final String K_KOMENTAR = "comment";
	public static final String SEMA = 
			"CREATE TABLE " + IME_TABELE + " (" 
			+ K_ID + " INTEGER PRIMARY KEY,"
			+ K_LINE + " INTEGER,"
			+ K_LAYER + " INTEGER DEFAULT 0,"
			+ K_MARGIN_L + " INTEGER DEFAULT 0,"
			+ K_MARGIN_R + " INTEGER DEFAULT 0,"
			+ K_MARGIN_V + " INTEGER DEFAULT 0,"
			+ K_START + " INTEGER DEFAULT 0,"
			+ K_END + " INTEGER DEFAULT 0,"
			+ K_STYLE + " TEXT," //TODO integer, referenca na id iz tabele stilovi
			+ K_ACTOR_NAME + " TEXT,"
			+ K_EFFECT + " TEXT,"
			+ K_TEXT + " TEXT,"
			+ K_KOMENTAR + " INTEGER DEFAULT 0);";
	
	
	public static RedPrevoda kreirajIzKursora(Cursor kurs) {
		if(!indeksiSetovani) setujIndekse(kurs);
		RedPrevoda red = new RedPrevoda();
		red.id = kurs.getInt(indexID);
		red.lineNumber = kurs.getInt(indexLine);
		red.start = kurs.getInt(indexStart);
		red.end = kurs.getInt(indexEnd);
		red.style = kurs.getString(indexStyle);
		red.actorName = kurs.getString(indexActor);
		red.layer = kurs.getInt(indexLayer);
		red.marginL = kurs.getInt(indexMarginL);
		red.marginR = kurs.getInt(indexMarginR);
		red.marginV = kurs.getInt(indexMarginV);
		red.text = kurs.getString(indexText);
		red.effect = kurs.getString(indexEffect);
		red.komentar = kurs.getInt(indexKomentar) == 1;
		return red;
	}

	private static int indexID, indexLine, indexLayer, indexMarginL, indexMarginR, indexMarginV, indexStart,
	indexEnd, indexStyle, indexActor, indexEffect, indexText, indexKomentar;
	private static boolean indeksiSetovani = false;
	public static void setujIndekse(Cursor kursor) {
		if(indeksiSetovani) {
			return;
		}
		indexID = kursor.getColumnIndex(K_ID);
		indexLine = kursor.getColumnIndex(K_LINE);
		indexLayer = kursor.getColumnIndex(K_LAYER);
		indexMarginL = kursor.getColumnIndex(K_MARGIN_L);
		indexMarginR = kursor.getColumnIndex(K_MARGIN_R);
		indexMarginV = kursor.getColumnIndex(K_MARGIN_V);
		indexStart = kursor.getColumnIndex(K_START);
		indexEnd = kursor.getColumnIndex(K_END);
		indexStyle = kursor.getColumnIndex(K_STYLE);
		indexActor = kursor.getColumnIndex(K_ACTOR_NAME);
		indexEffect = kursor.getColumnIndex(K_EFFECT);
		indexText = kursor.getColumnIndex(K_TEXT);
		indexKomentar = kursor.getColumnIndex(K_KOMENTAR);
		indeksiSetovani = true;
	}
}

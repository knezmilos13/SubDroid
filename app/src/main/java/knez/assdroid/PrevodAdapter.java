package knez.assdroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import knez.assdroid.logika.RedPrevoda;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.CursorAdapter;

/*
public class PrevodAdapter extends CursorAdapter {

	private boolean matchCaseTrazenog = false;
	private String trazeniTekst = "";
	private Pattern trazeniPattern = null;

	public PrevodAdapter(Context context, Cursor kursor) {
		super(context, kursor, false);
		defaultBojaPozadine = context.getResources().getColor(R.color.global_providna);
	}

// TODO za boju pozadine R.drawable.prevod_pozadina_komentar ili R.color.global_providna

	public void setTrazeniTekst(String trazeniTekst, boolean matchCase) {
		this.trazeniTekst = trazeniTekst;
		this.matchCaseTrazenog = matchCase;
		trazeniPattern = Pattern.compile((matchCaseTrazenog ? "" : "(?i)") + trazeniTekst);
	}
	public void clearTrazeniTekst() {
		this.trazeniTekst = null;
	}
	public String getTrazeniTekst() {
		return trazeniTekst;
	}



	*/
/** Hajlajtuje prosledjeni tekst u tekucoj liniji prevoda. Ne mora da znaci da ce ga biti,
	 * ako se zove SEARCH u vecini redova ga nece biti, a ako se zove FILTER, onda ce ga uglavnom
	 * biti osim kad se trazeno sprcka u tagu koji je saktiven. *//*

	private CharSequence hajlajtujTekst(String tekst) {
		Matcher m = trazeniPattern.matcher(tekst);

		SpannableStringBuilder ofarban = new SpannableStringBuilder(tekst);
		while(m.find()) {
			ofarban.setSpan(new BackgroundColorSpan(Color.WHITE), m.start(), m.end(), 0);
			ofarban.setSpan(new ForegroundColorSpan(Color.BLACK), m.start(), m.end(), 0);
		}
		return ofarban;
	}

	*/
/** Vraca poziciju elementa sa zadatim lineNumber-om u listi. Ako ga nema, vraca -1. *//*

	public int dajPozicijuZaLineNumber(int lineNumber) {
		int start, end, midPt, tekuciLN;
		start = 0;
		end = getCursor().getCount() - 1;
		int indeksLineNumber = getCursor().getColumnIndex(RedPrevoda.K_LINE);
		while (start <= end) {
			midPt = (start + end) / 2;
			getCursor().moveToPosition(midPt);
			tekuciLN = getCursor().getInt(indeksLineNumber);
			if (tekuciLN == lineNumber) {
				return midPt;
			} else if (tekuciLN < lineNumber) {
				start = midPt + 1;
			} else {
				end = midPt - 1;
			}
		}
		return -1;
	}
}*/

package knez.assdroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import knez.assdroid.logika.RedPrevoda;
import knez.assdroid.util.Alatke;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class PrevodAdapter extends CursorAdapter {

	public static final int FONT_SIZE_NEDEFINISAN = -1;
	
	private static final int DEFAULT_LAYOUT_STAVKI = R.layout.stavka_prevod;
	private boolean prikaziPrviRed = true;
	private boolean prikaziDrugiRed = true;
	private boolean prikaziTagove = false;
	private String tagZamena = "";
	private int tekstSize = FONT_SIZE_NEDEFINISAN;
	private int ostaloSize = FONT_SIZE_NEDEFINISAN;
	private boolean matchCaseTrazenog = false;
	private String trazeniTekst = "";
	private Pattern trazeniPattern = null;
	private int defaultBojaPozadine;

	public PrevodAdapter(Context context, Cursor kursor) {
		super(context, kursor, false);
		defaultBojaPozadine = context.getResources().getColor(R.color.global_providna);
	}

	public void setPrikaziDrugiRed(boolean prikaziDrugiRed) {
		this.prikaziDrugiRed = prikaziDrugiRed;
	}
	public void setPrikaziPrviRed(boolean prikaziPrviRed) {
		this.prikaziPrviRed = prikaziPrviRed;
	}
	public void setPrikaziTagove(boolean prikaziTagove) {
		this.prikaziTagove = prikaziTagove;
	}
	public String getTagZamena() {
		return tagZamena;
	}
	public void setTagZamena(String tagZamena) {
		this.tagZamena = tagZamena;
	}
	public int getTekstSize() {
		return tekstSize;
	}
	public int getOstaloSize() {
		return ostaloSize;
	}
	public void setTekstSize(int tekstSize) {
		this.tekstSize = tekstSize;
	}
	public void setOstaloSize(int ostaloSize) {
		this.ostaloSize = ostaloSize;
	}
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

	private class Holder {
		TextView redniBroj1, redniBroj2, redniBroj3, vremeOd, vremeDo, actor, style, tekst;
		TextView labelaCrtica, labelaStil, labelaActor;
		View prviRed, drugiRed;
		View koren;
		
		Holder(View koren) {
			this.koren = koren;
			prviRed = koren.findViewById(R.id.stavka_prevod_prvi_red);
			drugiRed = koren.findViewById(R.id.stavka_prevod_drugi_red);
			redniBroj1 = koren.findViewById(R.id.stavka_prevod_redni_broj_1);
			redniBroj2 = koren.findViewById(R.id.stavka_prevod_redni_broj_2);
			redniBroj3 = koren.findViewById(R.id.stavka_prevod_redni_broj_3);
			vremeOd = koren.findViewById(R.id.stavka_prevod_vreme_od);
			labelaCrtica = koren.findViewById(R.id.stavka_prevod_vreme_crtica);
			vremeDo = koren.findViewById(R.id.stavka_prevod_vreme_do);
			labelaActor = koren.findViewById(R.id.stavka_prevod_tekst_actor);
			actor = koren.findViewById(R.id.stavka_prevod_actor);
			labelaStil = koren.findViewById(R.id.stavka_prevod_tekst_stil);
			style = koren.findViewById(R.id.stavka_prevod_stil);
			tekst = koren.findViewById(R.id.stavka_prevod_tekst);
		}
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag();
		RedPrevoda item = RedPrevoda.kreirajIzKursora(cursor);

		srediPrikazPrvogReda(holder, item);
		srediPrikazDrugogReda(holder, item);
		srediPrikazTrecegReda(holder, item);
		
		primeniFontSize(holder);
		srediPozadinu(holder.koren, item.komentar);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater vi = LayoutInflater.from(context);
		View v = vi.inflate(DEFAULT_LAYOUT_STAVKI, null);
		Holder holder = new Holder(v);
		v.setTag(holder);
		return v;
	}

	/** Prikazuje ili sakriva prvi od tri reda koji sadrze podatke o prevodu (konkretno, tajming). */
	private void srediPrikazPrvogReda(Holder holder, RedPrevoda item) {
		if(prikaziPrviRed) {
			holder.prviRed.setVisibility(View.VISIBLE);
			holder.redniBroj1.setVisibility(View.VISIBLE);
			holder.redniBroj1.setText("#" + item.lineNumber);
			holder.vremeOd.setText(Alatke.formatirajVreme(item.start));
			holder.vremeDo.setText(Alatke.formatirajVreme(item.end));
		}  else {
			holder.prviRed.setVisibility(View.GONE);
			holder.redniBroj1.setVisibility(View.GONE);
		}
	}
	
	/** Prikazuje ili sakriva drugi od tri reda koji sadrze podatke o prevodu (konkretno, stil/akter). */
	private void srediPrikazDrugogReda(Holder holder, RedPrevoda item) {
		if(prikaziDrugiRed) {
			holder.drugiRed.setVisibility(View.VISIBLE);
			holder.actor.setText(item.actorName);
			holder.style.setText(item.style);
			if(!prikaziPrviRed) {
				holder.redniBroj2.setText("#" + item.lineNumber);
				holder.redniBroj2.setVisibility(View.VISIBLE);
			} else {
				holder.redniBroj2.setVisibility(View.GONE);
			}
		} else {
			holder.drugiRed.setVisibility(View.GONE);
		}
	}
	
	/** Sredjuje prikaz reda sa tekstom - da li se prikazuju tagovi, da li se hajlajtuje neki trazeni izraz
	 *  i da li se prikazuje redni broj u trecem redu (ili je vec prikazan u prvom/drugom) */
	private void srediPrikazTrecegReda(Holder holder, RedPrevoda item) {
		String tekstZaPrikaz = item.text;
		if(!prikaziTagove)
			tekstZaPrikaz = Alatke.izbaciTagove(tekstZaPrikaz, tagZamena);
		
		if(trazeniTekst != null && !trazeniTekst.equals("")) {
			holder.tekst.setText(hajlajtujTekst(tekstZaPrikaz));
		} else {
			holder.tekst.setText(tekstZaPrikaz);
		}
		
		if(!prikaziPrviRed && !prikaziDrugiRed) {
			holder.redniBroj3.setVisibility(View.VISIBLE);
			holder.redniBroj3.setText("#" + item.lineNumber);
		} else {
			holder.redniBroj3.setVisibility(View.GONE);
		}
	}
	
	/** Hajlajtuje prosledjeni tekst u tekucoj liniji prevoda. Ne mora da znaci da ce ga biti,
	 * ako se zove SEARCH u vecini redova ga nece biti, a ako se zove FILTER, onda ce ga uglavnom
	 * biti osim kad se trazeno sprcka u tagu koji je saktiven. */
	private CharSequence hajlajtujTekst(String tekst) {
		Matcher m = trazeniPattern.matcher(tekst);
		
		SpannableStringBuilder ofarban = new SpannableStringBuilder(tekst);
		while(m.find()) {
			ofarban.setSpan(new BackgroundColorSpan(Color.WHITE), m.start(), m.end(), 0);
			ofarban.setSpan(new ForegroundColorSpan(Color.BLACK), m.start(), m.end(), 0);
		}
		return ofarban;
	}
	
	/** Primenjuje zadate velicine fonta teksta i ostalih linija na njih. */
	private void primeniFontSize(Holder holder) {
		if(ostaloSize != FONT_SIZE_NEDEFINISAN) {
			TextView tv[] = { holder.redniBroj1,  holder.redniBroj2,  holder.redniBroj3,  holder.vremeOd,
				 holder.vremeDo,  holder.actor,  holder.style,  holder.labelaCrtica, 
				 holder.labelaStil,  holder.labelaActor };
		for(TextView polje : tv)
			polje.setTextSize(TypedValue.COMPLEX_UNIT_SP, ostaloSize);
		}
		if(tekstSize != FONT_SIZE_NEDEFINISAN) {
			holder.tekst.setTextSize(TypedValue.COMPLEX_UNIT_SP, tekstSize);
		}
	}
	
	/** Namenjen izmeni pozadine korenog viewa zavisno od toga da li je komentar ili ne (komentar treba
	 * da bude obojen u nekoj boji da se razlikuje od ostalih klasicnih linija) */
	private void srediPozadinu(View pogled, boolean jelKomentar) {
		if(jelKomentar)
			pogled.setBackgroundResource(R.drawable.prevod_pozadina_komentar);
		else
			pogled.setBackgroundColor(defaultBojaPozadine);
	}

	/** Vraca poziciju elementa sa zadatim lineNumber-om u listi. Ako ga nema, vraca -1. */
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
}
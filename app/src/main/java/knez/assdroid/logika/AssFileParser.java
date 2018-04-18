package knez.assdroid.logika;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import knez.assdroid.R;
import knez.assdroid.util.Alatke;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.SparseArray;

public class AssFileParser implements Parser {

	@NonNull private final Context context;

	private enum Sekcija { INFO, STYLE, PREVOD, NEPOZNATA }

	private ParserCallback kolbek;
	private Sekcija tekucaSekcija = null;
	@SuppressWarnings("rawtypes")
	private List lista;

	private SparseArray<Object> mapaProblema;
	private String warnString = "";
	private static final int PROB_FORMAT_PREVODA = 1;
	private static final int PROB_PARSIRANJE_PREVODA = 2;
	private static final int PROB_NEPOZNAT_RED_PREVODA = 3;
	private static final int KAPACITET_MAPE_PROBLEMA = 3;

	private boolean formatPrevodaUcitan = false;
	private int odeljakaPrevoda;

	private static final String UTF_BOM = "\uFEFF";

	// ------------------------------------------------------------------- Informacije za parsiranje prevoda
	private static final String SEKCIJA_PREVOD = "[Events]";
	private static final String SEKCIJA_PREVOD_LOWER_CASE = "[events]";

	private static final String SEKCIJA_PREVOD_DEFAULT_FORMAT = 
			"Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text";
	private static final String SEKCIJA_PREVOD_RED_FORMAT = "Format:";
	private static final String SEKCIJA_PREVOD_RED_COMMENT = "Comment:";
	private static final String SEKCIJA_PREVOD_RED_DIALOGUE = "Dialogue:";

	private static final String SEKCIJA_PREVOD_EL_LAYER = "Layer";
	private static final String SEKCIJA_PREVOD_EL_START = "Start";
	private static final String SEKCIJA_PREVOD_EL_END = "End";
	private static final String SEKCIJA_PREVOD_EL_STYLE = "Style";
	private static final String SEKCIJA_PREVOD_EL_NAME = "Name";
	private static final String SEKCIJA_PREVOD_EL_MARGINL = "MarginL";
	private static final String SEKCIJA_PREVOD_EL_MARGINR = "MarginR";
	private static final String SEKCIJA_PREVOD_EL_MARGINV = "MarginV";
	private static final String SEKCIJA_PREVOD_EL_EFFECT = "Effect";
	private static final String SEKCIJA_PREVOD_EL_TEXT = "Text";
	private static int indexPrevodLayer, indexPrevodStart, indexPrevodEnd, indexPrevodStyle, indexPrevodName, 
	indexPrevodMarginL, indexPrevodMarginR, indexPrevodMarginV, indexPrevodEffect, indexPrevodText;
	private static final int DEFAULT_INTEGERI = 0;
	private static final String DEFAULT_STRINGOVI = ""; // TODO (0.4.6) ove dve stvari su shit, ne smes tako semanticki razlicite da podvedes pod isto

	// ------------------------------------------------------------------- Informacije za parsiranje stilova
	private static final String SEKCIJA_STIL = "[V4+ Styles]";
	//private static final String SEKCIJA_STIL_OLD = "[V4 Styles]"; ne treba - za poredjenje koristis lower case
	private static final String SEKCIJA_STIL_LOWER_CASE = "[v4+ styles]";
	private static final String SEKCIJA_STIL_OLD_LOWER_CASE = "[v4 styles]";

	// ------------------------------------------------------------------- Informacije za parsiranje zaglavlja
	private static final String SEKCIJA_ZAGLAVLJE = "[Script Info]";
	private static final String SEKCIJA_ZAGLAVLJE_LOWER_CASE = "[script info]";

	public AssFileParser(@NonNull Context context, ParserCallback kolbek) {
		this.context = context;
		this.kolbek = kolbek;
	}
	
	/** Svim promenjljivama koje se koriste u parsiranju dodeljuje default vrednosti. */
	private void inicijalizujVrednosti() {
		warnString = "";
		indexPrevodLayer = 0;
		indexPrevodStart = 1;
		indexPrevodEnd = 2;
		indexPrevodStyle = 3;
		indexPrevodName = 4;
		indexPrevodMarginL = 5;
		indexPrevodMarginR = 6;
		indexPrevodMarginV = 7;
		indexPrevodEffect = 8;
		indexPrevodText = 9;
		odeljakaPrevoda = 10;
		formatPrevodaUcitan = false;
		tekucaSekcija = null;
		mapaProblema = new SparseArray<>(KAPACITET_MAPE_PROBLEMA);
	}

	/** Ucitava prevod sa tekuce putanje o kojem ce se ovaj handler starati. 
	 * @throws ParsiranjeException Ako dodje do problema pri parsiranju, usled pogresnog formata prevoda. */
	public void zapocniParsiranje(Uri uri) throws IOException, ParsiranjeException {
		inicijalizujVrednosti();
		boolean prvaLinija = true;
		try (BufferedReader citac = new BufferedReader(new InputStreamReader(new FileInputStream(uri.getPath())))) {
			while (true) {
				String linija = citac.readLine();
				if (linija == null) break;
				if (prvaLinija) {
					prvaLinija = false;
					if (linija.startsWith(UTF_BOM))
						linija = linija.substring(1);
				}
				linija = linija.trim();
				if (linija.equals("") || linija.startsWith(";")) continue;
				obradi(linija);
			}
			proslediTekucuSekciju(); // posto ucitavas od sekcije do sekcije, a na kraju nema sekcija da zatvori sve
			javiZavrsenoParsiranje();
		}
	}

	/** Obradjuje liniju - belezi ako je pocetak sekcije, parsira i ubacuje u listu ako je neki od redova. */
	@SuppressWarnings("unchecked")
	private void obradi(String linija) throws ParsiranjeException {
		if(linija.startsWith("[")) {
			Sekcija sekcija = odrediSekciju(linija);
			// Ako si bio u nekoj sekciji (a sada prelazis na novu), onda sibni sve sto si ucitao
			// na obradu. Dakle pre prelaska na sledecu sekciju obradjujes sve iz prethodne.
			if(tekucaSekcija != null) {
				proslediTekucuSekciju();
			}
			lista = new ArrayList<>();
			tekucaSekcija = sekcija;
		} else {
			Object isparsiran = parsiraj(linija);
			if(isparsiran!=null) lista.add(isparsiran);
		}
	}

	/** Odredjuje koju sekciju zapocinje zadata linija. */
	private Sekcija odrediSekciju(String linija) {
		String linijaLowerCase = linija.toLowerCase();

		if(linijaLowerCase.equals(SEKCIJA_PREVOD_LOWER_CASE)) 
			return Sekcija.PREVOD;
		else if(linijaLowerCase.equals(SEKCIJA_STIL_LOWER_CASE)
				|| linijaLowerCase.equals(SEKCIJA_STIL_OLD_LOWER_CASE)) 
			return Sekcija.STYLE;
		else if(linijaLowerCase.equals(SEKCIJA_ZAGLAVLJE_LOWER_CASE)) 
			return Sekcija.INFO;
		else
			return Sekcija.NEPOZNATA;
	}

	/** Prosledjuje listu sa nizom ucitanih redova odgovarajucoj metodi kolbeka zavisno od toga koja je sekcija
	 *  aktuelna. */
	@SuppressWarnings("unchecked")
	private void proslediTekucuSekciju() {
		switch (tekucaSekcija) {
		case INFO:
			kolbek.ucitaniRedoviZaglavlja((List<RedZaglavlja>) lista);
			break;
		case PREVOD:
			kolbek.ucitaniRedoviPrevoda((List<RedPrevoda>) lista);
			break;
		case STYLE:
			kolbek.ucitaniRedoviStila((List<RedStila>) lista);
			break;
		default:
			break;
		}
	}

	/** Obavlja parsiranje linije zavisno od toga cemu pripada (zaglavlju, stilu, prevodu itd.). */
	private Object parsiraj(String linija) throws ParsiranjeException {
		switch (tekucaSekcija) {
		case INFO:
			return parsirajZaglavlje(linija);
		case PREVOD:
			return parsirajPrevod(linija);
		case STYLE:
			return parsirajStil(linija);
		default:
			return null;
		}
	}

	private RedZaglavlja parsirajZaglavlje(String linija) {
		return new RedZaglavlja(linija);
	}

	private RedPrevoda parsirajPrevod(String linija) throws ParsiranjeException {		
		if(!formatPrevodaUcitan) {
			formatPrevodaUcitan = true;
			if(linija.startsWith(SEKCIJA_PREVOD_RED_FORMAT)) {
				parsirajFormatPrevoda(linija);
				return null; // format red ne ubacujes na listu 
			} else {
				dodajProblemNedostajucFormatPrevoda();
			}
		}

		RedPrevoda redPrevoda = new RedPrevoda();
		if(linija.startsWith(SEKCIJA_PREVOD_RED_COMMENT)) {
			redPrevoda.komentar = true;
			linija = linija.substring(SEKCIJA_PREVOD_RED_COMMENT.length()).trim();
		} else if(linija.startsWith(SEKCIJA_PREVOD_RED_DIALOGUE)) {
			redPrevoda.komentar = false;
			linija = linija.substring(SEKCIJA_PREVOD_RED_DIALOGUE.length()).trim();
		} else {
			dodajProblemNepoznatRedPrevoda();
			return null;
		}

		int brojZareza = 0;
		int zadnjiSplit = 0;
		List<String> parcad = new LinkedList<>();
		for (int i = 0; i < linija.length(); i++) {
			if (linija.charAt(i) == ',') {
				parcad.add(linija.substring(zadnjiSplit,i));
				zadnjiSplit = i+1;//plus 1 da bi preskocio i zarez koji sledi
				brojZareza++;
				if (brojZareza == odeljakaPrevoda-1) {
					parcad.add(linija.substring(i+1)); // takodje plus jedan da bi preskocio zarez
					break;
				}
			}
		}
		// ako nedostaje deo linije - dopuni praznim parcicima
		while(parcad.size() < odeljakaPrevoda)
			parcad.add("");

		Boolean outFailed[] = new Boolean[1];
		outFailed[0] = false;
				
		redPrevoda.effect = indexPrevodEffect != -1? parcad.get(indexPrevodEffect) : DEFAULT_STRINGOVI;
		if(indexPrevodStart != -1)
			redPrevoda.start = Alatke.parsirajVreme(parcad.get(indexPrevodStart),DEFAULT_INTEGERI, outFailed);
		if(indexPrevodEnd != -1)
			redPrevoda.end = Alatke.parsirajVreme(parcad.get(indexPrevodEnd), DEFAULT_INTEGERI, outFailed);
		if(indexPrevodLayer != -1)
			redPrevoda.layer = Alatke.parsirajInteger(parcad.get(indexPrevodLayer), DEFAULT_INTEGERI, outFailed);
		if(indexPrevodMarginL != -1)
			redPrevoda.marginL = Alatke.parsirajInteger(parcad.get(indexPrevodMarginL), DEFAULT_INTEGERI, outFailed);
		if(indexPrevodMarginR != -1)
			redPrevoda.marginR = Alatke.parsirajInteger(parcad.get(indexPrevodMarginR), DEFAULT_INTEGERI, outFailed);
		if(indexPrevodMarginV != -1)
			redPrevoda.marginV = Alatke.parsirajInteger(parcad.get(indexPrevodMarginV), DEFAULT_INTEGERI, outFailed);
		redPrevoda.actorName = indexPrevodName != -1? parcad.get(indexPrevodName) : DEFAULT_STRINGOVI;
		redPrevoda.style = indexPrevodStyle != -1? parcad.get(indexPrevodStyle) : DEFAULT_STRINGOVI;
		redPrevoda.text = indexPrevodText != -1? parcad.get(indexPrevodText) : DEFAULT_STRINGOVI;

		redPrevoda.lineNumber = lista.size() + 1; // +1 jer size je na pocetku 0
		
		if(outFailed[0]) dodajProblemParsiranjePrevoda(redPrevoda.lineNumber);
		return redPrevoda;
	}
	
	/** Parsira liniju koja specificira format svih linija sa prevodom. Belezi indekse gde je koji element i 
	 * to se kasnije koristi pri parsiranju redova prevoda. */
	private void parsirajFormatPrevoda(String linija) {
		String formatLinija = linija.substring(SEKCIJA_PREVOD_RED_FORMAT.length());
		String formatDelovi[] = formatLinija.split(",");
		Alatke.trimujElemente(formatDelovi);
		
		odeljakaPrevoda = 0;
		indexPrevodEffect = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_EFFECT, formatDelovi);
		if(indexPrevodEffect != -1) odeljakaPrevoda++;
		indexPrevodEnd = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_END, formatDelovi);
		if(indexPrevodEnd != -1) odeljakaPrevoda++;
		indexPrevodLayer = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_LAYER, formatDelovi);
		if(indexPrevodLayer != -1) odeljakaPrevoda++;
		indexPrevodMarginL = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_MARGINL, formatDelovi);
		if(indexPrevodMarginL != -1) odeljakaPrevoda++;
		indexPrevodMarginR = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_MARGINR, formatDelovi);
		if(indexPrevodMarginR != -1) odeljakaPrevoda++;
		indexPrevodMarginV = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_MARGINV, formatDelovi);
		if(indexPrevodMarginV != -1) odeljakaPrevoda++;
		indexPrevodName = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_NAME, formatDelovi);
		if(indexPrevodName != -1) odeljakaPrevoda++;
		indexPrevodStart = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_START, formatDelovi);
		if(indexPrevodStart != -1) odeljakaPrevoda++;
		indexPrevodStyle = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_STYLE, formatDelovi);
		if(indexPrevodStyle != -1) odeljakaPrevoda++;
		indexPrevodText = naKojojPozicijiJeString(SEKCIJA_PREVOD_EL_TEXT, formatDelovi);
		if(indexPrevodText != -1) odeljakaPrevoda++;
	}
	
	/** Vraca na kojoj poziciji u nizu Stringova se nalazi zadati string; -1 ako ga nema. */
	private int naKojojPozicijiJeString(String ovaj, String elementi[]) {
		for(int i = 0; i < elementi.length; i++)
			if(elementi[i].equals(ovaj)) return i;
		return -1;
	}
	
	private RedStila parsirajStil(String linija) {
		return new RedStila(linija); //TODO: ne radis nista sa linijom koja odredjuje format
	}

	private void javiZavrsenoParsiranje() {
		if(mapaProblema.size() != 0) {
			Resources r = context.getResources();
			if(mapaProblema.get(PROB_FORMAT_PREVODA) != null) {
				warnString = r.getString(R.string.parsiranje_fail_nema_prevod_format_linije);
			}
			Integer broj = (Integer) mapaProblema.get(PROB_NEPOZNAT_RED_PREVODA);
			if(broj != null) {
				warnString += (warnString.length() > 0? "\n" : "")
						+ r.getString(R.string.parsiranje_fail_problem_nepoznat_red_prevoda, broj);
			}
			@SuppressWarnings("unchecked")
			List<Integer> puknuti = (List<Integer>) mapaProblema.get(PROB_PARSIRANJE_PREVODA);
			if(puknuti != null) {
				StringBuilder spakovan = new StringBuilder();
				for(Integer b : puknuti)
					spakovan.append(b).append(",");
				spakovan.deleteCharAt(spakovan.length()-1);
				warnString += (warnString.length() > 0? "\n" : "")
						+ r.getString(R.string.parsiranje_fail_problem_red_prevoda, spakovan);
			}
		}
		kolbek.zavrsenoParsiranje(mapaProblema.size() != 0, warnString);
	}
	
	// --------------------------------------- Problemi
	
	/** U mapu problema ovog parsera dodaje novi zapis o problemu sa parsiranjem redova. */
	@SuppressWarnings("unchecked")
	private void dodajProblemParsiranjePrevoda(int lineNumber) {
		List<Integer> puknuti = (List<Integer>) mapaProblema.get(PROB_PARSIRANJE_PREVODA);
		if(puknuti == null) {
			puknuti = new ArrayList<>();
			mapaProblema.put(PROB_PARSIRANJE_PREVODA, puknuti);
		}
		puknuti.add(lineNumber);
	}
	
	/** U mapu problema ovog parsera dodaje novi zapis o nedostajucem redu sa formatom prevoda. */
	private void dodajProblemNedostajucFormatPrevoda() {
		mapaProblema.put(PROB_FORMAT_PREVODA, new Object());
	}
	
	private void dodajProblemNepoznatRedPrevoda() {
		int brojNepoznatih = (Integer) mapaProblema.get(PROB_NEPOZNAT_RED_PREVODA, 0);
		mapaProblema.put(PROB_NEPOZNAT_RED_PREVODA, ++brojNepoznatih);
	}
	
	// -------------------------------------------------------------------------------------------- Snimanje prevoda

	public void snimiPrevod(String putanjaPrevoda, Cursor redoviZaglavlja, Cursor redoviStila, Cursor redoviPrevoda)
			throws FileNotFoundException {
		File fajl = new File(putanjaPrevoda);
		PrintWriter p = new PrintWriter(fajl);

		p.print(UTF_BOM);

		p.println(SEKCIJA_ZAGLAVLJE);
		while(redoviZaglavlja.moveToNext()) {
			p.println(RedZaglavlja.kreirajStringIzKursora(redoviZaglavlja));
		}
		p.println();

		p.println(SEKCIJA_STIL);
		//TODO: ne stampas eksplicitno red za format stila - zajedno ti je na gomili sa ostalim
		// ali kad izmenis gore ucitavanje stila, onda menjaj i ovde stampanje stila
		while(redoviStila.moveToNext()) {
			p.println(RedStila.kreirajStringIzKursora(redoviStila)); //TODO: nece da moze. ili oce ako je AssRedStila
		}
		p.println();

		p.println(SEKCIJA_PREVOD);
		p.println(SEKCIJA_PREVOD_DEFAULT_FORMAT);
		while(redoviPrevoda.moveToNext()) {
			RedPrevoda red = RedPrevoda.kreirajIzKursora(redoviPrevoda);
			p.printf("%s%d,%s,%s,%s,%s,%04d,%04d,%04d,%s,%s\n", 
					(red.komentar? SEKCIJA_PREVOD_RED_COMMENT : SEKCIJA_PREVOD_RED_DIALOGUE) + " ",
					red.layer, 
					Alatke.formatirajVreme(red.start), 
					Alatke.formatirajVreme(red.end), 
					red.style, 
					red.actorName,
					red.marginL, 
					red.marginR, 
					red.marginV, 
					red.effect, 
					red.text);
		}
		p.close();
	}
}

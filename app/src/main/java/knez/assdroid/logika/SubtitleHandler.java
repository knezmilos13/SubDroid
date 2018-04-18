package knez.assdroid.logika;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import knez.assdroid.common.AbstractRepo;
import knez.assdroid.logika.Parser.ParserCallback;
import knez.assdroid.util.Loger;
import knez.assdroid.util.Threader;
import timber.log.Timber;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

public class SubtitleHandler extends AbstractRepo implements ParserCallback {

	private static final String PREFERENCE_FAJL = "global_podesavanja";
	private static final String PREF_ZADNJI_FAJL = "zadnji_otvoren_fajl";
	private static final String PREF_ZADNJA_PUTANJA = "zadnja_otvorena_putanja";
	private static final String PREF_MENJAN = "pref_prevod_menjan";

    @NonNull private final ContentResolver contentResolver;
    @NonNull private final ExecutorService executorService;
	@NonNull private final Threader mainThreader;
	@NonNull private final Timber.Tree logger;

	@NonNull private final List<Callback> callbacks = new ArrayList<>();

    private boolean currentSubtitleEdited = true; // TODO novi, untitled fajl je uvek currently edited
    @Nullable private Uri currentSubtitlePath = null;
    @Nullable private String currentSubtitleFilename;



    private final Context kontekst;
    private SQLiteDatabase baza;
	private long vremeZadnjeIzmene;

	public SubtitleHandler(@NonNull ContentResolver contentResolver,
                           @NonNull ExecutorService executorService,
                           @NonNull Threader mainThreader,
                           @NonNull Timber.Tree logger,
                           @NonNull Context kontekst) {
	    this.contentResolver = contentResolver;
	    this.executorService = executorService;
		this.mainThreader = mainThreader; // TODO: umesto da mu dajes kontekst, daj mu bazu i preference
        this.logger = logger;
        this.kontekst = kontekst;
		baza = new ProvajderBaze(kontekst).getWritableDatabase();
	}

	// TODO: posebna klasa za staranje o bazi koja se injectuje ovde
	// TODO: proslediti scheduler ovde (vidi na poslu malo kako si uradio)

	public void attachListener(Callback callback) {
		if(callbacks.contains(callback)) return;
		callbacks.add(callback);
	}

	public void detachListener(Callback callback) {
		callbacks.remove(callback);
	}

    // TODO: napraviti neki kontejner za parsere pa onda pitaj njega da li ima parser koji ovo moze da otvori
    public boolean canLoadSubtitle(@NonNull String subtitleFilename) {
        return subtitleFilename.toLowerCase().endsWith(".ass");
    }

	public void loadSubtitle(@NonNull Uri subtitlePath) {
        currentSubtitlePath = subtitlePath;
        currentSubtitleEdited = false;
        currentSubtitleFilename = getFileNameFromUri(subtitlePath);

        if(!canLoadSubtitle(currentSubtitleFilename)) {
            // TODO: javi da ne mozes da otvoris
            return;
        }

	    // TODO ocisti takodje lokalne podatke sve
		ocistiBazu();
		Parser parser = Parser.Fabrika.dajParserZaFajl(kontekst, subtitlePath.getPath(), this);
		try {
			parser.zapocniParsiranje(subtitlePath);
			//TODO pamti ime na kraju, kad uspe/ne uspe da ucita
//			putanja = subtitlePath.getPath();
//			String parcad[] = putanja.split(File.separator); // TODO what to do
			currentSubtitleEdited = false;
			SharedPreferences.Editor edit = kontekst.getSharedPreferences(PREFERENCE_FAJL, 0).edit();
//			edit.putString(PREF_ZADNJA_PUTANJA, putanja);
//			edit.putString(PREF_ZADNJI_FAJL, imePrevoda);
//			edit.putBoolean(PREF_MENJAN, prevodMenjan); // TODO oce ovo ovaj da radi ili neko drugi?
			edit.apply();
		} catch (IOException | ParsiranjeException e) {
			Loger.log(e);
			kreirajNoviPrevod();
			//Toast.makeText(this, R.string.editor_load_fail, Toast.LENGTH_LONG).show(); //JAVI NEKOME
		}
	}


    /** Kreira novi, prazan prevod o kojem ce se ovaj handler starati. */
	public void kreirajNoviPrevod() {
		ocistiBazu();
//		putanja = "";
		currentSubtitleFilename = "";
		currentSubtitleEdited = true;
		SharedPreferences.Editor edit = kontekst.getSharedPreferences(PREFERENCE_FAJL, 0).edit();
//		edit.putString(PREF_ZADNJA_PUTANJA, putanja);
//		edit.putString(PREF_ZADNJI_FAJL, imePrevoda);
//		edit.putBoolean(PREF_MENJAN, prevodMenjan);
		edit.apply();
	}

	public void ucitajAkoPostojiOdPre() {
		SharedPreferences shp = kontekst.getSharedPreferences(PREFERENCE_FAJL, 0);
//		putanja = shp.getString(PREF_ZADNJA_PUTANJA, "");
//		imePrevoda = shp.getString(PREF_ZADNJI_FAJL, "");
//		prevodMenjan = shp.getBoolean(PREF_MENJAN, false);
		// sad nista dalje. ako ima nesto u bazi, dobice ga kad zatrazi kursor
	}

	public String getImePrevoda() {
		return currentSubtitleFilename;
	}
	public String getPutanja() {
//		return putanja; // TODO cemu sve ovo
        return null;
	}
	public boolean isPrevodMenjan() { // TODO rename i vidi gde i kako se koristi uopste
        return currentSubtitleEdited;
	}
	public void setPrevodMenjan(boolean jelda) { // TODO
//		if(prevodMenjan == jelda) return;
//		prevodMenjan = jelda;
//		SharedPreferences.Editor edit = kontekst.getSharedPreferences(PREFERENCE_FAJL, 0).edit();
//		edit.putBoolean(PREF_MENJAN, prevodMenjan);
//		edit.apply();
	}

	public void snimiPrevod() throws FileNotFoundException { // TODO
//		Parser parser = Parser.Fabrika.dajParserZaFajl(kontekst, imePrevoda, this);
//		parser.snimiPrevod(putanja, dajSveRedoveZaglavlja(), dajSveRedoveStila(), ucitajSveRedovePrevoda());
//		setPrevodMenjan(false);
	}

	public RedPrevoda dajRedPrevoda(int lineNumber) {
		return ucitajRedPrevoda(lineNumber);
	}

	public boolean postojiLiRedPrevoda(int lineNumber) {
		return ucitajRedPrevoda(lineNumber) != null;
	}

	// ------------------------------------------------------------------------------------------ Ucitavanje prevoda

	@Override
	public void ucitaniRedoviPrevoda(List<RedPrevoda> redovi) {		
		baza.beginTransaction();
		baza.setLockingEnabled(false);
		
		// Create a single InsertHelper to handle this set of insertions.
        InsertHelper ih = new InsertHelper(baza, RedPrevoda.IME_TABELE);
 
        // Get the numeric indexes for each of the columns that we're updating
        int indexLine = ih.getColumnIndex(RedPrevoda.K_LINE);
        int indexLayer = ih.getColumnIndex(RedPrevoda.K_LAYER);
        int indexMarginL = ih.getColumnIndex(RedPrevoda.K_MARGIN_L);
        int indexMarginR = ih.getColumnIndex(RedPrevoda.K_MARGIN_R);
        int indexMarginV = ih.getColumnIndex(RedPrevoda.K_MARGIN_V);
        int indexStart = ih.getColumnIndex(RedPrevoda.K_START);
        int indexEnd = ih.getColumnIndex(RedPrevoda.K_END);
        int indexStyle = ih.getColumnIndex(RedPrevoda.K_STYLE);
		int indexActor = ih.getColumnIndex(RedPrevoda.K_ACTOR_NAME);
		int indexEffect = ih.getColumnIndex(RedPrevoda.K_EFFECT);
		int indexText = ih.getColumnIndex(RedPrevoda.K_TEXT);
		int indexKomentar = ih.getColumnIndex(RedPrevoda.K_KOMENTAR);
		
		for(RedPrevoda red : redovi) {
			ih.prepareForInsert();
			ih.bind(indexLine, red.lineNumber);
			ih.bind(indexLayer, red.layer);
			ih.bind(indexMarginL, red.marginL);
			ih.bind(indexMarginR, red.marginR);
			ih.bind(indexMarginV, red.marginV);
			ih.bind(indexStart, red.start);
			ih.bind(indexStyle, red.style);
			ih.bind(indexEnd, red.end);
			ih.bind(indexActor, red.actorName);
			ih.bind(indexEffect, red.effect);
			ih.bind(indexText, red.text);
			ih.bind(indexKomentar, red.komentar);
			ih.execute();
		}
		ih.close();
		baza.setTransactionSuccessful();
		baza.endTransaction();
		baza.setLockingEnabled(true);
	}
	@Override
	public void ucitaniRedoviStila(List<RedStila> redovi) {
		for(RedStila red : redovi)
			ubaciRedStilaUBazu(red);
	}
	@Override
	public void ucitaniRedoviZaglavlja(List<RedZaglavlja> redovi) {
		for(RedZaglavlja red : redovi)
			ubaciRedZaglavljaUBazu(red);
	}
	@Override
	public void zavrsenoParsiranje(boolean problemi, String warnString) {
		if(problemi)
			Log.d("UPOZORENJE", warnString); //TODO poslati neki warn korisniku
	}


	// --------------------------------------------------------------------------------------------------------- SQL

	private void ocistiBazu() {
		baza.delete(RedPrevoda.IME_TABELE, null, null);
		baza.delete(RedStila.IME_TABELE, null, null);
		baza.delete(RedZaglavlja.IME_TABELE, null, null);
	}

	private void ubaciRedPrevodaUBazu(RedPrevoda red) {
		baza.insert(RedPrevoda.IME_TABELE, null, red.dajVrednostiZaBazu());
	}

	private void ubaciRedStilaUBazu(RedStila red) {
		baza.insert(RedStila.IME_TABELE, null, red.dajVrednostiZaBazu());
	}

	private void ubaciRedZaglavljaUBazu(RedZaglavlja red) {
		baza.insert(RedZaglavlja.IME_TABELE, null, red.dajVrednostiZaBazu());
	}
	
	public void updateRedPrevoda(RedPrevoda red) {
		baza.update(RedPrevoda.IME_TABELE, red.dajVrednostiZaBazu(), RedPrevoda.K_ID + "=" + red.id, null);
	}

	public Cursor ucitajSveRedovePrevoda() {
		return ucitajRedovePrevoda(null, false);
	}
	
	public Cursor dajSveRedoveStila() {
		return baza.query(RedStila.IME_TABELE, null, null, null, null, null, null);
	}
	
	public Cursor dajSveRedoveZaglavlja() {
		return baza.query(RedZaglavlja.IME_TABELE, null, null, null, null, null, null);
	}

	public Cursor ucitajRedovePrevoda(String trazeniTekst, boolean matchCase) {
		if(trazeniTekst != null) {
			if(matchCase)
				trazeniTekst = RedPrevoda.K_TEXT + " glob '*" + trazeniTekst + "*'";
			else
				trazeniTekst = RedPrevoda.K_TEXT + " like '%" + trazeniTekst + "%'";
		}
		return baza.query(RedPrevoda.IME_TABELE, null, trazeniTekst, null, null, null, RedPrevoda.K_LINE);
	}

	public RedPrevoda ucitajRedPrevoda(int lineNumber) {
		Cursor kurs = baza.query(RedPrevoda.IME_TABELE, null, RedPrevoda.K_LINE + " = " + lineNumber, 
				null, null, null, RedPrevoda.K_LINE);
		if(kurs.moveToFirst()) {
			return RedPrevoda.kreirajIzKursora(kurs);
		} else return null;
	}




    // ------------------------------------------------------------------------------------- INTERNAL

    private String getFileNameFromUri(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                if(cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


	// ------------------------------------------------------------------------------------- CLASSES

	@UiThread
	public interface Callback {
		// TODO
//		void onDataDownloaded(List<Item> items, PaginationData paginationData, int priority);
	}


}

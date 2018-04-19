package knez.assdroid.subtitle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import knez.assdroid.common.AbstractRepo;
import knez.assdroid.subtitle.data.SubtitleFile;
import knez.assdroid.util.FileHandler;
import knez.assdroid.util.Threader;
import timber.log.Timber;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

public class SubtitleController extends AbstractRepo {

	@NonNull private final SubtitleHandlerRepository subtitleHandlerRepository;
    @NonNull private final FileHandler fileHandler;
    @NonNull private final ExecutorService executorService;
	@NonNull private final Threader mainThreader;
	@NonNull private final Timber.Tree logger;

	@NonNull private final List<Callback> callbacks = Collections.synchronizedList(new ArrayList<>());

	@Nullable private SubtitleFile currentSubtitleFile;

    public SubtitleController(@NonNull SubtitleHandlerRepository subtitleHandlerRepository,
                              @NonNull FileHandler fileHandler,
                              @NonNull ExecutorService executorService,
                              @NonNull Threader mainThreader,
                              @NonNull Timber.Tree logger) {
		this.subtitleHandlerRepository = subtitleHandlerRepository;
		this.fileHandler = fileHandler;
	    this.executorService = executorService;
		this.mainThreader = mainThreader;
        this.logger = logger;

//		baza = new ProvajderBaze(kontekst).getWritableDatabase();
// TODO posebna klasa za staranje o bazi koja se injectuje ovde
	}

    @AnyThread
    public void attachListener(Callback callback) {
    	synchronized (callbacks) {
			if (callbacks.contains(callback)) return;
			callbacks.add(callback);
		}
	}

    @AnyThread
    public void detachListener(Callback callback) {
		callbacks.remove(callback);
	}

    @AnyThread
    public boolean canLoadSubtitle(@NonNull String subtitleFilename) {
        return subtitleHandlerRepository.canOpenSubtitleFile(subtitleFilename);
    }

    @AnyThread
	public void loadSubtitle(@NonNull Uri subtitlePath) {
        executorService.execute(() -> _loadSubtitle(subtitlePath));
	}


    // ------------------------------------------------------------------------------------ INTERNAL

    private void _loadSubtitle(@NonNull Uri subtitlePath) {
        String subtitleFilename = fileHandler.getFileNameFromUri(subtitlePath);

        SubtitleParser subtitleParser = subtitleHandlerRepository.getParserForSubtitleFile(subtitleFilename);
        if(subtitleParser == null) {
            fireCallbacks(callbacks, callback -> callback.onInvalidSubtitleFormat(subtitleFilename),
                    mainThreader);
            return;
        }

        List<String> fileContent;
        try {
            fileContent = fileHandler.readFileContent(subtitlePath);
        } catch (IOException e) {
            logger.e(e);
            fireCallbacks(callbacks, callback -> callback.onFileReadingFailed(subtitleFilename),
                    mainThreader);
            return;
        }

        // TODO: vidi kakve izuzetke baca ovaj pa hendlaj; ali tek nakon sto prodjes kroz logiku parsera detaljno
		try {
			currentSubtitleFile = subtitleParser.parseSubtitle(fileContent);
		} catch (ParsiranjeException e) {
			e.printStackTrace();
			// TODO u staroj implementaciji si cisto kreirao novi fajl, ali ovde ne moras nista
            // posto taman nisi nista izmenio dok nisi uspesno ucitao fajl, cisto javi nazad callbackom
		}

//		ocistiBazu(); // TODO zameni sve stare podatke novim
        // TODO: javi klijentu na main threadu da je ucitan fajl... samo ne znam sta/kako da mu prosledis... sve?
    }


    // ------------------------------------------------------------------------------------- CLASSES

    @UiThread
    public interface Callback {
        void onInvalidSubtitleFormat(@NonNull String subtitleFilename);
        void onFileReadingFailed(@NonNull String subtitleFilename);
    }





    // TODO: nista ispod ove linije se ne koristi

    /** Kreira novi, prazan prevod o kojem ce se ovaj handler starati. */
	public void kreirajNoviPrevod() {
		ocistiBazu();
//		putanja = "";
//		currentSubtitleFilename = "";
//		currentSubtitleEdited = true;
//		SharedPreferences.Editor edit = kontekst.getSharedPreferences(PREFERENCE_FAJL, 0).edit();
//		edit.putString(PREF_ZADNJA_PUTANJA, putanja);
//		edit.putString(PREF_ZADNJI_FAJL, imePrevoda);
//		edit.putBoolean(PREF_MENJAN, prevodMenjan);
//		edit.apply();
	}

	public void ucitajAkoPostojiOdPre() {
//		SharedPreferences shp = kontekst.getSharedPreferences(PREFERENCE_FAJL, 0);
//		putanja = shp.getString(PREF_ZADNJA_PUTANJA, "");
//		imePrevoda = shp.getString(PREF_ZADNJI_FAJL, "");
//		prevodMenjan = shp.getBoolean(PREF_MENJAN, false);
		// sad nista dalje. ako ima nesto u bazi, dobice ga kad zatrazi kursor
	}

	public String getImePrevoda() {
		return currentSubtitleFile == null? null : currentSubtitleFile.getFilename();
	}
	public boolean isPrevodMenjan() {
        return currentSubtitleFile != null && currentSubtitleFile.isCurrentSubtitleEdited();
	}
	public void setPrevodMenjan(boolean jelda) {
//		if(prevodMenjan == jelda) return;
//		prevodMenjan = jelda;
//		SharedPreferences.Editor edit = kontekst.getSharedPreferences(PREFERENCE_FAJL, 0).edit();
//		edit.putBoolean(PREF_MENJAN, prevodMenjan);
//		edit.apply();
	}

	public void snimiPrevod() throws FileNotFoundException {
//		SubtitleParser parser = SubtitleParser.Fabrika.dajParserZaFajl(kontekst, imePrevoda, this);
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

//	@Override
	public void ucitaniRedoviPrevoda(List<RedPrevoda> redovi) {		
//		baza.beginTransaction();
//		baza.setLockingEnabled(false);
		
		// Create a single InsertHelper to handle this set of insertions.
//        InsertHelper ih = new InsertHelper(baza, RedPrevoda.IME_TABELE);
 
        // Get the numeric indexes for each of the columns that we're updating
//        int indexLine = ih.getColumnIndex(RedPrevoda.K_LINE);
//        int indexLayer = ih.getColumnIndex(RedPrevoda.K_LAYER);
//        int indexMarginL = ih.getColumnIndex(RedPrevoda.K_MARGIN_L);
//        int indexMarginR = ih.getColumnIndex(RedPrevoda.K_MARGIN_R);
//        int indexMarginV = ih.getColumnIndex(RedPrevoda.K_MARGIN_V);
//        int indexStart = ih.getColumnIndex(RedPrevoda.K_START);
//        int indexEnd = ih.getColumnIndex(RedPrevoda.K_END);
//        int indexStyle = ih.getColumnIndex(RedPrevoda.K_STYLE);
//		int indexActor = ih.getColumnIndex(RedPrevoda.K_ACTOR_NAME);
//		int indexEffect = ih.getColumnIndex(RedPrevoda.K_EFFECT);
//		int indexText = ih.getColumnIndex(RedPrevoda.K_TEXT);
//		int indexKomentar = ih.getColumnIndex(RedPrevoda.K_KOMENTAR);
//
//		for(RedPrevoda red : redovi) {
//			ih.prepareForInsert();
//			ih.bind(indexLine, red.lineNumber);
//			ih.bind(indexLayer, red.layer);
//			ih.bind(indexMarginL, red.marginL);
//			ih.bind(indexMarginR, red.marginR);
//			ih.bind(indexMarginV, red.marginV);
//			ih.bind(indexStart, red.start);
//			ih.bind(indexStyle, red.style);
//			ih.bind(indexEnd, red.end);
//			ih.bind(indexActor, red.actorName);
//			ih.bind(indexEffect, red.effect);
//			ih.bind(indexText, red.text);
//			ih.bind(indexKomentar, red.komentar);
//			ih.execute();
//		}
//		ih.close();
//		baza.setTransactionSuccessful();
//		baza.endTransaction();
//		baza.setLockingEnabled(true);
	}
//	@Override
	public void ucitaniRedoviStila(List<RedStila> redovi) {
		for(RedStila red : redovi)
			ubaciRedStilaUBazu(red);
	}
//	@Override
	public void ucitaniRedoviZaglavlja(List<RedZaglavlja> redovi) {
		for(RedZaglavlja red : redovi)
			ubaciRedZaglavljaUBazu(red);
	}
//	@Override
	public void zavrsenoParsiranje(boolean problemi, String warnString) {
		if(problemi)
			Log.d("UPOZORENJE", warnString); //TODO poslati neki warn korisniku
	}


	// --------------------------------------------------------------------------------------------------------- SQL

	private void ocistiBazu() {
//		baza.delete(RedPrevoda.IME_TABELE, null, null);
//		baza.delete(RedStila.IME_TABELE, null, null);
//		baza.delete(RedZaglavlja.IME_TABELE, null, null);
	}

	private void ubaciRedPrevodaUBazu(RedPrevoda red) {
//		baza.insert(RedPrevoda.IME_TABELE, null, red.dajVrednostiZaBazu());
	}

	private void ubaciRedStilaUBazu(RedStila red) {
//		baza.insert(RedStila.IME_TABELE, null, red.dajVrednostiZaBazu());
	}

	private void ubaciRedZaglavljaUBazu(RedZaglavlja red) {
//		baza.insert(RedZaglavlja.IME_TABELE, null, red.dajVrednostiZaBazu());
	}
	
	public void updateRedPrevoda(RedPrevoda red) {
//		baza.update(RedPrevoda.IME_TABELE, red.dajVrednostiZaBazu(), RedPrevoda.K_ID + "=" + red.id, null);
	}

	public Cursor ucitajSveRedovePrevoda() {
		return ucitajRedovePrevoda(null, false);
	}
	
	public Cursor dajSveRedoveStila() {
//		return baza.query(RedStila.IME_TABELE, null, null, null, null, null, null);
		return null;
	}
	
	public Cursor dajSveRedoveZaglavlja() {
//		return baza.query(RedZaglavlja.IME_TABELE, null, null, null, null, null, null);
		return null;
	}

	public Cursor ucitajRedovePrevoda(String trazeniTekst, boolean matchCase) {
		if(trazeniTekst != null) {
			if(matchCase)
				trazeniTekst = RedPrevoda.K_TEXT + " glob '*" + trazeniTekst + "*'";
			else
				trazeniTekst = RedPrevoda.K_TEXT + " like '%" + trazeniTekst + "%'";
		}
//		return baza.query(RedPrevoda.IME_TABELE, null, trazeniTekst, null, null, null, RedPrevoda.K_LINE);
		return null;
	}

	public RedPrevoda ucitajRedPrevoda(int lineNumber) {
//		Cursor kurs = baza.query(RedPrevoda.IME_TABELE, null, RedPrevoda.K_LINE + " = " + lineNumber,
//				null, null, null, RedPrevoda.K_LINE);
//		if(kurs.moveToFirst()) {
//			return RedPrevoda.kreirajIzKursora(kurs);
//		} else return null;
		return null;
	}

}

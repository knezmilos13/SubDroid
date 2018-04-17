package knez.assdroid.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import knez.assdroid.App;
import knez.assdroid.PrevodilacAktivnost;
import knez.assdroid.R;
import knez.assdroid.common.adapter.IdentifiableAdapter;
import knez.assdroid.editor.adapter.SubtitleLineAdapterPack;
import knez.assdroid.editor.gui.SubtitleLineLayoutItem;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.help.KategorijeHelpaAktivnost;
import knez.assdroid.podesavanja.KategorijePodesavanjaAktivnost;
import knez.assdroid.util.gui.BgpEditText;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class EditorActivity extends AppCompatActivity
        implements EditorMVP.ViewInterface, SubtitleLineLayoutItem.Callback, BgpEditText.Listener {

    private static final int REQUEST_CODE_OPEN_SUBTITLE = 1234;
    private static final int REQUEST_CODE_TRANSLATOR_ACTIVITY = 500;
    private static final int REQUEST_CODE_SETTINGS = 501;

    @BindView(R.id.editor_subtitle_list) protected RecyclerView itemListView;
    @BindView(R.id.editor_search_view) protected BgpEditText searchView;
    @BindView(R.id.editor_center_text) protected TextView centerTextView;

    private EditorMVP.PresenterInterface presenter;
    private IdentifiableAdapter subtitleLinesAdapter;

    // TODO: da probas start/stop umesto create/destroy?
    // TODO: po pokretanju aplikacije ucitati zadnje sve kako je bilo
    // TODO: po okretanju aplikacije sve da je kako je bilo
    // TODO:
    // osposobi editor activity sa 0 featurea, samo da moze da se pokrene
    // zatim da moze da ucita titlove
    // pa da otvori sledeci ekran? mozda?
    // i da tamo edituje i vrati
    // pa tek onda razmisljaj kako ces ona podesavanja silna sta da se vidi i tako to
    // to sve mozes i u neki drawer sa leve strane, najlakse tako
    // 2. subtitle line settings objekat snimaj u bundle i ucitavaj
    // 3. taj objekat daj prezenteru pre attacha
    // 4. taj objekat ubacujes u subtitle line VSO-jeve, taman neces imati sto puta iste atribute u njima


    // --------------------------------------------------------------------------- LIFECYCLE & SETUP

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        setUpInterface();

        presenter = App.getAppComponent().getEditorPresenter();
        presenter.onAttach(this);
	}

    @Override
    protected void onResume() {
        super.onResume();
        searchView.setListener(this);
    }

    @Override
    protected void onPause() {
        searchView.setListener(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        presenter = null;
        super.onDestroy();
    }

    private void setUpInterface() {
        setUpAdapter();
        setUpList();
    }

    private void setUpAdapter() {
        subtitleLinesAdapter = new IdentifiableAdapter(this);
        SubtitleLineAdapterPack slap = new SubtitleLineAdapterPack(this);
        subtitleLinesAdapter.addAdapterPack(slap);
    }

    private void setUpList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        itemListView.setLayoutManager(linearLayoutManager);
        itemListView.setAdapter(subtitleLinesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pocetna_aktivnost, menu);
        return super.onCreateOptionsMenu(menu);
    }


	// --------------------------------------------------------------------------------- USER EVENTS

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.meni_standard_create:
//				kreirajNoviPrevod(); // TODO
				break;
			case R.id.meni_standard_load:
				showFileSelector();
				break;
			case R.id.meni_standard_podesavanja:
				prikaziPodesavanja(); // TODO
				break;
			case R.id.meni_standard_save:
//				snimiPrevod(); // TODO
				break;
			case R.id.meni_standard_help:
				prikaziHelp(); // TODO
				break;
			default:
				return false;
		}
		return true;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch(requestCode) {
//			case RQ_CODE_FILE_DIALOG:
//				onZatvorenFajlDijalog(resultCode, data);
//				break;
//			case REQUEST_CODE_TRANSLATOR_ACTIVITY:
//				onZatvorenPrevodilac(resultCode, data);
//				break;
//			case REQUEST_CODE_SETTINGS:
//				boolean izmenjen = primeniPerzistentnaPodesavanjaNaAdapter();
//				if(izmenjen) prevodAdapter.notifyDataSetChanged();
//				primeniPerzistentnaPodesavanjaNaKontrole();
//				primeniFullscreen(panelView.isFullscreenOn());
//				break;
//		} // TODO

        if(resultCode != RESULT_OK) return;

        if(requestCode == REQUEST_CODE_OPEN_SUBTITLE && data != null) {
            Uri currentUri = data.getData();

            try {
                String content = readFileContent(currentUri);
                Log.e("WTF", content); // TODO
            } catch (IOException e) {
                // Handle error here
            }
        }
    }





	//	@Override // TODO
	protected void onListItemClick(ListView l, View v, int position, long id) {
//		boolean maknuo = prevodAdapter.getCursor().moveToPosition(position);
//		if(!maknuo) return;
//		RedPrevoda red = RedPrevoda.kreirajIzKursora(prevodAdapter.getCursor());
//		prikaziEditor(red.lineNumber);
	}

//	@Override
//	public void onKontroleMaximized() {
//		skupiListu(editorControlsView.getSirinaSekcijeDugmici(), editorControlsView.getVisinaSekcijeDugmici(),
//				editorControlsView.getTrenutnaPozicija());
//	}
//	@Override
//	public void onKontroleMinimized() {
//		rasiriListu();
//	}

//	@Override
//	public void onDimenzionisanaSekcijaDugmici(final int sirina, final int visina, final int rotacija) {
//		Handler h = new Handler();
//		h.post(new Runnable() {
//			@Override
//			public void run() {
//				// mora ovako, da bi odvojio fazu dimenzionisanja od faze promene lejauta
//				skupiListu(sirina, visina, rotacija);
//			}
//		});
//	}

	@Override
	public void onBackPressed() {
//		if(editorControlsView.isInterfejsMinimiziran())
//			editorControlsView.setInterfejsMinimiziran(false);
//		else if(editorControlsView.isPrikazanPanel())
//			editorControlsView.skloniPrikazaniPanel();
//		else super.onBackPressed();
	}


	// ---------------------------------------------------------------------------------------- Eventovi - panel View

//	@Override
//	public void onPritisnutoFilter(String tekst, boolean ukljucen, boolean matchCase, boolean highlight) {
//		if(!ukljucen) {
//			osveziListu();
//			prevodAdapter.clearTrazeniTekst();
//		} else {
//			if(tekst == null || tekst.equals(""))
//				// tek ukljucen - a prazno polje - ne treba nista da menjas ni da trazis osvezavanje
//				return;
//			izfiltrirajListu(tekst, matchCase, highlight);
//		}
//	}

//	@Override
//	public void onUnetTekstZaPretragu(String tekst, boolean matchCase, boolean highlight) {
//		if(panelView.isUkljucenFilter()) {
//			izfiltrirajListu(tekst, matchCase, highlight);
//		}
//	}
//	@Override
//	public void onPritisnutoFullScreen(boolean prikazi) {
//		primeniFullscreen(prikazi);
//	}
//	@Override
//	public void onPritisnutoStil(boolean prikazi) {
//		prevodAdapter.setPrikaziDrugiRed(prikazi);
//		prevodAdapter.notifyDataSetChanged();
//	}
//	@Override
//	public void onPritisnutoTagovi(boolean prikazi) {
//		prevodAdapter.setPrikaziTagove(prikazi);
//		prevodAdapter.notifyDataSetChanged();
//	}
//	@Override
//	public void onPritisnutoTajming(boolean prikazi) {
//		prevodAdapter.setPrikaziPrviRed(prikazi);
//		prevodAdapter.notifyDataSetChanged();
//	}
//	@Override
//	public void onPrikaziPodesavanjaFiltera() {
//		showDialog(PanelView.ID_DIJALOG_FILTER);
//	}
//	@Override
//	public void onPromenjenaPodesavanjaFiltera(boolean matchCase, boolean highlight) {
//		izfiltrirajListu(panelView.poljeUnos.getText().toString(), matchCase, highlight);
//	}

	@Override
	public void onSubtitleLineClicked(@NonNull SubtitleLineVso subtitleLineVso, @NonNull SubtitleLineLayoutItem layoutItem) {

	}




//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		Bundle stanjeView = panelView.dajStanje();
//		outState.putBundle(SAVE_PANEL_VIEW_STANJE, stanjeView);
//		super.onSaveInstanceState(outState);
//	}
//
//	@Override
//	protected void onPause() {
//		perzistirajStanjeInterfejsa();
//		super.onPause();
//	}
//
//	private void perzistirajStanjeInterfejsa() {
//		panelView.perzistirajStanje();
//	}
//
//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch(id) {
//		case PanelView.ID_DIJALOG_FILTER:
//			return panelView.kreirajDijalogFilter();
//		}
//		return super.onCreateDialog(id);
//	}
//
//	@Override
//	protected void onPrepareDialog(int id, Dialog dialog) {
//		switch(id) {
//		case PanelView.ID_DIJALOG_FILTER:
//			panelView.osveziDijalogFilter(dialog);
//		}
//		super.onPrepareDialog(id, dialog);
//	}

	// ---------------------------------------------------------------------------------------------------- Interfejs

	/** Ucitava kursor sa svim redovima prevoda i koristi ga da prikaze sve u listi. */
//	private void osveziListu() {
//		Cursor kurs = subHandler.ucitajSveRedovePrevoda();
//		stopManagingCursor(prevodAdapter.getCursor());
//		startManagingCursor(kurs);
//		prevodAdapter.changeCursor(kurs);
//	}
	/** Ucitava kursor sa trazenim redovima prevoda i koristi ga da ih prikaze u listi. */
//	private void osveziListu(String filterTekst, boolean matchCase) {
//		Cursor kurs = subHandler.ucitajRedovePrevoda(filterTekst, matchCase);
//		stopManagingCursor(prevodAdapter.getCursor());
//		startManagingCursor(kurs);
//		prevodAdapter.changeCursor(kurs);
//	}

//	private void osveziNaslov() {
//		String ceoNaslov = subHandler.isPrevodMenjan()? getResources().getString(R.string.editor_prevod_menjan_znak) : "";
//		if(subHandler.getImePrevoda().equals("")) {
//			ceoNaslov += getResources().getString(R.string.standard_untitled);
//		} else {
//			ceoNaslov += subHandler.getImePrevoda();
//		}
//
//		setTitle(ceoNaslov);
//	}

	/** Menja stanje adaptera zavisno od podesavanja snimljenih u preferencama. Vraca true
	 * ako je stanje stvarno izmenjeno, posle cega bi trebalo pozvati notifyDataSetChanged. */
//	private boolean primeniPerzistentnaPodesavanjaNaAdapter() {
//		boolean promenjenPrikazListe = false;
//		String tagZnak = PodesavanjaEditorUtil.getMinimizedCharTag();
//		if(prevodAdapter.getTagZamena()==null || !prevodAdapter.getTagZamena().equals(tagZnak)) {
//			prevodAdapter.setTagZamena(tagZnak);
//			promenjenPrikazListe = true;
//		}
//
//		int fontTekst = PodesavanjaEditorUtil.getTextFontSize();
//		if(prevodAdapter.getTekstSize() != fontTekst) {
//			prevodAdapter.setTekstSize(fontTekst);
//			promenjenPrikazListe = true;
//		}
//		int fontOstalo = PodesavanjaEditorUtil.getOstaloFontSize();
//		if(prevodAdapter.getOstaloSize() != fontOstalo) {
//			prevodAdapter.setOstaloSize(fontOstalo);
//			promenjenPrikazListe = true;
//		}
//
//		return promenjenPrikazListe;
//	}

	/** Menja stanje kontrola zavisno od podesavanja snimljenih u preferencama. */
//	private void primeniPerzistentnaPodesavanjaNaKontrole() {
//		int transparentnost = PodesavanjaEditorUtil.getMinimizedTransparentnost();
//		if(editorControlsView.getTransparentnost() != transparentnost) {
//			editorControlsView.setTransparentnost(transparentnost);
//		}
//	}

	/** Lista ce biti skrolovana tako da red prevoda sa zadatim brojem linije ispliva kao drugi na njoj. */
//	private void centrirajPrikazListeNa(int zadnjiMenjanLine) {
//		int pozicija = prevodAdapter.dajPozicijuZaLineNumber(zadnjiMenjanLine);
//		if(pozicija == -1) return;
//
////		getListView().setSelection(pozicija >= 1? pozicija-1 : pozicija); // TODO
//	}

	/** Siri listu da zauzme ceo prostor ekrana (zovi kad minimiziras kontrole). */
//	private void rasiriListu() {
//		zadnjaDimenzija = zadnjaRotacija = -1;
//		LayoutParams parametriL = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		LayoutParams parametriZ = new LayoutParams(0, 0);
//		zauzimacProstora.setLayoutParams(parametriZ);
////		getListView().setLayoutParams(parametriL); // TODO
//	}

//	private int zadnjaDimenzija = -1, zadnjaRotacija = -1;
//	/** Skuplja listu tako da nijedan njen deo ne bude ispod osnovnog reda sa kontrolama. */
//	private void skupiListu(int sirina,int visina,int rotacija) {
//		int sirinaViewa = sirina > visina? visina : sirina;
//		if(sirina <= 0 || visina <= 0 || rotacija <= 0 || (sirinaViewa == zadnjaDimenzija && rotacija == zadnjaRotacija))
//			return;
//
//		zadnjaDimenzija = sirinaViewa;
//		zadnjaRotacija = rotacija;
//
//		LayoutParams parametriZ = null;
//		LayoutParams parametriL = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		switch (rotacija) {
//		case EditorControlsView.POZICIJA_GORE:
//			parametriZ = new LayoutParams(LayoutParams.MATCH_PARENT, sirinaViewa);
//			parametriZ.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//			parametriL.addRule(RelativeLayout.BELOW, R.id.editor_zauzimac);
//			break;
//		case EditorControlsView.POZICIJA_DESNO:
//			parametriZ = new LayoutParams(sirinaViewa, LayoutParams.MATCH_PARENT);
//			parametriZ.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			parametriL.addRule(RelativeLayout.LEFT_OF, R.id.editor_zauzimac);
//			break;
//		case EditorControlsView.POZICIJA_DOLE:
//			parametriZ = new LayoutParams(LayoutParams.MATCH_PARENT, sirinaViewa);
//			parametriZ.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//			parametriL.addRule(RelativeLayout.ABOVE, R.id.editor_zauzimac);
//			break;
//		case EditorControlsView.POZICIJA_LEVO:
//			parametriZ = new LayoutParams(sirinaViewa, LayoutParams.MATCH_PARENT);
//			parametriZ.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			parametriL.addRule(RelativeLayout.RIGHT_OF, R.id.editor_zauzimac);
//			break;
//		}
//		zauzimacProstora.setLayoutParams(parametriZ);
////		getListView().setLayoutParams(parametriL); // TODO
//	}

//	private void izfiltrirajListu(String tekst, boolean filterMatchCase, boolean highlight) {
//		if(highlight)
//			prevodAdapter.setTrazeniTekst(tekst, filterMatchCase);
//		else
//			prevodAdapter.clearTrazeniTekst();
//		osveziListu(tekst, filterMatchCase);
//	}

//	/** Gasi odredjene elemente interfejsa za situacije kada kreiras/ucitas novi prevod. */
//	private void resetujInterfejsZaNoviPrevod() {
//		if(panelView.isUkljucenFilter())
//			panelView.setUkljucenFilter(false);
//	}

	// ---------------------------------------------------------------------------------------- Manipulacija prevodom

//	private void kreirajNoviPrevod() {
//		subHandler.kreirajNoviPrevod();
//		osveziListu();
//		osveziNaslov();
//		resetujInterfejsZaNoviPrevod();
//	}

//	private void ucitajPrevod(Uri odakle) {
//		subHandler.ucitajPrevod(odakle);
//		osveziListu();
//		osveziNaslov();
//		resetujInterfejsZaNoviPrevod();
//	}

//	private void snimiPrevod() {
//		try {
//			subHandler.snimiPrevod();
//		} catch (FileNotFoundException e) {
//			Loger.log(e);
//			e.printStackTrace();
//			//TODO ne postoji fajl... a ovo je save... da je saveas pa ajde
//		}
//		osveziNaslov();
//	}


	// --------------------------------------------------------------------------------------- Startovanje aktivnosti

	private void prikaziPodesavanja() {
		Intent namera = new Intent(this,KategorijePodesavanjaAktivnost.class);
		startActivityForResult(namera, REQUEST_CODE_SETTINGS);
	}

	private void prikaziHelp() {
		Intent namera = new Intent(this,KategorijeHelpaAktivnost.class);
		startActivity(namera);
	}

	private void prikaziEditor(int lineNumber) {
		Intent namera = new Intent(this,PrevodilacAktivnost.class);
		namera.putExtra(PrevodilacAktivnost.INPUT_BROJ_REDA, lineNumber);
		startActivityForResult(namera, REQUEST_CODE_TRANSLATOR_ACTIVITY);
	}

	private String readFileContent(Uri uri) throws IOException {

		InputStream inputStream =
				getContentResolver().openInputStream(uri);
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String currentline;
		while ((currentline = reader.readLine()) != null) {
			stringBuilder.append(currentline + "\n");
		}
		inputStream.close();
		return stringBuilder.toString();
	}

	private void onZatvorenFajlDijalog(int resultCode, Intent data) {
//		if(resultCode == RESULT_OK) {
//			if(!data.hasExtra(FileDialogOptions.RESULT_FILE))
//				return;
//
//			String izabranaPutanja = data.getStringExtra(FileDialogOptions.RESULT_FILE);
//			if(!FormatValidator.formatPrihvatljiv(izabranaPutanja)) {
//				return;
//			}
//
//			ucitajPrevod(Uri.fromFile(new File(izabranaPutanja)));
//		}
	}

    @Override
    public void onXClicked() {
        // TODO
    }

    @Override
    public void onSearchSubmitted(@NonNull String text) {
        // TODO
    }

    @Override
    public void onLetterInputted(@NonNull String text) {
        // TODO
    }

//	private void onZatvorenPrevodilac(int resultCode, Intent data) {
//		if(resultCode == RESULT_OK) {
//			boolean menjanoTamo = data.getExtras().getBoolean(PrevodilacAktivnost.OUTPUT_RADJENE_IZMENE_OVDE);
//			int zadnjiMenjanLine = data.getExtras().getInt(PrevodilacAktivnost.OUTPUT_ZADNJI_PREGLEDAN);
//
//			// Iz nekog razloga oce da pukne ponekad ako nista ne menjas u prevodiocu ako se ne pozove
//			// osveziListu... pa ono kao aj' onda sto ne bi zvao svaki put. Neki fazon sa kursorima baguje.
//			osveziListu();
//			osveziNaslov();
//
//			centrirajPrikazListeNa(zadnjiMenjanLine);
//
//			// Kad se vratis iz prevodioca neka ne bude ukljucen filter (posto oce nesto da pukne ponekad)
//			if(panelView.isUkljucenFilter()) {
//				panelView.setUkljucenFilter(false);
//				prevodAdapter.clearTrazeniTekst();
//			}
//		}
//	}


    // ------------------------------------------------------------------------------------ INTERNAL

    private void showFileSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_SUBTITLE);
    }

}

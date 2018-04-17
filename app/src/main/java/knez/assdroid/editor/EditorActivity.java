package knez.assdroid.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import knez.assdroid.App;
import knez.assdroid.EditorControlsView;
import knez.assdroid.EditorControlsView.KontroleStateListener;
import knez.assdroid.PanelView;
import knez.assdroid.PanelView.PanelViewListener;
import knez.assdroid.PrevodilacAktivnost;
import knez.assdroid.R;
import knez.assdroid.common.adapter.IdentifiableAdapter;
import knez.assdroid.editor.adapter.SubtitleLineAdapterPack;
import knez.assdroid.editor.gui.SubtitleLineLayoutItem;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.help.KategorijeHelpaAktivnost;
import knez.assdroid.logika.RedPrevoda;
import knez.assdroid.podesavanja.KategorijePodesavanjaAktivnost;
import knez.assdroid.podesavanja.PodesavanjaEditorUtil;
import knez.assdroid.util.Loger;
import yogesh.firzen.filelister.FileListerDialog;
import yogesh.firzen.filelister.OnFileSelectedListener;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class EditorActivity extends AppCompatActivity
        implements EditorMVP.ViewInterface, KontroleStateListener, PanelViewListener, SubtitleLineLayoutItem.Callback {

	private static final int RQ_CODE_FILE_DIALOG = 1;
	private static final int RQ_CODE_PREVODILAC_AKTIVNOST = 2;
	private static final int RQ_CODE_PODESAVANJA = 3;
	private static final String SAVE_PANEL_VIEW_STANJE = "stanje_panel_view";

    @BindView(R.id.subtitle_list_recycler) protected RecyclerView itemRecycler;
    @BindView(R.id.editor_controls_view) protected EditorControlsView editorControlsView;
    @BindView(R.id.editor_zauzimac) protected View zauzimacProstora; // TODO wtf je ovo

	private PanelView panelView;

    private EditorMVP.PresenterInterface presenter;
    private IdentifiableAdapter subtitleLinesAdapter;


    // --------------------------------------------------------------------------- LIFECYCLE & SETUP

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        setUpInterface();

        presenter = App.getAppComponent().getEditorPresenter();
        presenter.onAttach(this);



        // TODO:
        // 1. implementiraj subtitle line vso i layout item
        // osposobi editor activity sa 0 featurea, samo da moze da se pokrene
        // zatim da moze da ucita titlove
        // pa da otvori sledeci ekran? mozda?
        // i da tamo edituje i vrati
        // pa tek onda razmisljaj kako ces ona podesavanja silna sta da se vidi i tako to
        // to sve mozes i u neki drawer sa leve strane, najlakse tako

        // 2. subtitle line settings objekat snimaj u bundle i ucitavaj
        // 3. taj objekat daj prezenteru pre attacha
        // 4. taj objekat ubacujes u subtitle line VSO-jeve, taman neces imati sto puta iste atribute u njima



        // TODO: verovatno uri do fajla?
//		if(savedInstanceState == null) {
//			Uri data = getIntent().getData();
//			if (data == null) {
//				subHandler.ucitajAkoPostojiOdPre();
//			} else {
//				subHandler.ucitajPrevod(data);
//			}
//		}

        panelView.primeniStanje(savedInstanceState == null?
                null : savedInstanceState.getBundle(SAVE_PANEL_VIEW_STANJE));

//		primeniPerzistentnaPodesavanjaNaAdapter();
//		primeniPerzistentnaPodesavanjaNaKontrole();
//		osveziNaslov();
//
//		osveziListu();
	}

    private void setUpInterface() {
        setUpEditorControls();
        setUpAdapter();
        setUpList();
    }

    /** Kreira panele i ubacuje ih u kontrole u dnu ekrana. */
	private void setUpEditorControls() {
        editorControlsView.setKontroleStateListener(this);

        panelView = new PanelView(this, this);
		
		TextView lol2 = new TextView(this);
		lol2.setText("Second part");
		lol2.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

		TextView lol3 = new TextView(this);
		lol3.setText("Thrid section");
		lol3.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

		TextView lol4 = new TextView(this);
		lol4.setText("Foruth department");
		lol4.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

		TextView lol5 = new TextView(this);
		lol5.setText("Fifth unit");
		lol5.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		editorControlsView.namestiPanele(Arrays.asList(getResources().getDrawable(R.drawable.kontrole_search),
				getResources().getDrawable(R.drawable.kontrole_tajming),
				getResources().getDrawable(R.drawable.kontrole_stil),
				getResources().getDrawable(R.drawable.kontrole_text),
				getResources().getDrawable(R.drawable.kontrole_edit)),
				Arrays.asList(panelView.glavniLejaut, lol2, lol3, lol4, lol5));
    }

    private void setUpAdapter() {
        subtitleLinesAdapter = new IdentifiableAdapter(this);
        SubtitleLineAdapterPack slap = new SubtitleLineAdapterPack(this);
        subtitleLinesAdapter.addAdapterPack(slap);
    }

    private void setUpList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        itemRecycler.setLayoutManager(linearLayoutManager);
        itemRecycler.setAdapter(subtitleLinesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pocetna_aktivnost, menu);
        return super.onCreateOptionsMenu(menu);
    }



	// --------------------------------------------------------------------------------- USER EVENTS

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.meni_standard_create:
//				kreirajNoviPrevod();
//				break;
//			case R.id.meni_standard_load:
//				prikaziIzborPrevoda();
//				break;
//			case R.id.meni_standard_podesavanja:
//				prikaziPodesavanja();
//				break;
//			case R.id.meni_standard_save:
//				snimiPrevod();
//				break;
//			case R.id.meni_standard_help:
//				prikaziHelp();
//				break;
//			default:
//				return false;
//		}
		return true;
	}

	//	@Override // TODO
	protected void onListItemClick(ListView l, View v, int position, long id) {
//		boolean maknuo = prevodAdapter.getCursor().moveToPosition(position);
//		if(!maknuo) return;
//		RedPrevoda red = RedPrevoda.kreirajIzKursora(prevodAdapter.getCursor());
//		prikaziEditor(red.lineNumber);
	}

	@Override
	public void onKontroleMaximized() {
//		skupiListu(editorControlsView.getSirinaSekcijeDugmici(), editorControlsView.getVisinaSekcijeDugmici(),
//				editorControlsView.getTrenutnaPozicija());
	}
	@Override
	public void onKontroleMinimized() {
//		rasiriListu();
	}

	@Override
	public void onDimenzionisanaSekcijaDugmici(final int sirina, final int visina, final int rotacija) {
//		Handler h = new Handler();
//		h.post(new Runnable() {
//			@Override
//			public void run() {
//				// mora ovako, da bi odvojio fazu dimenzionisanja od faze promene lejauta
//				skupiListu(sirina, visina, rotacija);
//			}
//		});
	}

	@Override
	public void onBackPressed() {
		if(editorControlsView.isInterfejsMinimiziran())
			editorControlsView.setInterfejsMinimiziran(false);
		else if(editorControlsView.isPrikazanPanel())
			editorControlsView.skloniPrikazaniPanel();
		else super.onBackPressed();
	}


	// ---------------------------------------------------------------------------------------- Eventovi - panel View

	@Override
	public void onPritisnutoFilter(String tekst, boolean ukljucen, boolean matchCase, boolean highlight) {
//		if(!ukljucen) {
//			osveziListu();
//			prevodAdapter.clearTrazeniTekst();
//		} else {
//			if(tekst == null || tekst.equals(""))
//				// tek ukljucen - a prazno polje - ne treba nista da menjas ni da trazis osvezavanje
//				return;
//			izfiltrirajListu(tekst, matchCase, highlight);
//		}
	}

	@Override
	public void onUnetTekstZaPretragu(String tekst, boolean matchCase, boolean highlight) {
//		if(panelView.isUkljucenFilter()) {
//			izfiltrirajListu(tekst, matchCase, highlight);
//		}
	}
	@Override
	public void onPritisnutoFullScreen(boolean prikazi) {
//		primeniFullscreen(prikazi);
	}
	@Override
	public void onPritisnutoStil(boolean prikazi) {
//		prevodAdapter.setPrikaziDrugiRed(prikazi);
//		prevodAdapter.notifyDataSetChanged();
	}
	@Override
	public void onPritisnutoTagovi(boolean prikazi) {
//		prevodAdapter.setPrikaziTagove(prikazi);
//		prevodAdapter.notifyDataSetChanged();
	}
	@Override
	public void onPritisnutoTajming(boolean prikazi) {
//		prevodAdapter.setPrikaziPrviRed(prikazi);
//		prevodAdapter.notifyDataSetChanged();
	}
	@Override
	public void onPrikaziPodesavanjaFiltera() {
//		showDialog(PanelView.ID_DIJALOG_FILTER);
	}
	@Override
	public void onPromenjenaPodesavanjaFiltera(boolean matchCase, boolean highlight) {
//		izfiltrirajListu(panelView.poljeUnos.getText().toString(), matchCase, highlight);
	}

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

	private void prikaziIzborPrevoda() {
//		Intent namera = new Intent(this.getBaseContext(), FileDialog.class);
		//TODO
		// proveri jel ima SD card
		// ako nema, prikazi poruku
		// proveri zadnji folder u kom je otvarano nesto, ako ga nema otvori mnt/sdcard
//		startActivityForResult(namera, RQ_CODE_FILE_DIALOG);
        FileListerDialog fileListerDialog = FileListerDialog.createFileListerDialog(this);
        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                //your code here
            }
        });
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.ALL_FILES);
        fileListerDialog.show();
//        fileListerDialog.setDefaultDir(path);
	}

	private void prikaziPodesavanja() {
		Intent namera = new Intent(this,KategorijePodesavanjaAktivnost.class);
		startActivityForResult(namera, RQ_CODE_PODESAVANJA);
	}

	private void prikaziHelp() {
		Intent namera = new Intent(this,KategorijeHelpaAktivnost.class);
		startActivity(namera);
	}

	private void prikaziEditor(int lineNumber) {
		Intent namera = new Intent(this,PrevodilacAktivnost.class);
		namera.putExtra(PrevodilacAktivnost.INPUT_BROJ_REDA, lineNumber);
		startActivityForResult(namera, RQ_CODE_PREVODILAC_AKTIVNOST);
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch(requestCode) {
//		case RQ_CODE_FILE_DIALOG:
//			onZatvorenFajlDijalog(resultCode, data);
//			break;
//		case RQ_CODE_PREVODILAC_AKTIVNOST:
//			onZatvorenPrevodilac(resultCode, data);
//			break;
//		case RQ_CODE_PODESAVANJA:
//			boolean izmenjen = primeniPerzistentnaPodesavanjaNaAdapter();
//			if(izmenjen) prevodAdapter.notifyDataSetChanged();
//			primeniPerzistentnaPodesavanjaNaKontrole();
//			primeniFullscreen(panelView.isFullscreenOn());
//			break;
//		}
//	}

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

}

package knez.assdroid.editor;

import butterknife.BindView;
import butterknife.ButterKnife;
import knez.assdroid.App;
import knez.assdroid.common.mvp.CommonSubtitleActivity;
import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.translator.TranslatorActivity;
import knez.assdroid.R;
import knez.assdroid.common.adapter.IdentifiableAdapter;
import knez.assdroid.editor.adapter.SubtitleLineAdapterPack;
import knez.assdroid.editor.data.SubtitleLineSettings;
import knez.assdroid.editor.gui.SubtitleLineLayoutItem;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.util.AndroidUtil;
import knez.assdroid.util.gui.BgpEditText;
import solid.collections.SolidList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class EditorActivity extends CommonSubtitleActivity
        implements EditorMVP.ViewInterface, SubtitleLineLayoutItem.Callback, BgpEditText.Listener {

    private static final int REQUEST_CODE_OPEN_SUBTITLE = 1234;
    private static final int REQUEST_CODE_TRANSLATOR_ACTIVITY = 500;

    @BindView(R.id.editor_subtitle_list) protected RecyclerView itemListRecycler;
    @BindView(R.id.editor_search_view) protected BgpEditText searchView;
    @BindView(R.id.editor_center_text) protected TextView centerTextView;

    private EditorMVP.PresenterInterface presenter;
    private IdentifiableAdapter subtitleLinesAdapter;

    // TODO: zvezdica kad je editovan fajl da bude ispred imena jer se ne vidi nista

    // TODO: da probas start/stop umesto create/destroy?
    // TODO:
    // da tamo edituje i vrati
    // pa tek onda razmisljaj kako ces ona podesavanja silna sta da se vidi i tako to
    // to sve mozes i u neki drawer sa leve strane, najlakse tako
    // 2. subtitle line settings objekat snimaj u bundle i ucitavaj
    // 3. taj objekat daj prezenteru pre attacha
    // 4. taj objekat ubacujes u subtitle line VSO-jeve, taman neces imati sto puta iste atribute u njima

    // TODO: zbog komplikacija sa navigatorom, nemas vise startactivity for result. Treba ga implementirati za settings
    // (ili implementirati proveru promena nakon svakog zatvaranja)

    // TODO dodaj dugme za dodavanje nove linije prevoda (mada je malo besmisleno dok nemas edit tajminga)
    // eventualno duplicate linije na long hold

    // TODO: tablet and horizontal variant

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
    protected CommonSubtitleMVP.PresenterInterface getPresenter() {
        return presenter;
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

        itemListRecycler.setLayoutManager(linearLayoutManager);
        itemListRecycler.setAdapter(subtitleLinesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }


	// --------------------------------------------------------------------------------- USER EVENTS

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_create_subtitle:
//				kreirajNoviPrevod(); // TODO
				break;
			case R.id.menu_item_load_subtitle:
				showFileOpenSelector();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_OPEN_SUBTITLE) {
            if(resultCode != RESULT_OK) return;
            if(data == null) return;

            Uri uri = data.getData();
            if(uri == null) return;

            String filename = AndroidUtil.getFileNameFromUri(this, uri);
            presenter.onFileSelectedForLoad(uri, filename);
            return;
        } else if(requestCode == REQUEST_CODE_TRANSLATOR_ACTIVITY) {
            // TODO
//            boolean hadChanges = data.getBooleanExtra(TranslatorActivity.INSTANCE_STATE_HAD_CHANGES, false);
//			int zadnjiMenjanLine = data.getExtras().getInt(TranslatorActivity.OUTPUT_LAST_VIEWED_LINE_NUMBER);
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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSubtitleLineClicked(@NonNull SubtitleLineVso subtitleLineVso,
                                      @NonNull SubtitleLineLayoutItem layoutItem) {
	    presenter.onSubtitleLineClicked(subtitleLineVso.getId());
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

    @Override
    public void onBackPressed() { // TODO - mada mozda ne moras nista, to ce sve da bude u detached
//		if(editorControlsView.isInterfejsMinimiziran())
//			editorControlsView.setInterfejsMinimiziran(false);
//		else if(editorControlsView.isPrikazanPanel())
//			editorControlsView.skloniPrikazaniPanel();
//		else super.onBackPressed();
    }


    // ------------------------------------------------------------------------------ VIEW INTERFACE

    @Override
    public void showErrorLoadingSubtitleInvalidFormat(@NonNull String filename) {
        // TODO tostiraj ili sta god
    }

    @Override
    public void showSubtitleLines(@NonNull SolidList<SubtitleLineVso> subtitleLineVsos) {
        subtitleLinesAdapter.setItems(subtitleLineVsos);
        updateCenterText();
    }

    @Override
    public void showSubtitleLines(@NonNull SolidList<SubtitleLineVso> subtitleLineVsos,
                                  @NonNull DiffUtil.DiffResult diffResult) {
        subtitleLinesAdapter.setItemsDontNotify(subtitleLineVsos);
        diffResult.dispatchUpdatesTo(subtitleLinesAdapter);
        updateCenterText();
    }

    @Override
    public void showCurrentSubtitleLineSettings(@NonNull SubtitleLineSettings subtitleLineSettings) {
        // TODO
    }

    @Override
    public void removeAllCurrentSubtitleData() {
	    subtitleLinesAdapter.clear();
	    searchView.setText(""); // TODO dal ovo puca listener
    }

    @Override
    public void showTranslatorScreen(long lineId) {
        Intent translatorIntent = new Intent(this, TranslatorActivity.class);
        translatorIntent.putExtra(TranslatorActivity.INPUT_LINE_ID, lineId);
        startActivityForResult(translatorIntent, REQUEST_CODE_TRANSLATOR_ACTIVITY);
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    private void updateCenterText() {
        // TODO vidi samo sad imas tipa no subtitle lines i no results kad je filtriranje ukljuceno
//        if(adapter.getItemCount() == 0 && centerTextView.getVisibility() == View.GONE)
//            FadeAnimationHelper.fadeView(true, centerTextView, false);
//        else if(adapter.getItemCount() != 0 && centerTextView.getVisibility() == View.VISIBLE)
//            FadeAnimationHelper.fadeView(false, centerTextView, false);
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


    // ------------------------------------------------------------------------------------ INTERNAL

    private void showFileOpenSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_SUBTITLE);
    }

}

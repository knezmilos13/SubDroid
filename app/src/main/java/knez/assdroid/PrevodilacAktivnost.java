package knez.assdroid;

import java.io.FileNotFoundException;

import knez.assdroid.help.HelpEditorAkt;
import knez.assdroid.subtitle.data.SubtitleLine;
import knez.assdroid.subtitle.SubtitleController;
import knez.assdroid.podesavanja.PodesavanjaPrevodilacAktivnost;
import knez.assdroid.podesavanja.PodesavanjaPrevodilacUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PrevodilacAktivnost extends Activity implements OnClickListener, OnKeyListener, OnEditorActionListener {

	public static final String INPUT_BROJ_REDA = "input_broj_reda";
	public static final String OUTPUT_RADJENE_IZMENE_OVDE = "save_radjene_izmene";
	public static final String OUTPUT_ZADNJI_PREGLEDAN = "output_zadnji_pregledan";

	private static final int DEFAULT_BROJ_REDA = 1;
	private static final int ID_ACTION_DUGMETA = 1234;
	private static final int ID_AKTIVNOSTI_PODESAVANJA = 1;
	private static final String PREFERENCE_FAJL = "global_podesavanja";

	private TextView labelaPrethodniRed, labelaTekuciRed, labelaSledeciRed;
	private EditText unos;
	private Button dugmeCopy, dugmeCommit, dugmeCommitNext;

	private SubtitleLine prethodniRed, tekuciRed, sledeciRed;
	private SubtitleController subtitleController;

	// iako postoji evidencija globalno da li je prevod menjan, ovo je zgodno znati da bi se vratila
	// informacija editoru da li treba da refreshuje ista
	private boolean radjeneIzmeneOvde = false;


	// ------------------------------------------------------------------------------ Zivotni ciklus

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.akt_prevodilac);

		// TODO ovo u prezenter
		subtitleController = App.getAppComponent().getSubtitleHandler();

		Bundle bandl;
		if(savedInstanceState != null) {
			bandl = savedInstanceState;
		} else if(getIntent().getExtras() != null) {
			bandl = getIntent().getExtras();
		} else {
			throw new IllegalStateException("Nije primljen Bundle sa podacima!");
		}
		int brojReda = bandl.getInt(INPUT_BROJ_REDA, DEFAULT_BROJ_REDA);

		radjeneIzmeneOvde = bandl.getBoolean(OUTPUT_RADJENE_IZMENE_OVDE, false);

		pokupiPoglede();
		dodajListenere();

		namestiRed(brojReda);

		osveziNaslov();
	}

	private void pokupiPoglede() {
		labelaPrethodniRed = findViewById(R.id.prevodilac_prethodna_linija);
		labelaTekuciRed = findViewById(R.id.prevodilac_tekuca_linija);
		labelaSledeciRed = findViewById(R.id.prevodilac_sledeca_linija);
		unos = findViewById(R.id.prevodilac_unos);
		dugmeCopy = findViewById(R.id.prevodilac_dugme_kopiraj);
		dugmeCommit = findViewById(R.id.prevodilac_dugme_commit);
		dugmeCommitNext = findViewById(R.id.prevodilac_dugme_commit_next);
	}

	private void dodajListenere() {
		labelaPrethodniRed.setOnClickListener(this);
		labelaSledeciRed.setOnClickListener(this);
		dugmeCopy.setOnClickListener(this);
		dugmeCommit.setOnClickListener(this);
		dugmeCommitNext.setOnClickListener(this);

		unos.setOnKeyListener(this);
		unos.setImeActionLabel(getResources().getString(R.string.prevodilac_samo_next), ID_ACTION_DUGMETA);
		unos.setOnEditorActionListener(this);
		unos.addTextChangedListener(spremiNadgledacTeksta());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(INPUT_BROJ_REDA, tekuciRed.getLineNumber());
		outState.putBoolean(OUTPUT_RADJENE_IZMENE_OVDE, radjeneIzmeneOvde);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed() {
		Intent output = new Intent();
		output.putExtra(OUTPUT_RADJENE_IZMENE_OVDE, radjeneIzmeneOvde);
		output.putExtra(OUTPUT_ZADNJI_PREGLEDAN, tekuciRed.getLineNumber());
		setResult(RESULT_OK, output);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_translator, menu);
		return true;
	}


	// ----------------------------------------------------------------------------------- Interfejs
	
	/** Prikazuje tekst tekuce ucitanih redova u odgovarajucim labelama. */
	private void prikaziRedove() {
			labelaPrethodniRed.setText(prethodniRed == null? " " : prethodniRed.getText());
		labelaTekuciRed.setText(tekuciRed.getText());
		labelaSledeciRed.setText(sledeciRed == null? " " : sledeciRed.getText());
	}
	
	/** Primenjuje podesavanja koja se odnose na prikaz polja za unos. */
	private void primeniUnosPodesavanja() {
		if(PodesavanjaPrevodilacUtil.isPrevodilacHintOn() && unos.getText().toString().equals("")) {
			unos.setHint(tekuciRed.getText()); //pazi da iza hinta imas setText inace mozda nece raditi
			unos.setText("");
		} else {
			unos.setHint(null);
		}
		
		if(PodesavanjaPrevodilacUtil.isAlwaysCopyOn() && unos.getText().toString().equals("")) {
			unos.setText(tekuciRed.getText());
		}
	}
	
	/** Sklapa i prikazuje naslov ove aktivnosti koji zavisi od imena prevoda i njegovog statusa snimljenosti. */
	private void osveziNaslov() {
//		String ceoNaslov =
//				subtitleController.isPrevodMenjan()? getResources().getString(R.string.editor_prevod_menjan_znak) : "";
//		if(subtitleController.getImePrevoda().equals("")) {
//			ceoNaslov += getResources().getString(R.string.standard_untitled);
//		} else {
//			ceoNaslov += subtitleController.getImePrevoda();
//		}
//
//		setTitle(ceoNaslov);
	}


	// ----------------------------------------------------------------------- Manipulacija prevodom

	/** Ucitava, prikazuje zadati red prevoda i u skladu sa time modifikuje ostatak interfejsa. */
	private void namestiRed(int lineNumber) {
		ucitajRedove(lineNumber);
		prikaziRedove();
		unos.setText("");
		primeniUnosPodesavanja();
	}
	
	private void ucitajRedove(int tekuci) {
//		prethodniRed = tekuci>1 ? subtitleController.dajRedPrevoda(tekuci - 1) : null;
//		tekuciRed = subtitleController.dajRedPrevoda(tekuci);
//		sledeciRed = subtitleController.postojiLiRedPrevoda(tekuci + 1)? subtitleController.dajRedPrevoda(tekuci + 1) : null;
	}
	
	/** Primenjuje izmene na tekucu liniju prevoda i osvezava naslov aktivnosti */
	private void commitujIzmene() {
//		if(PodesavanjaPrevodilacUtil.isCommitKeepOriginalOn() && unos.getText().toString().equals("")) {
			// ako commit prazne linije ne menja nista, a jeste bila prazna linija... do nothing
//		} else {
//			tekuciRed.text = unos.getText().toString(); // TODO: nece da moze setText - immutable tebra
//			subtitleController.updateRedPrevoda(tekuciRed);
//			radjeneIzmeneOvde = true;
//			if(!subtitleController.isPrevodMenjan()) {
//				subtitleController.setPrevodMenjan(true);
//				osveziNaslov();
//			}
//		}
	}
	
	private void premotajNaSledeciRed() {
		if(sledeciRed != null)
			namestiRed(sledeciRed.getLineNumber());
		else
			osveziTekuciRed();
	}
	
	private void premotajNaPrethodniRed() {
		if(prethodniRed != null)
			namestiRed(prethodniRed.getLineNumber());
		else
			osveziTekuciRed();
	}
	
	private void osveziTekuciRed() {
		labelaTekuciRed.setText(tekuciRed.getText());
	}
	
	private void snimiPrevod() {
//		try {
//			subtitleController.snimiPrevod();
//		} catch (FileNotFoundException e) {
//			Loger.log(e);
//			e.printStackTrace();
//			//TODO ne postoji fajl... a ovo je save... da je saveas pa ajde
//			// u ovoj varijanti u prevodiocu moze da ga snimi negde na SD kao temp fajl i da ispise obavestenje
//		}
//		osveziNaslov();
	}
	
	// ----------------------------------------------------------------------------------------------------- Eventovi

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.prevodilac_prethodna_linija:
			premotajNaPrethodniRed();
			break;
		case R.id.prevodilac_sledeca_linija:
			premotajNaSledeciRed();
			break;
		case R.id.prevodilac_dugme_commit:
			commitujIzmene();
			prikaziRedove();
			break;
		case R.id.prevodilac_dugme_commit_next:
			commitujIzmene();
			premotajNaSledeciRed();
			break;
		case R.id.prevodilac_dugme_kopiraj:
			unos.setText(tekuciRed.getText());
			break;
		}
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.meni_standard_podesavanja:
			prikaziPodesavanja();
			break;
		case R.id.meni_standard_save:
			snimiPrevod();
			break;
		case R.id.meni_standard_help:
			prikaziHelp();
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				commitujIzmene();
				premotajNaSledeciRed();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == ID_ACTION_DUGMETA) {
			commitujIzmene();
			premotajNaSledeciRed();
			return true;
		}
		return false;
	}
	
	private TextWatcher spremiNadgledacTeksta() {
		return new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				String tekst = s.toString();
				if(tekst.contains("\n")) {
					tekst = tekst.replace("\n", "\\N");
					unos.setText(tekst);
					unos.setSelection(unos.getText().length());
				}
			}
		};
	}


	// ---------------------------------------------------------------------- Startovanje aktivnosti

	private void prikaziPodesavanja() {
		Intent namera = new Intent(this,PodesavanjaPrevodilacAktivnost.class);
		startActivityForResult(namera, ID_AKTIVNOSTI_PODESAVANJA);
	}

	private void prikaziHelp() {
		Intent namera = new Intent(this,HelpEditorAkt.class);
		startActivity(namera);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ID_AKTIVNOSTI_PODESAVANJA) {
			primeniUnosPodesavanja();
		}
	}

}

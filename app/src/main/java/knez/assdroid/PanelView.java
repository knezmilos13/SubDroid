package knez.assdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/** Klasa cija je jedina uloga... da izdvoji parce koda koje se odnosi na View panel da ne bih zatrpavao
 * samu aktivnost time. Aktivnost ce imati pun pristup (public) poljima ovog panela.
 * Eventovi koji mogu u potpunosti da se obrade ovde ce biti obradjeni ovde, inace ce biti prosledjeni preko
 * posebnog callback interfejsa (PanelViewListener)
 */
public class PanelView implements OnClickListener, TextWatcher {

	public static final int ID_DIJALOG_FILTER = 495347;
	private static final String PREFERENCE_FAJL = "global_podesavanja";

	private PanelViewListener listener;
	private Activity roditelj;

	public ScrollView glavniLejaut;
	public EditText poljeUnos;
	public Button dugmeFilter;
	public ImageButton dugmeVidiTajming, dugmeVidiStajl, dugmeVidiTagove, dugmeFullscreen, dugmeFilterPodesavanja;

	private boolean prikazujLinijuTajming, prikazujLinijuStil, prikazujTagove, fullscreenOn;
	private boolean ukljucenFilter, ukljucenFilterMatchCase, ukljucenFilterHajlajt;
	private static final String SAVE_VIEW_TAJMING_UKLJUCEN = "view_tajming_ukljucen";
	private static final String SAVE_VIEW_TAGOVI_UKLJUCEN = "view_tagovi_ukljucen";
	private static final String SAVE_VIEW_STAJL_UKLJUCEN = "view_stajl_ukljucen";
	private static final String SAVE_VIEW_FULLSCREEN_UKLJUCEN = "view_fullscreen_ukljucen";
	private static final String SAVE_FILTER_UKLJUCEN = "view_filter_ukljucen";
	private static final String SAVE_VIEW_FILTER_MATCH_CASE = "view_filter_match_case";
	private static final String SAVE_VIEW_FILTER_HIGHLIGHT = "view_filter_highlight";

	private static final boolean DEFAULT_FILTER_UKLJUCEN = false;

	// ---------------------------------------------------------------------------------------------- Inicijalizacija

	public PanelView(Activity roditelj, PanelViewListener listener) {
		this.listener = listener;
		this.roditelj = roditelj;
		pokupiPoglede(roditelj);
		dodajListenere();
	}

	private void pokupiPoglede(Activity roditelj) {
		glavniLejaut = (ScrollView) LayoutInflater.from(roditelj).inflate(R.layout.panel_view, null);
		glavniLejaut.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		poljeUnos = glavniLejaut.findViewById(R.id.panel_view_unos_search);
		dugmeFilter = glavniLejaut.findViewById(R.id.panel_view_dugme_filter);
		dugmeFilterPodesavanja = glavniLejaut.findViewById(R.id.panel_view_dugme_filter_podesavanja);
		dugmeFilterPodesavanja.setBackgroundResource(android.R.drawable.btn_default);
		dugmeVidiTajming = glavniLejaut.findViewById(R.id.panel_view_dugme_tajming);
		dugmeVidiStajl = glavniLejaut.findViewById(R.id.panel_view_dugme_stil);
		dugmeVidiTagove = glavniLejaut.findViewById(R.id.panel_view_dugme_tagovi);
		dugmeFullscreen = glavniLejaut.findViewById(R.id.panel_view_dugme_fullscreen);
	}

	private void dodajListenere() {
		dugmeFilter.setOnClickListener(this);
		dugmeFilterPodesavanja.setOnClickListener(this);
		dugmeVidiTajming.setOnClickListener(this);
		dugmeVidiTagove.setOnClickListener(this);
		dugmeVidiStajl.setOnClickListener(this);
		dugmeFullscreen.setOnClickListener(this);
		poljeUnos.addTextChangedListener(this);
	}

	/** Vraca stanje panel-podesavanja u obliku Bundlea. */
	public Bundle dajStanje() {
		Bundle stanje = new Bundle();
		stanje.putBoolean(SAVE_VIEW_TAJMING_UKLJUCEN, prikazujLinijuTajming);
		stanje.putBoolean(SAVE_VIEW_TAGOVI_UKLJUCEN, prikazujTagove);
		stanje.putBoolean(SAVE_VIEW_STAJL_UKLJUCEN, prikazujLinijuStil);
		stanje.putBoolean(SAVE_VIEW_FULLSCREEN_UKLJUCEN, fullscreenOn);
		stanje.putBoolean(SAVE_FILTER_UKLJUCEN, ukljucenFilter);
		stanje.putBoolean(SAVE_VIEW_FILTER_MATCH_CASE, ukljucenFilterMatchCase);
		stanje.putBoolean(SAVE_VIEW_FILTER_HIGHLIGHT, ukljucenFilterHajlajt);
		return stanje;
	}

	/** Ucitava ranije snimljeno stanje iz primljenog bandla. U pitanju je stanje mojih custom vrednosti, ne 
	 * vizualno stanje interfejsa koje ce se automatski restorirati kada dodje vreme za to. */
	public void primeniStanje(Bundle bandl) {
		SharedPreferences pref = roditelj.getSharedPreferences(PREFERENCE_FAJL, 0);
		Resources r = roditelj.getResources();

		// ucitas prethodno zapamcene, ili default ako ih nema
		prikazujLinijuTajming = pref.getBoolean(SAVE_VIEW_TAJMING_UKLJUCEN, 
				r.getBoolean(R.bool.panel_view_default_tajming));
		prikazujTagove = pref.getBoolean(SAVE_VIEW_TAGOVI_UKLJUCEN, 
				r.getBoolean(R.bool.panel_view_default_tagovi));
		prikazujLinijuStil = pref.getBoolean(SAVE_VIEW_STAJL_UKLJUCEN, 
				r.getBoolean(R.bool.panel_view_default_stajl));
		fullscreenOn = pref.getBoolean(SAVE_VIEW_FULLSCREEN_UKLJUCEN, 
				r.getBoolean(R.bool.panel_view_default_fullscreen));
		ukljucenFilter = DEFAULT_FILTER_UKLJUCEN; //ovo ne ide u preference
		ukljucenFilterMatchCase = pref.getBoolean(SAVE_VIEW_FILTER_MATCH_CASE, 
				r.getBoolean(R.bool.panel_view_default_filter_match_case));
		ukljucenFilterHajlajt = pref.getBoolean(SAVE_VIEW_FILTER_HIGHLIGHT, 
				r.getBoolean(R.bool.panel_view_default_filter_highlight));

		// ako imas stanje od pre, primeni njega, a za svaki koji nemas primeni ono od iznad
		if(bandl != null) {
			prikazujLinijuTajming = bandl.getBoolean(SAVE_VIEW_TAJMING_UKLJUCEN, prikazujLinijuTajming);
			prikazujTagove = bandl.getBoolean(SAVE_VIEW_TAGOVI_UKLJUCEN, prikazujTagove);
			prikazujLinijuStil = bandl.getBoolean(SAVE_VIEW_STAJL_UKLJUCEN, prikazujLinijuStil);
			fullscreenOn = bandl.getBoolean(SAVE_VIEW_FULLSCREEN_UKLJUCEN, fullscreenOn);
			ukljucenFilter = bandl.getBoolean(SAVE_FILTER_UKLJUCEN, ukljucenFilter);
			ukljucenFilterMatchCase = bandl.getBoolean(SAVE_VIEW_FILTER_MATCH_CASE, ukljucenFilterMatchCase);
			ukljucenFilterHajlajt = bandl.getBoolean(SAVE_VIEW_FILTER_HIGHLIGHT, ukljucenFilterHajlajt);
		}

		promeniStatusDugmeta(dugmeFullscreen, fullscreenOn);
		promeniStatusDugmeta(dugmeVidiStajl, prikazujLinijuStil);
		promeniStatusDugmeta(dugmeVidiTagove, prikazujTagove);
		promeniStatusDugmeta(dugmeVidiTajming, prikazujLinijuTajming);
		promeniStatusDugmeta(dugmeFilter, ukljucenFilter);
	}

	public void perzistirajStanje() {
		SharedPreferences.Editor editor = roditelj.getSharedPreferences(PREFERENCE_FAJL, 0).edit();
		editor.putBoolean(PanelView.SAVE_VIEW_TAJMING_UKLJUCEN, prikazujLinijuTajming);
		editor.putBoolean(PanelView.SAVE_VIEW_TAGOVI_UKLJUCEN, prikazujTagove);
		editor.putBoolean(PanelView.SAVE_VIEW_STAJL_UKLJUCEN, prikazujLinijuStil);
		editor.putBoolean(PanelView.SAVE_VIEW_FULLSCREEN_UKLJUCEN, fullscreenOn);
		editor.putBoolean(PanelView.SAVE_VIEW_FILTER_HIGHLIGHT, ukljucenFilterHajlajt);
		editor.putBoolean(PanelView.SAVE_VIEW_FILTER_MATCH_CASE, ukljucenFilterMatchCase);
		editor.apply();
	}

	// ----------------------------------------------------------------------------------------------------- Dijalozi

	public Dialog kreirajDijalogFilter() {
		AlertDialog.Builder adb = new AlertDialog.Builder(roditelj);
		LayoutInflater li = LayoutInflater.from(roditelj);
		adb.setTitle(R.string.editor_view_dijalog_filter_naslov);
		DijalogFilterListener dfl = new DijalogFilterListener();
		adb.setPositiveButton(R.string.editor_view_dijalog_filter_dugme_ok, dfl);
		adb.setNegativeButton(R.string.editor_view_dijalog_filter_dugme_cancel, dfl);
		adb.setView(li.inflate(R.layout.dijalog_filter, null));		
		return adb.create();
	}

	public void osveziDijalogFilter(Dialog dialog) {
		((CheckBox) dialog.findViewById(R.id.panel_view_dij_filter_match_case)).setChecked(ukljucenFilterMatchCase);
		((CheckBox) dialog.findViewById(R.id.panel_view_dij_filter_highlight)).setChecked(ukljucenFilterHajlajt);
	}

	private class DijalogFilterListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(which == Dialog.BUTTON_POSITIVE) {
				ukljucenFilterMatchCase = ((CheckBox)((AlertDialog)dialog).findViewById(R.id.panel_view_dij_filter_match_case)).isChecked();
				ukljucenFilterHajlajt = ((CheckBox)((AlertDialog)dialog).findViewById(R.id.panel_view_dij_filter_highlight)).isChecked();
				listener.onPromenjenaPodesavanjaFiltera(ukljucenFilterMatchCase, ukljucenFilterHajlajt);
			}			
		}
	}

	// ---------------------------------------------------------------------------------------------------- Interfejs

	private void promeniStatusDugmeta(View dugme, boolean selektovano) {
		if(selektovano) {
			StateListDrawable sld = (StateListDrawable) dugme.getContext().getResources().getDrawable(android.R.drawable.btn_default);
			sld.setState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled, 
					android.R.attr.state_focused, android.R.attr.state_selected, android.R.attr.state_window_focused });
			dugme.setBackground(sld.getCurrent());
		} else {
			dugme.setBackgroundResource(android.R.drawable.btn_default);
		}
	}

	// ---------------------------------------------------------------------------------------------------- Listeneri

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.panel_view_dugme_filter:
			setUkljucenFilter(!ukljucenFilter);
			listener.onPritisnutoFilter(poljeUnos.getText().toString(), isUkljucenFilter(),
					ukljucenFilterMatchCase, ukljucenFilterHajlajt);
			break;
		case R.id.panel_view_dugme_filter_podesavanja:
			listener.onPrikaziPodesavanjaFiltera();
			break;
		case R.id.panel_view_dugme_tajming:
			setPrikazujLinijuTajming(!prikazujLinijuTajming);
			listener.onPritisnutoTajming(isPrikazujLinijuTajming());
			break;
		case R.id.panel_view_dugme_stil:
			setPrikazujLinijuStil(!prikazujLinijuStil);
			listener.onPritisnutoStil(isPrikazujLinijuStil());
			break;
		case R.id.panel_view_dugme_tagovi:
			setPrikazujTagove(!prikazujTagove);
			listener.onPritisnutoTagovi(isPrikazujTagove());
			break;
		case R.id.panel_view_dugme_fullscreen:
			setFullscreenOn(!fullscreenOn);
			listener.onPritisnutoFullScreen(isFullscreenOn());
			break;
		}
	}

	@Override public void afterTextChanged(Editable arg0) {}
	@Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		listener.onUnetTekstZaPretragu(arg0.toString(), ukljucenFilterMatchCase, ukljucenFilterHajlajt);
	}

	public interface PanelViewListener {
		void onPritisnutoFilter(String tekst, boolean ukljucen, boolean matchCase, boolean highlight);
		void onPrikaziPodesavanjaFiltera();
		void onPromenjenaPodesavanjaFiltera(boolean matchCase, boolean highlight);
		void onPritisnutoTajming(boolean prikazi);
		void onPritisnutoStil(boolean prikazi);
		void onPritisnutoTagovi(boolean prikazi);
		void onPritisnutoFullScreen(boolean prikazi);
		void onUnetTekstZaPretragu(String tekst, boolean matchCase, boolean highlight);
	}

	// ---------------------------------------------------------------------------------------------- Geteri/seteri

	public boolean isFullscreenOn() {
		return fullscreenOn;
	}
	public boolean isPrikazujLinijuStil() {
		return prikazujLinijuStil;
	}
	public boolean isPrikazujTagove() {
		return prikazujTagove;
	}
	public boolean isPrikazujLinijuTajming() {
		return prikazujLinijuTajming;
	}
	public boolean isUkljucenFilter() {
		return ukljucenFilter;
	}
	public boolean isUkljucenFilterHajlajt() {
		return ukljucenFilterHajlajt;
	}
	public boolean isUkljucenFilterMatchCase() {
		return ukljucenFilterMatchCase;
	}

	public void setPrikazujLinijuTajming(boolean prikazujLinijuTajming) {
		this.prikazujLinijuTajming = prikazujLinijuTajming;
		promeniStatusDugmeta(dugmeVidiTajming, prikazujLinijuTajming);
	}
	public void setPrikazujTagove(boolean prikazujTagove) {
		this.prikazujTagove = prikazujTagove;
		promeniStatusDugmeta(dugmeVidiTagove, prikazujTagove);
	}
	public void setPrikazujLinijuStil(boolean prikazujLinijuStil) {
		this.prikazujLinijuStil = prikazujLinijuStil;
		promeniStatusDugmeta(dugmeVidiStajl, prikazujLinijuStil);
	}
	public void setFullscreenOn(boolean fullscreenOn) {
		this.fullscreenOn = fullscreenOn;
		promeniStatusDugmeta(dugmeFullscreen, fullscreenOn);
	}
	public void setUkljucenFilter(boolean ukljucenFilter) {
		this.ukljucenFilter = ukljucenFilter;
		promeniStatusDugmeta(dugmeFilter, ukljucenFilter);
	}
	public void setUkljucenFilterMatchCase(boolean ukljucenFilterMatchCase) {
		this.ukljucenFilterMatchCase = ukljucenFilterMatchCase;
	}
	public void setUkljucenFilterHighlight(boolean ukljucenFilterHajlajt) {
		this.ukljucenFilterHajlajt = ukljucenFilterHajlajt;
	}

}

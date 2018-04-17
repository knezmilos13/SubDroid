package knez.assdroid;

import java.util.ArrayList;
import java.util.List;

import knez.assdroid.gui.MeasurableLinearLayout;
import knez.assdroid.gui.MeasurableLinearLayout.OnIzmeren;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class EditorControlsView extends RelativeLayout implements OnClickListener, OnIzmeren {

	private static final int INTEGER_NIJE_SPECIFICIRAN = -1;
	private static final int ID_SEKCIJE_DUGMICI = 100099;
	private static final int TAG_DUGME_MINIMIZE = -100;
	private static final int TAG_DUGME_MAXIMIZE = -101;
	
	private int zeljenaSirinaDugmica = INTEGER_NIJE_SPECIFICIRAN;
	private int zeljenaVisinaDugmica = INTEGER_NIJE_SPECIFICIRAN;

	public static final int POZICIJA_DOLE = 1;
	public static final int POZICIJA_GORE = 2;
	public static final int POZICIJA_LEVO = 3;
	public static final int POZICIJA_DESNO = 4;
	public static final int POZICIJA_AUTO = 5;
	private int pozicijaLandscape = INTEGER_NIJE_SPECIFICIRAN;
	private int pozicijaPortrait = INTEGER_NIJE_SPECIFICIRAN;
	private static final int POZICIJA_PORTRAIT_DEFAULT = POZICIJA_DOLE;
	private static final int POZICIJA_LANDSCAPE_DEFAULT = POZICIJA_DESNO;
	private int trenutnaPozicija = INTEGER_NIJE_SPECIFICIRAN;

	private List<ImageButton> dugmad;
	private List<LinearLayout> paneli;
	private MeasurableLinearLayout sekcijaDugmici; 
	private LinearLayout sekcijaMinimized;
	private ImageButton dugmeMinimize, dugmeMaximize;
	private Drawable slikaMinimize, slikaMaximize;
	private int slikaPozadinaSekcije, slikaPozadinaDugmici, slikaPozadinaMinimized, slikaPozadinaSekcijeLevo,
	slikaPozadinaDugmiciLevo, slikaPozadinaMinimizedLevo, slikaPozadinaSekcijeDesno, slikaPozadinaDugmiciDesno,
	slikaPozadinaSekcijeGore, slikaPozadinaDugmiciGore, slikaPozadinaMinimizedGore;
	
	private static final boolean DEFAULT_MINIMIZIRAN = false;
	private boolean interfejsMinimiziran = DEFAULT_MINIMIZIRAN;
	private static final int NIJEDAN_PRIKAZAN = -1;
	private static final int DEFAULT_PRIKAZAN = NIJEDAN_PRIKAZAN;
	private int prikazanPanel = DEFAULT_PRIKAZAN;
	private KontroleStateListener listener;
	private int transparentnost = INTEGER_NIJE_SPECIFICIRAN;
	
	private static final String SAVE_MINIMIZED = "kontrole_save_minimized";
	private static final String SAVE_PRIKAZAN_PANEL = "kontrole_save_prikazan_panel";
	private static final String SAVE_ORIGINAL = "kontrole_save_original";

	// ---------------------------------------------------------------------------------------------- Inicijalizacija

	public EditorControlsView(Context context) {
		super(context);
		init(null);
	}
	public EditorControlsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	public EditorControlsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		Bundle ban = new Bundle();
		ban.putParcelable(SAVE_ORIGINAL, p);
		ban.putInt(SAVE_PRIKAZAN_PANEL, prikazanPanel);
		ban.putBoolean(SAVE_MINIMIZED, interfejsMinimiziran);
		return ban;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle) {
			Bundle ban = (Bundle) state;
			int panelZaPrikaz = ban.getInt(SAVE_PRIKAZAN_PANEL, DEFAULT_PRIKAZAN);
			if(panelZaPrikaz != NIJEDAN_PRIKAZAN) prikaziPanel(panelZaPrikaz);
			interfejsMinimiziran = ban.getBoolean(SAVE_MINIMIZED,DEFAULT_MINIMIZIRAN);
			setStanjeMinimiziranosti(interfejsMinimiziran);
			super.onRestoreInstanceState(ban.getParcelable(SAVE_ORIGINAL));
		} else {
			super.onRestoreInstanceState(state);
		}
	}
	
	public void setKontroleStateListener(KontroleStateListener listener) {
		this.listener = listener;
	}

	private void init(AttributeSet attrs) {
		dugmad = new ArrayList<>();
		paneli = new ArrayList<>();

		setBackgroundColor(Color.TRANSPARENT);

		ucitajAtribute(attrs);

		sekcijaDugmici = new MeasurableLinearLayout(getContext());
		sekcijaDugmici.setGravity(Gravity.CENTER);
		sekcijaDugmici.setId(ID_SEKCIJE_DUGMICI);
		sekcijaDugmici.setListener(this);
				
		dugmeMinimize = kreirajDugme(slikaMinimize);
		dugmeMinimize.setTag(TAG_DUGME_MINIMIZE);
		dugmeMinimize.setOnClickListener(this);
		
		dugmeMaximize = kreirajDugme(slikaMaximize);
		dugmeMaximize.setTag(TAG_DUGME_MAXIMIZE);
		dugmeMaximize.setOnClickListener(this);
		
		sekcijaMinimized = new LinearLayout(getContext());
		sekcijaMinimized.addView(dugmeMaximize);
		
		this.addView(sekcijaDugmici);
		this.addView(sekcijaMinimized);
	}

	private void ucitajAtribute(AttributeSet attrs) {
		if(attrs == null) return;

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.atributi_kontrole);
		
		ucitajDimenzije(a);
		ucitajPozicije(a);
		ucitajDrawables(a);
		
		a.recycle();
	}
	
	private void ucitajDimenzije(TypedArray a) {
		zeljenaSirinaDugmica = Math.round(a.getDimension(R.styleable.atributi_kontrole_zeljena_sirina_dugmica, 
				INTEGER_NIJE_SPECIFICIRAN));
		zeljenaVisinaDugmica = Math.round(a.getDimension(R.styleable.atributi_kontrole_zeljena_visina_dugmica, 
				INTEGER_NIJE_SPECIFICIRAN));
	}

	private void ucitajPozicije(TypedArray a) {
		pozicijaLandscape = a.getInteger(R.styleable.atributi_kontrole_pozicija_landscape, 
				POZICIJA_LANDSCAPE_DEFAULT);
		pozicijaPortrait = a.getInteger(R.styleable.atributi_kontrole_pozicija_portrait, 
				POZICIJA_PORTRAIT_DEFAULT);
	}

	private void ucitajDrawables(TypedArray a) {
		Resources r = getResources();
		int temp;

		temp = a.getResourceId(R.styleable.atributi_kontrole_ikonica_minimize, INTEGER_NIJE_SPECIFICIRAN);
		slikaMinimize = temp == INTEGER_NIJE_SPECIFICIRAN ?
				new ColorDrawable(Color.TRANSPARENT) : r.getDrawable(temp);
		temp = a.getResourceId(R.styleable.atributi_kontrole_ikonica_maximize, INTEGER_NIJE_SPECIFICIRAN);
		slikaMaximize = temp == INTEGER_NIJE_SPECIFICIRAN ?
				new ColorDrawable(Color.TRANSPARENT) : r.getDrawable(temp);
				
		slikaPozadinaDugmici = a.getResourceId(R.styleable.atributi_kontrole_pozadina_dugmici, INTEGER_NIJE_SPECIFICIRAN);
		slikaPozadinaSekcije = a.getResourceId(R.styleable.atributi_kontrole_pozadina_sekcije, INTEGER_NIJE_SPECIFICIRAN);
		slikaPozadinaMinimized = a.getResourceId(R.styleable.atributi_kontrole_pozadina_minimized, INTEGER_NIJE_SPECIFICIRAN);
		
		slikaPozadinaDugmiciDesno = a.getResourceId(R.styleable.atributi_kontrole_pozadina_dugmici_desno, slikaPozadinaDugmici);
		slikaPozadinaSekcijeDesno = a.getResourceId(R.styleable.atributi_kontrole_pozadina_sekcije_desno, slikaPozadinaSekcije);
		
		slikaPozadinaDugmiciLevo = a.getResourceId(R.styleable.atributi_kontrole_pozadina_dugmici_levo, slikaPozadinaDugmici);
		slikaPozadinaSekcijeLevo = a.getResourceId(R.styleable.atributi_kontrole_pozadina_sekcije_levo, slikaPozadinaSekcije);
		slikaPozadinaMinimizedLevo = a.getResourceId(R.styleable.atributi_kontrole_pozadina_minimized_levo, slikaPozadinaMinimized);
		
		slikaPozadinaDugmiciGore = a.getResourceId(R.styleable.atributi_kontrole_pozadina_dugmici_gore, slikaPozadinaDugmici);
		slikaPozadinaSekcijeGore = a.getResourceId(R.styleable.atributi_kontrole_pozadina_sekcije_gore, slikaPozadinaSekcije);
		slikaPozadinaMinimizedGore = a.getResourceId(R.styleable.atributi_kontrole_pozadina_minimized_gore, slikaPozadinaMinimized);
	}
	
	/** Kreira standardno dugme za red sa dugmicima. */
	private ImageButton kreirajDugme(Drawable slika) {
		ImageButton dugme = new ImageButton(getContext());
		dugme.setScaleType(ScaleType.CENTER_INSIDE);
		dugme.setImageDrawable(slika);
		dugme.setBackgroundResource(android.R.drawable.btn_default);
		return dugme;
	}

	// --------------------------------------------------------------------------------- Sredjivanje dugmica i panela

	public void namestiPanele(List<Drawable> slikeDugmici, List<View> paneli) {
		if(slikeDugmici.size() == 0 || paneli.size() == 0 || slikeDugmici.size()!=paneli.size())
			throw new IllegalStateException();
		
		// cisti sve
		sekcijaDugmici.removeAllViews();
		for(View brisiPanel : this.paneli)
			this.removeView(brisiPanel);
		dugmad.clear();
		this.paneli.clear();
		
		ImageButton dugme;
		LinearLayout mojPanel;
		for(int i = 0; i < slikeDugmici.size(); i++) {
			// kreiraj/ubaci dugme
			dugme = kreirajDugme(slikeDugmici.get(i));
			dugme.setTag(i);
			dugme.setOnClickListener(this);
			dugmad.add(dugme);
			sekcijaDugmici.addView(dugme);
			
			// kreiraj/ubaci panel (nevidljiv za sada)
			mojPanel = new LinearLayout(getContext());
			mojPanel.setBackgroundColor(Color.GREEN);
			mojPanel.setGravity(Gravity.CENTER);
			mojPanel.addView(paneli.get(i));
			this.paneli.add(mojPanel);
			this.addView(mojPanel);
			
			if(i!=prikazanPanel) {
				mojPanel.setVisibility(View.GONE);
			} else {
				prikaziPanel(i);
			}
		}
		
		sekcijaDugmici.addView(dugmeMinimize);
		setStanjeMinimiziranosti(interfejsMinimiziran);
	}

	// ---------------------------------------------------------------------------------------- 

	@Override
	protected void onSizeChanged(int sirina, int visina, int oldw, int oldh) {
		super.onSizeChanged(sirina, visina, oldw, oldh);
		dimenzionisiElemente(sirina, visina);
	}

	/** Odredjuje velicine dugmica. U portret modu dugmici treba da zauzmu celu sirinu ekrana. */
	private void dimenzionisiElemente(int sirinaEkrana, int visinaEkrana) {
		if(sirinaEkrana <= 0 || visinaEkrana <= 0) return;

		int rotacija = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getRotation();

		//ako dugmici imaju dimenziju od ranije, radi se resize, pa moras da zahtevas layout pass kasnije
		boolean postRequestLayout = dugmeMaximize.getLayoutParams().height > 0;
		dimenzionisiDugmice(sirinaEkrana, visinaEkrana,
				rotacija == Surface.ROTATION_0 || rotacija == Surface.ROTATION_180);

		switch (rotacija) {
			case Surface.ROTATION_0:
				dimenzionisiPortrait(sirinaEkrana, visinaEkrana);
				break;
			case Surface.ROTATION_90:
				dimenzionisiLandscape(sirinaEkrana, visinaEkrana, true);
				break;
			case Surface.ROTATION_180:
				dimenzionisiPortrait(sirinaEkrana, visinaEkrana);
				break;
			default:
				dimenzionisiLandscape(sirinaEkrana, visinaEkrana, false);
				break;
		}
		
		setTransparentnostMinimiziranog(transparentnost); //ovo ovde jer imas razlicite pozicine zavisno od pozicije

		//TODO samo kad je resize, ne i kad je prvi put
		if(postRequestLayout)
			post(new Runnable() {
				public void run() {
					requestLayout();
				}
			});
	}
	
	/** Odredjuje dimenzije dugmica zavisno od toga da li se iscrtavaju u horiznotalnoj liniji (portrait)
	 * ili vertikalnoj liniji (landscape) */
	private void dimenzionisiDugmice(int sirinaEkrana, int visinaEkrana, boolean portrait) {
		int novaSirina, novaVisina;
		if(portrait) {
			int maxSirina = sirinaEkrana/(dugmad.size() + 1); //+1 zbog minimize dugmeta
			if(zeljenaSirinaDugmica != INTEGER_NIJE_SPECIFICIRAN && zeljenaSirinaDugmica < maxSirina)
				novaSirina = zeljenaSirinaDugmica;
			else novaSirina = maxSirina;
			novaVisina = zeljenaVisinaDugmica == INTEGER_NIJE_SPECIFICIRAN? novaSirina : zeljenaVisinaDugmica;
		} else {
			int maxVisina = visinaEkrana/(dugmad.size() + 1); //+1 zbog minimize dugmeta
			if(zeljenaVisinaDugmica != INTEGER_NIJE_SPECIFICIRAN && zeljenaVisinaDugmica < maxVisina)
				novaVisina = zeljenaVisinaDugmica;
			else novaVisina = maxVisina;
			novaSirina = zeljenaSirinaDugmica == INTEGER_NIJE_SPECIFICIRAN? novaVisina : zeljenaSirinaDugmica;
		}
		
		ImageButton[] zaFor = dugmad.toArray(new ImageButton[dugmad.size() + 2]);
		zaFor[zaFor.length-2] = dugmeMinimize;
		zaFor[zaFor.length-1] = dugmeMaximize;
		LinearLayout.LayoutParams lp;
		for(ImageButton v : zaFor) {
			if(v == null) return;
			lp = new LinearLayout.LayoutParams(novaVisina, novaSirina);
			v.setLayoutParams(lp);
		}
	}

	private void dimenzionisiPortrait(int sirinaEkrana, int visinaEkrana) {		
		if(pozicijaPortrait == POZICIJA_DOLE) {
			pozicionirajSekciju(sekcijaDugmici, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 
					slikaPozadinaDugmici, new int[] { RelativeLayout.ALIGN_PARENT_BOTTOM });
			pozicionirajSekciju(sekcijaMinimized, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
					slikaPozadinaMinimized, 
					new int[] { RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM });
			pozicionirajSekcije(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, slikaPozadinaSekcije,
					new int[] { RelativeLayout.ABOVE, ID_SEKCIJE_DUGMICI });
			trenutnaPozicija = POZICIJA_DOLE;
		} else {
			pozicionirajSekciju(sekcijaDugmici, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 
					slikaPozadinaDugmiciGore, new int[] { RelativeLayout.ALIGN_PARENT_TOP});
			pozicionirajSekciju(sekcijaMinimized, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
					slikaPozadinaMinimizedGore, 
					new int[] { RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_TOP });
			pozicionirajSekcije(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, slikaPozadinaSekcijeGore,
					new int[] { RelativeLayout.BELOW, ID_SEKCIJE_DUGMICI });
			trenutnaPozicija = POZICIJA_GORE;
		}
		
		sekcijaDugmici.setOrientation(LinearLayout.HORIZONTAL);
	}
	
	private void dimenzionisiLandscape(int sirinaEkrana, int visinaEkrana, boolean okrenutLevo) {
		WindowManager w = ((WindowManager)(getContext().getSystemService(Context.WINDOW_SERVICE)));
		int sirinaProzora = w.getDefaultDisplay().getWidth() - dugmeMinimize.getLayoutParams().width - 10;
		int sirinaSekcija = sirinaProzora/2;
		if(pozicijaLandscape == POZICIJA_LEVO || (pozicijaLandscape == POZICIJA_AUTO
				&& ((pozicijaPortrait == POZICIJA_DOLE && !okrenutLevo) 
						|| (pozicijaPortrait == POZICIJA_GORE && okrenutLevo)))) {
			pozicionirajSekciju(sekcijaDugmici, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 
					slikaPozadinaDugmiciLevo, new int[] { RelativeLayout.ALIGN_PARENT_LEFT});
			pozicionirajSekciju(sekcijaMinimized, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
					slikaPozadinaMinimizedLevo, 
					new int[] { RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_BOTTOM });
			pozicionirajSekcije(sirinaSekcija, LayoutParams.MATCH_PARENT, slikaPozadinaSekcijeLevo,
					new int[] { RelativeLayout.RIGHT_OF, ID_SEKCIJE_DUGMICI});
			trenutnaPozicija = POZICIJA_LEVO;
		} else {
			pozicionirajSekciju(sekcijaDugmici, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 
					slikaPozadinaDugmiciDesno, new int[] { RelativeLayout.ALIGN_PARENT_RIGHT});
			pozicionirajSekciju(sekcijaMinimized, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
					slikaPozadinaMinimized, 
					new int[] { RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM});
			pozicionirajSekcije(sirinaSekcija, LayoutParams.MATCH_PARENT, slikaPozadinaSekcijeDesno,
					new int[] { RelativeLayout.LEFT_OF, ID_SEKCIJE_DUGMICI});
			trenutnaPozicija = POZICIJA_DESNO;
		} 
		
		sekcijaDugmici.setOrientation(LinearLayout.VERTICAL);
	}
	
	/** Pozicionira sve sekcije - zadaje im prosledjenu sirinu/visinu, odgovarajuce orijentisanu pozadinu
	 * i primenjuje niz pravila (za RelativeLayout) na njih. Pravila redom treba da budu formata:<br/>
	 * pravilo, anchor, pravilo, anchor...*/
	private void pozicionirajSekcije(int sirina, int visina, int pozadina, int[] pravila) {
		Drawable drav;
		for(View pogled : paneli) {
			LayoutParams parametri = new LayoutParams(sirina, visina);
			for(int i = 0; i < pravila.length; i+=2) {
				parametri.addRule(pravila[i], pravila[i+1]);
			}
			pogled.setLayoutParams(parametri);
			if(pozadina != INTEGER_NIJE_SPECIFICIRAN) drav = getResources().getDrawable(pozadina);
			else drav = new ColorDrawable(Color.BLACK);
			pogled.setBackground(drav);
		}
	}
	
	/** Pozicionira minimize sekciju - zadaje joj prosledjenu sirinu/visinu, odgovarajuce orijentisanu pozadinu
	 * i primenjuje niz pravila (za RelativeLayout). Pravila redom treba da budu formata:<br/>
	 * pravilo, pravilo, pravilo... (ne koriste se anchori, sve je u odnosu na parent view) */
	private void pozicionirajSekciju(View sekcija, int sirina, int visina, int pozadina, int[] pravila) {
		LayoutParams parametri = new LayoutParams(sirina, visina);
		for(int pravilo : pravila)
			parametri.addRule(pravilo);
		sekcija.setLayoutParams(parametri);
		
		Drawable drav;
		if(pozadina != INTEGER_NIJE_SPECIFICIRAN) drav = getResources().getDrawable(pozadina);
		else drav = new ColorDrawable(Color.BLACK);
		sekcija.setBackground(drav);
	}

	// -------------------------------------- Stanje interfejsa (minimizirano/maksimizirano, prikazana sekcija)s

	/** Prebacuje interfejs u zadato stanje (minimizirano/maksimizirano) */
	private void setStanjeMinimiziranosti(boolean minimiziraj) {
		if(minimiziraj) minimizirajInterfejs();
		else maksimizirajInterfejs();
	}

	/** Prebacuje interfejs iz maksimiziranog u minimizirano stanje. */
	private void minimizirajInterfejs() {
		sekcijaDugmici.setVisibility(GONE);
		sekcijaMinimized.setVisibility(VISIBLE);
		if(prikazanPanel != NIJEDAN_PRIKAZAN) paneli.get(prikazanPanel).setVisibility(GONE);
		
		if(listener!=null) listener.onKontroleMinimized();
	}

	/** Prebacuje interfejs iz minimiziranog u maksimizirano stanje. */
	private void maksimizirajInterfejs() {
		if(prikazanPanel != NIJEDAN_PRIKAZAN) paneli.get(prikazanPanel).setVisibility(VISIBLE);
		sekcijaMinimized.setVisibility(GONE);
		sekcijaDugmici.setVisibility(VISIBLE);
		
		if(listener!=null) listener.onKontroleMaximized();
	}
	
	private void prikaziPanel(int koji) {
		paneli.get(koji).setVisibility(VISIBLE);
		StateListDrawable sld = (StateListDrawable) getResources().getDrawable(android.R.drawable.btn_default);
		sld.setState(PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET);
		dugmad.get(koji).setBackground(sld.getCurrent());
		prikazanPanel = koji;
	}
	
	private void skloniPanel(int koji) {
		paneli.get(koji).setVisibility(GONE);
		dugmad.get(koji).setBackgroundResource(android.R.drawable.btn_default);
		prikazanPanel = NIJEDAN_PRIKAZAN;
	}

	private void onSekcijaDugmeStisnuto(int koja) {
		if(koja == prikazanPanel) {
			skloniPanel(koja);
		} else {
			if(prikazanPanel != NIJEDAN_PRIKAZAN)
				skloniPanel(prikazanPanel);
			prikaziPanel(koja);
		}
	}
	
	/** Namesta providnost minimiziranog interfejsa. Ulaz u procentima. */
	private void setTransparentnostMinimiziranog(int transparentnostProcenat) {
		if(transparentnostProcenat == INTEGER_NIJE_SPECIFICIRAN) return;
		int opacityProcenat = 100 - transparentnostProcenat;
		int odstupanje = 100;
		int alfaDugme = (opacityProcenat * 255)/100;
		int alfaPozadina = (opacityProcenat * (255+odstupanje))/100 - odstupanje; 
		alfaPozadina = alfaPozadina < 0? 0 : alfaPozadina;
		dugmeMaximize.getBackground().setAlpha(alfaDugme);
		if(sekcijaMinimized.getBackground()!=null)
			sekcijaMinimized.getBackground().setAlpha(alfaPozadina);
	}

	// ---------------------------------------------------------------------------------------------------- Listeneri

	@Override
	public void onClick(View v) {
		int svic = (Integer)v.getTag();
		if(svic == TAG_DUGME_MINIMIZE) {
			interfejsMinimiziran = true;
			minimizirajInterfejs();
		} else if(svic == TAG_DUGME_MAXIMIZE) {
			interfejsMinimiziran = false;
			maksimizirajInterfejs();
		} else {
			onSekcijaDugmeStisnuto(svic);
		}
	}
	
	@Override
	public void onIzmeren(int sirina, int visina) {
		if(listener!=null && trenutnaPozicija != INTEGER_NIJE_SPECIFICIRAN)
			listener.onDimenzionisanaSekcijaDugmici(sirina, visina, trenutnaPozicija);
	}
	
	public interface KontroleStateListener {
		void onKontroleMinimized();
		void onKontroleMaximized();
		void onDimenzionisanaSekcijaDugmici(int sirina, int visina, int rotacija);
	}
	
	// ------------------------------------------------------------------------------------------------ Geteri/seteri
	
	public int getSirinaSekcijeDugmici() {
		return sekcijaDugmici.getMeasuredWidth();
	}
	public int getVisinaSekcijeDugmici() {
		return sekcijaDugmici.getMeasuredHeight();
	}
	public int getTrenutnaPozicija() {
		return trenutnaPozicija;
	}
	public void setTransparentnost(int transparentnost) {
		this.transparentnost = transparentnost;
		setTransparentnostMinimiziranog(this.transparentnost);
	}
	public int getTransparentnost() {
		return transparentnost;
	}
	public boolean isPrikazanPanel() {
		return prikazanPanel != NIJEDAN_PRIKAZAN;
	}
	public void skloniPrikazaniPanel() {
		skloniPanel(prikazanPanel);
	}
	public boolean isInterfejsMinimiziran() {
		return interfejsMinimiziran;
	}
	public void setInterfejsMinimiziran(boolean jel) {
		interfejsMinimiziran = jel;
		setStanjeMinimiziranosti(jel);
	}

}

package knez.assdroid.util.gui;

import knez.assdroid.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.EditText;

public class ResetEditTextPreference extends DialogPreference implements TextWatcher {

    private static final boolean DEFAULT_EMPTY_ALLOWED = false;
    private static final String DEFAULT_RESET_TEXT = "Reset";

    private String resetButtonText;
    private boolean emptyAllowed;

    private String vrednost, defaultVrednost;
    private EditText polje;

    public ResetEditTextPreference(Context context, AttributeSet attrs,int defStyle) {
        super(context, attrs, defStyle);
        setValuesFromXml(attrs);
    }
    public ResetEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setValuesFromXml(attrs);
    }

    private void setValuesFromXml(AttributeSet attrs) {
        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ResetEditTextPreferenceAttributes);
            emptyAllowed = a.getBoolean(R.styleable.ResetEditTextPreferenceAttributes_empty_allowed, DEFAULT_EMPTY_ALLOWED);

            resetButtonText = a.getString(R.styleable.ResetEditTextPreferenceAttributes_reset_button_text);
            if(resetButtonText == null) resetButtonText = DEFAULT_RESET_TEXT;

            a.recycle();
        }
    }

    /** Vraca String vrednost - bez obzira da li je zadata kao literal ili kao referenca na zapis u string.xml */
    private String iskopajStringVrednost(AttributeSet atributi, String namespace, String ime, String defaultVrednost) {
        int proveraResurs = atributi.getAttributeResourceValue(namespace, ime, -1);
        String rezultat;
        if(proveraResurs != -1) {
            rezultat = getContext().getResources().getString(proveraResurs);
        } else {
            rezultat = atributi.getAttributeValue(namespace, ime);
        }
        return rezultat == null? "" : rezultat;
    }
    /** Vraca boolean vrednost - bez obzira da li je zadata kao literal ili kao referenca na zapis u bool.xml */
    private boolean iskopajBooleanVrednost(AttributeSet atributi, String namespace, String ime,
            boolean defaultVrednost) {
        int proveraResurs = atributi.getAttributeResourceValue(namespace, ime, -1);
        boolean rezultat;
        if(proveraResurs != -1) {
            rezultat = getContext().getResources().getBoolean(proveraResurs);
        } else {
            rezultat = atributi.getAttributeBooleanValue(namespace, ime, defaultVrednost);
        }
        return rezultat;
    }

    // Ovo se zove svaki put kad korisnik klikne na preferencu, tj. svaki put se pravi novi dijalog.
    // Posto je ovo preferenca sa dijalogom, tek ovde ubacujes vrednost.
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        builder.setNeutralButton(resetButtonText, new NeutralDugmeListener());

        polje = spremiPoljeUnos();
        LinearLayout lejaut = new LinearLayout(getContext());
        lejaut.addView(polje);
        builder.setView(lejaut);
    }

    private EditText spremiPoljeUnos() {
        vrednost = getPersistedString(""); //default moze "" posto si ionako u onSetInitialValue vec namestio default
        EditText rezultat = new EditText(getContext());
        int dimenzija = 4; // 4dp, or w/e, temporary patch
        LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        param.leftMargin = dimenzija;
        param.rightMargin = dimenzija;
        rezultat.setLayoutParams(param);
        rezultat.setText(vrednost);
        rezultat.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        rezultat.setImeOptions(EditorInfo.IME_ACTION_DONE);
        rezultat.setMaxLines(1);
        rezultat.setSelection(vrednost.length());
        rezultat.addTextChangedListener(this);
        return rezultat;
    }

    // Ovde stalno dobijas default value koji si namestio u XMLu i u ovom slucaju ga zapamtis za posle
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        defaultVrednost = a.getString(index);
        defaultVrednost = defaultVrednost == null? "" : defaultVrednost;
        return defaultVrednost;
    }

    // ovo se zove samo jednom po pokretanju aplikacije. Da bi osvezio vrednost svaki put kad prikazujes
    // dijalog, treba da stavis ucitavanje perzistirane vrednosti u onPrepareDialogBuilder
    // P.S. defaultValue ce ti biti ono sto si zadao u XML fajlu, ali samo prvi put kad se otvore preference
    // kasnije ces dobijati null posto ti nece trebati default value vec ces citati iz skladista
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if(restoreValue) {
            vrednost = getPersistedString((String) defaultValue);
        }
        else {
            vrednost = (String) defaultValue;
            persistString(vrednost);
        }
    }

    private class NeutralDugmeListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            polje.setText(defaultVrednost);
            // moras sam persist da zoves ovde, posto onDialogClosed radi samo za positiveResult = true
            // a ovo je neutral button
            persistString(defaultVrednost);
        }
    }

    @Override public void afterTextChanged(Editable s) { }
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(emptyAllowed) return;
        ((AlertDialog)getDialog())
        .getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s!=null && s.toString().trim().length() != 0);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            persistString(polje.getText().toString());
        }
    }

}

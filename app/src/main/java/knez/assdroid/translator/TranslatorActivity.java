package knez.assdroid.translator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import knez.assdroid.App;
import knez.assdroid.R;
import knez.assdroid.common.mvp.CommonSubtitleActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TranslatorActivity extends CommonSubtitleActivity implements TranslatorMVP.ViewInterface {

    public static final String INPUT_LINE_ID =
            TranslatorActivity.class.getCanonicalName() + "input_line_id";

    private static final String INSTANCE_STATE_CURRENT_LINE_ID =
            TranslatorActivity.class.getCanonicalName() + ".current_line_id";
    public static final String INSTANCE_STATE_HAD_CHANGES =
            TranslatorActivity.class.getCanonicalName() + "had_changes";

    // TODO ovi dugmici ti ni ne trebaju
    @BindView(R.id.translator_prev_line) protected TextView prevLineTextView;
    @BindView(R.id.translator_current_line) protected TextView currentLineTextView;
    @BindView(R.id.translator_next_line) protected TextView nextLineTextView;
    @BindView(R.id.translator_input) protected EditText inputView;
    @BindView(R.id.translator_copy_button) protected Button copyButton;
    @BindView(R.id.translator_commit_button) protected Button commitButton;
    @BindView(R.id.trnanslator_commit_next_button) protected Button commitAndNextButton;



    public static final String OUTPUT_LAST_VIEWED_LINE_ID = "output_last_viewed_line_id";
    private static final int ID_AKTIVNOSTI_PODESAVANJA = 1;

    private TranslatorMVP.PresenterInterface presenter;


// TODO ukloni navigatora

    // ------------------------------------------------------------------------------ Zivotni ciklus

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);
        ButterKnife.bind(this);

        long lineId;
        boolean hadChanges;
        if(savedInstanceState != null) {
            lineId = savedInstanceState.getLong(INSTANCE_STATE_CURRENT_LINE_ID, 0L);
            hadChanges = savedInstanceState.getBoolean(INSTANCE_STATE_HAD_CHANGES, false);
        }
        else if(getIntent().getExtras() != null) {
            lineId = getIntent().getLongExtra(INPUT_LINE_ID, 0L);
            hadChanges = false;
        }
        else {
            logger.w("%s did not receive any input data nor has a instance state!", getClass().getCanonicalName());
            lineId = 0L;
            hadChanges = false;
        }

        presenter = App.getAppComponent().getTranslatorPresenter();
        presenter.onAttach(this, lineId, hadChanges);
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        presenter = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(INSTANCE_STATE_CURRENT_LINE_ID, presenter.getCurrentLineId());
        outState.putBoolean(INSTANCE_STATE_HAD_CHANGES, presenter.hasHadChangesToSubtitleMade());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_translator, menu);
        return true;
    }


    // ------------------------------------------------------------------------ USER & SYSYEM EVENTS

    @Override
    public void onBackPressed() {
        Intent output = new Intent();
        output.putExtra(INSTANCE_STATE_HAD_CHANGES, presenter.hasHadChangesToSubtitleMade());
        output.putExtra(OUTPUT_LAST_VIEWED_LINE_ID, presenter.getCurrentLineId());
        setResult(RESULT_OK, output);
        finish();
    }

    @OnClick(R.id.translator_prev_line)
    protected void onPrevLineClicked() {
        presenter.onPrevLineRequested();
    }

    @OnClick(R.id.translator_next_line)
    protected void onNextLineClicked() {
        presenter.onNextLineRequested();
    }

    @OnClick(R.id.translator_commit_button)
    protected void onCommitClicked() {
        presenter.onCommitRequested();
    }

    @OnClick(R.id.trnanslator_commit_next_button)
    protected void onCommitAndNextClicked() {
        presenter.onCommitAndNextRequested();
    }

    @OnClick(R.id.translator_copy_button)
    protected void onCopyClicked() {
        presenter.onCopyCurrentLineToInputRequested();
    }

// TODO proveri jel ti treba ovo
//    @Override
//    public boolean onKey(View v, int keyCode, KeyEvent event) {
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            switch (keyCode) {
//                case KeyEvent.KEYCODE_DPAD_CENTER:
//                case KeyEvent.KEYCODE_ENTER:
//                    commitujIzmene();
//                    premotajNaSledeciRed();
//                    return true;
//            }
//        }
//        return false;
//    }

    @OnEditorAction(R.id.translator_input)
    protected boolean onInputEditorAction(TextView v, int actionId, KeyEvent event) {
        presenter.onCommitAndNextRequested();
        return true;
    }

    @OnTextChanged(value = R.id.translator_input, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void afterInputTextChanged(Editable editable) {
        String text = editable.toString();
        if(text.contains("\n")) {
            text = text.replace("\n", "\\N");
            inputView.setText(text);
            inputView.setSelection(inputView.getText().length());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == ID_AKTIVNOSTI_PODESAVANJA) { // TODO
//            primeniUnosPodesavanja();
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) { // TODO vidi prebaci u nadklasu
//            case R.id.meni_standard_podesavanja:
//                prikaziPodesavanja();
//                break;
//            case R.id.meni_standard_save:
//                snimiPrevod();
//                break;
//            case R.id.meni_standard_help:
//                prikaziHelp();
//                break;
//            default:
//                return false;
//        }
        return true;
    }


    // ------------------------------------------------------------------------------ VIEW INTERFACE

    @Override
    public void closeScreen() {
        onBackPressed();
    }

    @Override
    public void showSubtitleTexts(@NonNull String currentLineText,
                                  @Nullable String previousLineText,
                                  @Nullable String nextLineText) {
        currentLineTextView.setText(currentLineText);
        prevLineTextView.setText(previousLineText == null? "" : previousLineText);
        nextLineTextView.setText(nextLineText == null? "" : nextLineText);
    }

    @Override
    public void resetInputField(@NonNull String hint) {
        inputView.setHint(hint);
        inputView.setText("");
    }

    @Override
    public void setInputText(@NonNull String text) {
        inputView.setText(text);
    }

    @Override @NonNull
    public String getTranslationText() {
        return inputView.getText().toString();
    }

}

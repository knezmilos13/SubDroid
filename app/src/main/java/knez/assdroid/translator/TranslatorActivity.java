package knez.assdroid.translator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import knez.assdroid.App;
import knez.assdroid.R;
import knez.assdroid.common.mvp.CommonSubtitleActivity;
import knez.assdroid.common.mvp.CommonSubtitleMVP;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import java.util.ArrayList;

public class TranslatorActivity extends CommonSubtitleActivity implements TranslatorMVP.ViewInterface {

    public static final String INPUT_LINE_ID =
            TranslatorActivity.class.getCanonicalName() + "input_line_id";

    private static final String INSTANCE_STATE_CURRENT_LINE_ID =
            TranslatorActivity.class.getCanonicalName() + ".current_line_id";
    private static final String INSTANCE_STATE_HAD_CHANGES =
            TranslatorActivity.class.getCanonicalName() + "had_changes";

    public static final String OUTPUT_LAST_VIEWED_LINE_NUMBER =
            TranslatorActivity.class.getCanonicalName() + "output_last_viewed_line_number";
    public static final String OUTPUT_EDITED_LINE_NUMBERS =
            TranslatorActivity.class.getCanonicalName() + "output_edited_line_numbers";

    @BindView(R.id.translator_prev_line) protected TextView prevLineTextView;
    @BindView(R.id.translator_current_line) protected TextView currentLineTextView;
    @BindView(R.id.translator_next_line) protected TextView nextLineTextView;
    @BindView(R.id.translator_input) protected EditText inputView;
    @BindView(R.id.translator_commit_indicator) protected ImageView commitIndicatorView;

    private TranslatorMVP.PresenterInterface presenter;


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

        setUpViews();

        presenter = App.getAppComponent().getTranslatorPresenter();
        presenter.onAttach(this, lineId, hadChanges);
    }

    @Override
    protected CommonSubtitleMVP.PresenterInterface getPresenter() {
        return presenter;
    }

    private void setUpViews() {
        // hack - allows enter to work as ime action even though the field is multiline
        inputView.setHorizontallyScrolling(false);
        inputView.setMaxLines(200); // 200 is a random value not likely to be hit ever

        commitIndicatorView.setImageDrawable(
                new IconDrawable(this, MaterialIcons.md_edit)
                        .sizePx(getResources().getDimensionPixelSize(
                                R.dimen.translator_commit_indicator_dimension))
                        .colorRes(R.color.translator_commit_indicator)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        inputView.addTextChangedListener(inputViewTextWatcher);
    }

    @Override
    protected void onPause() {
        inputView.removeTextChangedListener(inputViewTextWatcher);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        presenter = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(INSTANCE_STATE_CURRENT_LINE_ID, presenter.getCurrentLineNumber());
        outState.putBoolean(INSTANCE_STATE_HAD_CHANGES, presenter.hasHadChangesToSubtitleMade());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_translator, menu);
        return true;
    }


    // ------------------------------------------------------------------------ USER & SYSTEM EVENTS

    @Override
    public void onBackPressed() {
        Intent output = new Intent();
        output.putExtra(OUTPUT_LAST_VIEWED_LINE_NUMBER, presenter.getCurrentLineNumber());
        output.putIntegerArrayListExtra(OUTPUT_EDITED_LINE_NUMBERS,
                new ArrayList<>(presenter.getEditedLineNumbers()));
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

    @OnClick(R.id.translator_prev_button)
    protected void onPrevButtonClicked() {
        presenter.onPrevLineRequested();
    }

    @OnClick(R.id.translator_next_button)
    protected void onNextButtonClicked() {
        presenter.onNextLineRequested();
    }

    @OnClick(R.id.translator_commit_button)
    protected void onCommitClicked() {
        presenter.onCommitRequested();
    }

    @OnClick(R.id.translator_commit_next_button)
    protected void onCommitAndNextClicked() {
        presenter.onCommitAndNextRequested();
    }

    @OnClick(R.id.translator_copy_button)
    protected void onCopyClicked() {
        presenter.onCopyCurrentLineToInputRequested();
    }

    @OnEditorAction(R.id.translator_input)
    protected boolean onInputEditorAction(TextView v, int actionId, KeyEvent event) {
        if(event != null && event.getAction() != KeyEvent.ACTION_DOWN) return false;

        presenter.onCommitAndNextRequested();
        return true;
    }

    private TextWatcher inputViewTextWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            if(text.contains("\n")) {
                text = text.replace("\n", "\\N");
                inputView.setText(text);
                inputView.setSelection(inputView.getText().length());
            }
            presenter.onTextChanged(text);
        }
    };


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
        inputView.setSelection(text.length());
    }

    @Override @NonNull
    public String getTranslationText() {
        return inputView.getText().toString();
    }

    @Override
    public void showCurrentLineEdited(boolean currentLineEdited) {
        commitIndicatorView.setVisibility(currentLineEdited? View.VISIBLE : View.GONE);
    }

}

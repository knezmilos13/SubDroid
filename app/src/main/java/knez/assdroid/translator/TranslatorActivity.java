package knez.assdroid.translator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import knez.assdroid.App;
import knez.assdroid.R;
import knez.assdroid.common.mvp.CommonSubtitleActivity;
import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.util.gui.FadeAnimationHelper;

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
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import java.util.ArrayList;

import static knez.assdroid.translator.TranslatorMVP.*;

public class TranslatorActivity extends CommonSubtitleActivity implements ViewInterface {

    public static final String INPUT_LINE_ID =
            TranslatorActivity.class.getCanonicalName() + ".input_line_id";

    private static final String INSTANCE_STATE_PRESENTER_STATE =
            TranslatorActivity.class.getCanonicalName() + ".presenter_state";

    public static final String OUTPUT_LAST_VIEWED_LINE_NUMBER =
            TranslatorActivity.class.getCanonicalName() + ".output_last_viewed_line_number";
    public static final String OUTPUT_EDITED_LINE_NUMBERS =
            TranslatorActivity.class.getCanonicalName() + ".output_edited_line_numbers";

    @BindView(R.id.translator_prev_line) protected TextView prevLineTextView;
    @BindView(R.id.translator_current_line) protected TextView currentLineTextView;
    @BindView(R.id.translator_next_line) protected TextView nextLineTextView;
    @BindView(R.id.translator_input) protected EditText inputView;
    @BindView(R.id.translator_commit_indicator) protected ImageView commitIndicatorView;
    @BindView(R.id.subtitle_processing_progress) protected View progressBar;
    @BindView(R.id.subtitle_processing_text) protected TextView progressLabel;

    private PresenterInterface presenter;


    // ----------------------------------------------------------------------------------- LIFECYCLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);
        ButterKnife.bind(this);

        setUpViews();

        presenter = App.getAppComponent().getTranslatorPresenter();

        if(savedInstanceState != null) {
            InternalState state = (InternalState)
                    savedInstanceState.getSerializable(INSTANCE_STATE_PRESENTER_STATE);
            if(state == null) {
                logger.w("%s did not receive any presenter state!", getClass().getCanonicalName());
                presenter.onAttach(this);
            }
            else
                presenter.onAttach(this, state);
        }
        else {
            long lineId = getIntent().getLongExtra(INPUT_LINE_ID, 0L);
            if(lineId == 0) {
                logger.w("%s did not receive any input data!", getClass().getCanonicalName());
                presenter.onAttach(this);
            } else
                presenter.onAttach(this, lineId);
        }
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
        outState.putSerializable(INSTANCE_STATE_PRESENTER_STATE, presenter.getInternalState());
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
    public void closeScreenNoSubtitle() {
        Toast.makeText(this, R.string.translator_no_subtitle_data, Toast.LENGTH_LONG);
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

    @Override
    public void showProgressSavingFile() {
        progressLabel.setText(R.string.common_saving_file);
        FadeAnimationHelper.fadeView(true, progressBar, false);
    }

    @Override
    public void hideProgress() {
        FadeAnimationHelper.fadeView(false, progressBar, false);
    }

}
